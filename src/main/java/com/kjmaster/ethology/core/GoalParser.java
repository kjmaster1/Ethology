package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.GoalInspector;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.function.Consumer;

public class GoalParser {

    public static void parseCapabilities(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        // Analyze Task Goals
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            parseGoal(wrapped, info::addCapability);
        }
        // Analyze Target Goals
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            parseGoal(wrapped, info::addCapability);
        }
    }

    public static void parseCurrentState(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        // Check Running Goals
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.isRunning()) {
                parseGoal(wrapped, info::addCurrentState);
            }
        }
        // Check Running Target Goals (often overlooked, but important for state display)
        for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
            if (wrapped.isRunning()) {
                parseGoal(wrapped, info::addCurrentState);
            }
        }
    }

    private static void parseGoal(Goal inputGoal, Consumer<MobTrait> consumer) {
        Goal innerGoal = unwrap(inputGoal);
        Class<? extends Goal> goalClass = innerGoal.getClass();

        // 1. Registry Lookup (Exact Match)
        GoalInspector inspector = EthologyRegistries.getGoalInspector(goalClass);
        if (inspector != null) {
            inspector.inspect(innerGoal).ifPresent(consumer);
            return;
        }

        // 2. Generic Fallback (Heuristics)
        GenericGoalInspector.INSTANCE.inspect(innerGoal).ifPresent(consumer);
    }

    private static Goal unwrap(Goal goal) {
        Goal current = goal;
        while (current instanceof WrappedGoal wrapped) {
            current = wrapped.getGoal();
        }
        return current;
    }
}