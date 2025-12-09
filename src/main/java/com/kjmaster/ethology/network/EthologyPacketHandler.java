package com.kjmaster.ethology.network;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.EthologyTags;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.core.EntityAnalyzer;
import com.kjmaster.ethology.core.EthologyDatabase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EventBusSubscriber(modid = Ethology.MODID)
public class EthologyPacketHandler {

    // Thread-safe cache to store analyzed archetype data.
    private static final Map<EntityType<?>, MobScopedInfo> ARCHETYPE_CACHE = new ConcurrentHashMap<>();

    // Map to track ongoing scans to prevent duplicate processing for the same entity type.
    private static final Map<EntityType<?>, CompletableFuture<MobScopedInfo>> PENDING_SCANS = new ConcurrentHashMap<>();

    // Rate Limiting: Track request rates per player UUID
    private static final Map<UUID, RateLimiter> PLAYER_LIMITERS = new ConcurrentHashMap<>();

    // Dedicated Executor for Analysis to keep the Main Thread free.
    private static final ExecutorService ANALYSIS_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "Ethology-Analyzer");
                t.setDaemon(true);
                return t;
            }
    );

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Client -> Server
        registrar.playBidirectional(
                RequestScanPayload.TYPE,
                RequestScanPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        // Enforce execution on the Main Server Thread for initial handling
                        context.enqueueWork(() -> handleScanRequest(serverPlayer, payload));
                    }
                }
        );

        // Server -> Client
        registrar.playToClient(
                SyncMobDataPayload.TYPE,
                SyncMobDataPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.info().getEntityId());
                        EthologyDatabase.register(type, payload.info());
                    });
                }
        );
    }

    private static void handleScanRequest(ServerPlayer player, RequestScanPayload payload) {
        // IMPROVEMENT: Packet Safety / Rate Limiting
        // Get or create a rate limiter for this player
        RateLimiter limiter = PLAYER_LIMITERS.computeIfAbsent(player.getUUID(), k -> new RateLimiter(10, 2)); // Max 10 burst, refill 2/sec

        // If the player has exceeded their rate limit, ignore the request.
        if (!limiter.tryAcquire()) {
            // Optionally log a warning if needed, but silent drop is safer for performance during spam attacks.
            return;
        }

        MinecraftServer server = player.serverLevel().getServer();

        // 1. Instance Scan (Targeted Entity) - Keep on Main Thread
        if (payload.instanceId().isPresent()) {
            Entity target = player.serverLevel().getEntity(payload.instanceId().get());
            if (target instanceof LivingEntity living) {
                try {
                    MobScopedInfo info = EntityAnalyzer.analyze(living);
                    player.connection.send(new SyncMobDataPayload(info));
                } catch (Exception e) {
                    Ethology.LOGGER.warn("Failed to analyze specific entity: {}", payload.instanceId(), e);
                }
                return;
            }
        }

        // 2. Archetype Scan
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.typeId());

        // Basic Validation
        if (type == EntityType.PIG && !payload.typeId().equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG))) return;
        if (type.is(EthologyTags.NO_ANALYSIS)) return;
        if (!type.canSummon()) return;

        // CHECK CACHE
        if (ARCHETYPE_CACHE.containsKey(type)) {
            player.connection.send(new SyncMobDataPayload(ARCHETYPE_CACHE.get(type)));
            return;
        }

        // ASYNC ANALYSIS HANDLING
        CompletableFuture<MobScopedInfo> future = PENDING_SCANS.computeIfAbsent(type, k -> {

            // A. Main Thread: Create and Prepare Entity
            LivingEntity living = null;
            try {
                Entity entity = type.create(player.serverLevel());
                if (entity instanceof LivingEntity l) {
                    living = l;
                    try { living.tick(); } catch (Exception ignored) {}
                } else {
                    if (entity != null) entity.discard();
                    return CompletableFuture.completedFuture(null);
                }
            } catch (Exception e) {
                Ethology.LOGGER.warn("Failed to instantiate archetype for analysis: {}", payload.typeId(), e);
                return CompletableFuture.completedFuture(null);
            }

            final LivingEntity entityToAnalyze = living;

            // B. Offload Thread: Heavy Reflection Analysis
            return CompletableFuture.supplyAsync(() -> {
                try {
                    MobScopedInfo info = EntityAnalyzer.analyze(entityToAnalyze);
                    info.setUuid(null);
                    return info;
                } catch (Exception e) {
                    Ethology.LOGGER.error("Async analysis error for {}", type, e);
                    return null;
                }
            }, ANALYSIS_EXECUTOR).whenCompleteAsync((result, ex) -> {
                // C. Main Thread: Cleanup & Cache Update
                entityToAnalyze.discard();

                if (result != null) {
                    ARCHETYPE_CACHE.put(type, result);
                }
                PENDING_SCANS.remove(type);

            }, server);
        });

        // 3. Attach Response Handler
        future.thenAcceptAsync(info -> {
            if (info != null) {
                player.connection.send(new SyncMobDataPayload(info));
            }
        }, server);
    }

    /**
     * Clears the server-side archetype cache and rate limiters.
     */
    public static void clearCache() {
        ARCHETYPE_CACHE.clear();
        PENDING_SCANS.clear();
        PLAYER_LIMITERS.clear();
    }

    /**
     * Simple Token Bucket Rate Limiter.
     */
    private static class RateLimiter {
        private final int capacity;
        private final double refillRatePerMs;
        private double tokens;
        private long lastRefillTimestamp;

        public RateLimiter(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerMs = refillRatePerSecond / 1000.0;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryAcquire() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long delta = now - lastRefillTimestamp;
            if (delta > 0) {
                double tokensToAdd = delta * refillRatePerMs;
                this.tokens = Math.min(capacity, this.tokens + tokensToAdd);
                this.lastRefillTimestamp = now;
            }
        }
    }
}