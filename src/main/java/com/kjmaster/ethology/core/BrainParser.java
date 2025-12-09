package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.ActivityInspector;
import com.kjmaster.ethology.api.MemoryInspector;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.SensorInspector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Optional;

public class BrainParser {

    // Helper: Static Traits (Capabilities)
    public static void parseCapabilities(LivingEntity entity, MobScopedInfo info) {
        Brain<?> brain = entity.getBrain();

        // 1. Sensors
        // Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>>
        for (var entry : brain.sensors.entrySet()) {
            SensorType<?> type = entry.getKey();
            Sensor<?> sensor = entry.getValue();

            SensorInspector inspector = EthologyRegistries.getSensorInspector(type);
            if (inspector != null) {
                inspector.inspect(type, sensor).ifPresent(info::addCapability);
            }
        }

        // 2. Memories (Potential)
        // Check what memories are registered in the brain schema
        for (MemoryModuleType<?> type : brain.memories.keySet()) {
            MemoryInspector inspector = EthologyRegistries.getMemoryInspector(type);
            if (inspector != null) {
                // Pass empty value for capability check (we only care that it CAN remember this)
                inspector.inspect(type, Optional.empty()).ifPresent(info::addCapability);
            }
        }

        // 3. Activities (Potential Behaviors)
        // Map<Integer, Map<Activity, Set<Behavior<? super E>>>>
        for (Map<Activity, ?> innerMap : brain.availableBehaviorsByPriority.values()) {
            for (Activity activity : innerMap.keySet()) {
                ActivityInspector inspector = EthologyRegistries.getActivityInspector(activity);
                if (inspector != null) {
                    inspector.inspect(activity).ifPresent(info::addCapability);
                }
            }
        }
    }

    // Helper: Dynamic State
    public static void parseCurrentState(LivingEntity entity, MobScopedInfo info) {
        Brain<?> brain = entity.getBrain();

        // 1. Active Memories (What is the mob thinking about RIGHT NOW?)
        // Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>>
        for (Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : brain.memories.entrySet()) {
            // Check if the memory actually has a value stored
            if (entry.getValue().isPresent()) {
                MemoryModuleType<?> type = entry.getKey();

                // Access the raw value via the ExpirableValue wrapper
                // Uses the Access Transformer for 'value' field in ExpirableValue
                Object rawValue = entry.getValue().get().value;

                MemoryInspector inspector = EthologyRegistries.getMemoryInspector(type);
                if (inspector != null) {
                    inspector.inspect(type, Optional.ofNullable(rawValue)).ifPresent(info::addCurrentState);
                }
            }
        }

        // 2. Active Activities (What is the mob doing RIGHT NOW?)
        for (Activity activity : brain.activeActivities) {
            ActivityInspector inspector = EthologyRegistries.getActivityInspector(activity);
            if (inspector != null) {
                inspector.inspect(activity).ifPresent(info::addCurrentState);
            }
        }
    }
}