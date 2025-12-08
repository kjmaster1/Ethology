package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.network.RequestScanPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class EthologyScanner {

    /**
     * Triggers the analysis for a specific entity type.
     * 1. Runs local analysis immediately (fast, partial data).
     * 2. Requests server analysis (slow, full data).
     */
    public static void scanEntity(EntityType<?> type) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        // 1. Local Analysis (Client Thread)
        // Checks basic stats and class hierarchy.
        try {
            // We do a fresh analyze every time to ensure we get the latest data,
            // or we could check EthologyDatabase.get(type) first if we implemented a timestamp.
            // For now, re-analyzing ensures we try to get "Better" data if previous was partial.
            Entity entity = type.create(level);
            if (entity instanceof LivingEntity living) {
                MobScopedInfo info = EntityAnalyzer.analyze(type, level);
                if (info != null) {
                    EthologyDatabase.register(type, info);
                }
                living.discard();
            }
        } catch (Exception e) {
            Ethology.LOGGER.warn("Local analysis failed for {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }

        // 2. Remote Analysis Request
        if (Minecraft.getInstance().getConnection() != null) {
            try {
                PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
            } catch (Exception e) {
                Ethology.LOGGER.debug("Could not send scan request packet.");
            }
        }
    }
}