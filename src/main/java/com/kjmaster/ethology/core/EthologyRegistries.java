package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.ActivityInspector;
import com.kjmaster.ethology.api.GoalInspector;
import com.kjmaster.ethology.api.MemoryInspector;
import com.kjmaster.ethology.api.SensorInspector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EthologyRegistries {
    private static final Map<Class<? extends Goal>, GoalInspector> GOAL_INSPECTORS = new ConcurrentHashMap<>();
    private static final Map<SensorType<?>, SensorInspector> SENSOR_INSPECTORS = new ConcurrentHashMap<>();
    private static final Map<MemoryModuleType<?>, MemoryInspector> MEMORY_INSPECTORS = new ConcurrentHashMap<>();
    private static final Map<Activity, ActivityInspector> ACTIVITY_INSPECTORS = new ConcurrentHashMap<>();

    // --- Goals ---
    public static void registerGoal(Class<? extends Goal> type, GoalInspector inspector) {
        GOAL_INSPECTORS.put(type, inspector);
    }

    public static GoalInspector getGoalInspector(Class<? extends Goal> type) {
        return GOAL_INSPECTORS.get(type);
    }

    // --- Sensors ---
    public static void registerSensor(SensorType<?> type, SensorInspector inspector) {
        SENSOR_INSPECTORS.put(type, inspector);
    }

    public static SensorInspector getSensorInspector(SensorType<?> type) {
        return SENSOR_INSPECTORS.get(type);
    }

    // --- Memories ---
    public static void registerMemory(MemoryModuleType<?> type, MemoryInspector inspector) {
        MEMORY_INSPECTORS.put(type, inspector);
    }

    public static MemoryInspector getMemoryInspector(MemoryModuleType<?> type) {
        return MEMORY_INSPECTORS.get(type);
    }

    // --- Activities ---
    public static void registerActivity(Activity activity, ActivityInspector inspector) {
        ACTIVITY_INSPECTORS.put(activity, inspector);
    }

    public static ActivityInspector getActivityInspector(Activity activity) {
        return ACTIVITY_INSPECTORS.get(activity);
    }
}