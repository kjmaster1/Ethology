package com.kjmaster.ethology.network;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.core.EntityAnalyzer;
import com.kjmaster.ethology.core.EthologyDatabase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
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
                    // SERVER SIDE LOGIC
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        context.enqueueWork(() -> handleScanRequest(serverPlayer, payload.entityId()));
                    }
                }
        );

        // Server -> Client
        registrar.playToClient(
                SyncMobDataPayload.TYPE,
                SyncMobDataPayload.STREAM_CODEC,
                (payload, context) -> {
                    // CLIENT SIDE LOGIC
                    context.enqueueWork(() -> {
                        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.info().getEntityId());
                        EthologyDatabase.register(type, payload.info());
                        // Force GUI update if open? (Handled by polling or event in a fuller impl)
                    });
                }
        );
    }

    private static void handleScanRequest(ServerPlayer player, net.minecraft.resources.ResourceLocation entityId) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (type == EntityType.PIG && !entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIG))) {
            // Registry returns PIG (default) if ID is invalid or missing, double check ID match if needed
            return;
        }

        try {
            // Run analysis on the Server Level (has Brains/Goals)
            MobScopedInfo info = EntityAnalyzer.analyze(type, player.serverLevel());
            if (info != null) {
                player.connection.send(new SyncMobDataPayload(info));
            }
        } catch (Exception e) {
            Ethology.LOGGER.warn("Failed to analyze entity on server: {}", entityId, e);
        }
    }
}