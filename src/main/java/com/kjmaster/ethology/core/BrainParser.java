package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;

public class BrainParser {

    public static void parse(LivingEntity entity, MobScopedInfo info) {
        Brain<?> brain = entity.getBrain();
        parseSensors(brain, info);
        parseMemories(brain, info);
        parseActivities(brain, info);
    }

    private static void parseActivities(Brain<?> brain, MobScopedInfo info) {
        // Iterate over all activities in the brain
        for (Map<Activity, ?> innerMap : brain.availableBehaviorsByPriority.values()) {
            for (Activity activity : innerMap.keySet()) {
                // Query the Manager
                Ethology.TRAIT_MANAGER.getTrait(activity).ifPresent(trait ->
                        addUniqueTrait(info, trait)
                );
            }
        }
    }

    private static void parseSensors(Brain<?> brain, MobScopedInfo info) {
        for (SensorType<?> sensorType : brain.sensors.keySet()) {
            Ethology.TRAIT_MANAGER.getTrait(sensorType).ifPresent(trait ->
                    addUniqueTrait(info, trait)
            );
        }
    }

    private static void parseMemories(Brain<?> brain, MobScopedInfo info) {
        for (MemoryModuleType<?> type : brain.memories.keySet()) {
            Ethology.TRAIT_MANAGER.getTrait(type).ifPresent(trait ->
                    addUniqueTrait(info, trait)
            );
        }
    }

    private static void addUniqueTrait(MobScopedInfo info, MobTrait trait) {
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals(trait.title().getString()))) {
            info.addTrait(trait);
        }
    }
}