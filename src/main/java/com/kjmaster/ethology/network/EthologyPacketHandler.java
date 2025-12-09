package com.kjmaster.ethology.network;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.core.EntityAnalyzer;
import com.kjmaster.ethology.core.EthologyDatabase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Ethology.MODID)
public class EthologyPacketHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Client -> Server
        registrar.playBidirectional(
                RequestScanPayload.TYPE,
                RequestScanPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
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
        // 1. Try to find the specific entity instance if UUID is provided
        if (payload.instanceId().isPresent()) {
            Entity target = player.serverLevel().getEntity(payload.instanceId().get());
            if (target instanceof LivingEntity living) {
                try {
                    MobScopedInfo info = EntityAnalyzer.analyze(living);
                    player.connection.send(new SyncMobDataPayload(info));
                } catch (Exception e) {
                    Ethology.LOGGER.warn("Failed to analyze specific entity: {}", payload.instanceId(), e);
                }
                return; // Targeted scan complete
            }
        }

        // 2. Fallback: Archetype Scan (Original behavior)
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.typeId());
        if (type == EntityType.PIG && !payload.typeId().equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG))) {
            return; // Invalid ID
        }

        try {
            MobScopedInfo info = EntityAnalyzer.analyze(type, player.serverLevel());
            if (info != null) {
                player.connection.send(new SyncMobDataPayload(info));
            }
        } catch (Exception e) {
            Ethology.LOGGER.warn("Failed to analyze entity archetype: {}", payload.typeId(), e);
        }
    }
}