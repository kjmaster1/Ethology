// File: src/main/java/com/kjmaster/ethology/core/EthologyScanner.java
package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.network.RequestScanPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class EthologyScanner {

    // Archetype Scan
    public static void scanEntity(EntityType<?> type) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        try {
            // Local Archetype Analysis
            MobScopedInfo info = EntityAnalyzer.analyze(type, level);
            if (info != null) EthologyDatabase.register(type, info);
        } catch (Exception e) {
            Ethology.LOGGER.warn("Local analysis failed for {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }

        // Remote Request
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type), Optional.empty()));
        }
    }

    // Targeted Scan
    public static void scanTargetedEntity(LivingEntity target) {
        if (Minecraft.getInstance().getConnection() == null) return;

        EntityType<?> type = target.getType();

        // 1. Run local instance analysis
        MobScopedInfo localInfo = null;
        try {
            localInfo = EntityAnalyzer.analyze(target);
        } catch (Exception e) {
            Ethology.LOGGER.warn("Local targeted analysis failed", e);
        }

        // 2. Check for Fresh Cache (Server Data)
        MobScopedInfo cachedInfo = EthologyDatabase.getFreshInstance(target.getUUID());
        if (cachedInfo != null && localInfo != null) {
            // Merge Cache (Capabilities) into Local (State/Stats)
            // We trust server capabilities more, but local state is immediate
            for (MobTrait trait : cachedInfo.getCapabilities()) {
                localInfo.addCapability(trait);
            }
            // (We generally do not merge state from server cache as local observation is newer)

            EthologyDatabase.register(type, localInfo);
            return;
        }

        if (localInfo != null) {
            EthologyDatabase.register(type, localInfo);
        }

        PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type), Optional.of(target.getUUID())));
    }
}