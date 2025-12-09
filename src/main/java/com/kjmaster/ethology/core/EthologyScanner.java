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

import java.util.Optional;

public class EthologyScanner {

    // Existing archetype scan
    public static void scanEntity(EntityType<?> type) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        // Local Guess
        try {
            Entity entity = type.create(level);
            if (entity instanceof LivingEntity living) {
                MobScopedInfo info = EntityAnalyzer.analyze(type, level);
                if (info != null) EthologyDatabase.register(type, info);
                living.discard();
            }
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

        // 1. Run local analysis immediately on the client instance (gets visible data like Health/Armor)
        try {
            MobScopedInfo info = EntityAnalyzer.analyze(target);
            EthologyDatabase.register(type, info);
        } catch (Exception e) {
            Ethology.LOGGER.warn("Local targeted analysis failed", e);
        }

        // 2. Request deep analysis from Server (gets Brain/Goals)
        PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type), Optional.of(target.getUUID())));
    }
}