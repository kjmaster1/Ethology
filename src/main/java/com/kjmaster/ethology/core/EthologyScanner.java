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

    // Existing archetype scan
    public static void scanEntity(EntityType<?> type) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        // Local Guess
        try {
            // Note: EntityAnalyzer now handles the "Deny List" and try-catch safety internally
            MobScopedInfo info = EntityAnalyzer.analyze(type, level);
            if (info != null) EthologyDatabase.register(type, info);
        } catch (Exception e) {
            Ethology.LOGGER.warn("Local analysis failed for {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }

        // Remote Request (Archetype, no UUID)
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type), Optional.empty()));
        }
    }

    // New Targeted Scan
    public static void scanTargetedEntity(LivingEntity target) {
        if (Minecraft.getInstance().getConnection() == null) return;

        EntityType<?> type = target.getType();

        // 1. Run local analysis immediately (gets visible data like Health/Armor)
        MobScopedInfo localInfo = null;
        try {
            localInfo = EntityAnalyzer.analyze(target);
        } catch (Exception e) {
            Ethology.LOGGER.warn("Local targeted analysis failed", e);
        }

        // 2. Check for Fresh Data (Rate Limiting)
        MobScopedInfo cachedInfo = EthologyDatabase.getFreshInstance(target.getUUID());
        if (cachedInfo != null && localInfo != null) {
            // Merge Cached Traits (Brain/Goals) into Local Info (Realtime Health)
            // We use the cached server data to fill in the "Brain" gaps of the local scan
            for (MobTrait trait : cachedInfo.getTraits()) {
                // Simple deduplication based on title
                boolean exists = localInfo.getTraits().stream()
                        .anyMatch(t -> t.title().getString().equals(trait.title().getString()));

                if (!exists) {
                    localInfo.addTrait(trait);
                }
            }

            // Register the merged result to update the UI immediately
            EthologyDatabase.register(type, localInfo);
            return; // SKIP NETWORK REQUEST
        }

        // 3. If no fresh cache, register local info and request deep analysis
        if (localInfo != null) {
            EthologyDatabase.register(type, localInfo);
        }

        PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type), Optional.of(target.getUUID())));
    }
}