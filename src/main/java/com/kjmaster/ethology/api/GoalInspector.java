package com.kjmaster.ethology.api;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.Optional;

/**
 * Interface for analyzing AI Goals.
 * Implementations of this interface inspect a Goal instance (heuristically or via type checking)
 * and return a descriptive MobTrait if applicable.
 */
@FunctionalInterface
public interface GoalInspector {

    /**
     * Inspects a specific AI Goal to determine if it corresponds to a known behavior (Trait).
     *
     * @param goal The goal instance to inspect.
     * @return An Optional containing the generated MobTrait if the goal is recognized, or empty otherwise.
     */
    Optional<MobTrait> inspect(Goal goal);
}