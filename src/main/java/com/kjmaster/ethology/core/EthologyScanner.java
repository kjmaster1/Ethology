package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EthologyScanner {

    public static void scanAll(Level level) {
        if (EthologyDatabase.isScanned()) return;

        long startTime = System.currentTimeMillis();
        Ethology.LOGGER.info("Starting Ethology Entity Scan...");

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            try {
                // We attempt to create an instance of every entity.
                // We wrap this in try-catch because some mods might have entities that crash
                // if created in specific ways or without specific NBT.
                Entity entity = type.create(level);

                if (entity instanceof LivingEntity living) {
                    // This triggers our GoalParser and BrainParser logic
                    MobScopedInfo info = EntityAnalyzer.analyze(type, level);

                    if (info != null && !info.getTraits().isEmpty()) {
                        EthologyDatabase.register(type, info);
                    }

                    // Cleanup the dummy entity
                    living.discard();
                }
            } catch (Exception e) {
                // Log warning but continue scanning other mobs
                Ethology.LOGGER.warn("Ethology failed to analyze entity: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
            }
        }

        EthologyDatabase.setScanned(true);
        Ethology.LOGGER.info("Ethology Scan Complete. Analyzed {} entities in {}ms.",
                EthologyDatabase.getAll().size(),
                System.currentTimeMillis() - startTime);
    }
}