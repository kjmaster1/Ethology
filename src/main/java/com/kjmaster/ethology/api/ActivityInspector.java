package com.kjmaster.ethology.api;

import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

/**
 * Interface for analyzing AI Activities (high-level states like Idle, Work, Panic).
 */
@FunctionalInterface
public interface ActivityInspector {
    /**
     * Inspects an Activity.
     *
     * @param activity The Activity being inspected.
     * @return An Optional containing the MobTrait if recognized.
     */
    Optional<MobTrait> inspect(Activity activity);
}