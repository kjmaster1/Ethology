package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.GoalInspector;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GoalParser {

    public static void parseCapabilities(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        // Use a visited set to prevent infinite recursion in cyclic goal references
        Set<Goal> visited = new HashSet<>();

        // Analyze Task Goals
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            analyzeGoal(wrapped, info::addCapability, visited);
        }
        // Analyze Target Goals
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            analyzeGoal(wrapped, info::addCapability, visited);
        }
    }

    public static void parseCurrentState(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        Set<Goal> visited = new HashSet<>();

        // Check Running Goals
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.isRunning()) {
                analyzeGoal(wrapped, info::addCurrentState, visited);
            }
        }
        // Check Running Target Goals
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            if (wrapped.isRunning()) {
                analyzeGoal(wrapped, info::addCurrentState, visited);
            }
        }
    }

    /**
     * Recursively analyzes a Goal to find its true underlying logic.
     * Handles Vanilla wrappers, custom Mod wrappers (heuristically), and Parallel goals.
     */
    private static void analyzeGoal(Goal goal, Consumer<MobTrait> consumer, Set<Goal> visited) {
        if (goal == null) return;
        if (!visited.add(goal)) return; // Prevent cycles

        // 1. Vanilla WrappedGoal (Explicit Unwrap)
        if (goal instanceof WrappedGoal wrapped) {
            analyzeGoal(wrapped.getGoal(), consumer, visited);
            return; // WrappedGoal is just a container, so we don't inspect the wrapper itself
        }

        // 2. Registry Lookup (Exact Match)
        // If the goal is known/registered, we inspect it and STOP recursing.
        // We assume known goals handle their own logic and we don't want to peek inside their private fields.
        GoalInspector inspector = EthologyRegistries.getGoalInspector(goal.getClass());
        if (inspector != null) {
            inspector.inspect(goal).ifPresent(consumer);
            return;
        }

        // 3. Heuristic Wrapper Detection (For Unknown/Modded Goals)
        boolean isWrapper = false;

        // A. Check for single child Goals (Custom Wrapper pattern)
        List<Goal> childGoals = EthologyReflection.getAllFieldsOfType(goal, Goal.class);
        if (!childGoals.isEmpty()) {
            isWrapper = true;
            for (Goal child : childGoals) {
                analyzeGoal(child, consumer, visited);
            }
        }

        // B. Check for Collections of Goals (Parallel/Composite pattern)
        // This is expensive, so we rely on the cached reflection from EthologyReflection.
        List<Collection> collections = EthologyReflection.getAllFieldsOfType(goal, Collection.class);
        for (Collection<?> collection : collections) {
            if (collection == null || collection.isEmpty()) continue;

            // Peek at the first element to see if it's a collection of Goals
            Object first = collection.iterator().next();
            if (first instanceof Goal) {
                isWrapper = true;
                for (Object item : collection) {
                    if (item instanceof Goal childGoal) {
                        analyzeGoal(childGoal, consumer, visited);
                    }
                }
            }
        }

        // 4. Generic Fallback
        // Only run the generic inspector if we didn't identify this as a wrapper.
        // If it WAS a wrapper, we assume the "meat" of the logic was in the children we just recursed on.
        if (!isWrapper) {
            GenericGoalInspector.INSTANCE.inspect(goal).ifPresent(consumer);
        }
    }
}