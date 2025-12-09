package com.kjmaster.ethology.api;

import com.kjmaster.ethology.core.EthologyRegistries;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired on the Mod Bus to allow registration of Ethology Inspectors.
 */
public class RegisterEthologyInspectorsEvent extends Event implements IModBusEvent {

    public void registerGoal(Class<? extends Goal> goalClass, GoalInspector inspector) {
        EthologyRegistries.registerGoal(goalClass, inspector);
    }

    public void registerSensor(SensorType<?> sensorType, SensorInspector inspector) {
        EthologyRegistries.registerSensor(sensorType, inspector);
    }

    public void registerMemory(MemoryModuleType<?> memoryType, MemoryInspector inspector) {
        EthologyRegistries.registerMemory(memoryType, inspector);
    }

    public void registerActivity(Activity activity, ActivityInspector inspector) {
        EthologyRegistries.registerActivity(activity, inspector);
    }
}