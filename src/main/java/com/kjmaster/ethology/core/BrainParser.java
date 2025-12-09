package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;

public class BrainParser {

    // Helper: Static Traits (Capabilities)
    public static void parseCapabilities(LivingEntity entity, MobScopedInfo info) {
        Brain<?> brain = entity.getBrain();

        // 1. Sensors
        for (SensorType<?> sensorType : brain.sensors.keySet()) {
            Ethology.TRAIT_MANAGER.getTrait(sensorType).ifPresent(info::addCapability);
        }

        // 2. Memories (Potential)
        for (MemoryModuleType<?> type : brain.memories.keySet()) {
            Ethology.TRAIT_MANAGER.getTrait(type).ifPresent(info::addCapability);
        }

        // 3. Activities (Potential Behaviors)
        for (Map<Activity, ?> innerMap : brain.availableBehaviorsByPriority.values()) {
            for (Activity activity : innerMap.keySet()) {
                Ethology.TRAIT_MANAGER.getTrait(activity).ifPresent(info::addCapability);
            }
        }
    }

    // Helper: Dynamic State
    public static void parseCurrentState(LivingEntity entity, MobScopedInfo info) {
        Brain<?> brain = entity.getBrain();

        // Active Activities
        // In 1.21, activeActivities is the set of currently running activities
        for (Activity activity : brain.activeActivities) {
            Ethology.TRAIT_MANAGER.getTrait(activity).ifPresent(info::addCurrentState);
        }
    }
}