package com.kjmaster.ethology.api;

import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Optional;

/**
 * Interface for analyzing AI Sensors.
 */
@FunctionalInterface
public interface SensorInspector {
    /**
     * Inspects a Sensor to determine if it provides a specific behavior or capability.
     *
     * @param type   The SensorType being inspected.
     * @param sensor The Sensor instance.
     * @return An Optional containing the MobTrait if recognized.
     */
    Optional<MobTrait> inspect(SensorType<?> type, Sensor<?> sensor);
}