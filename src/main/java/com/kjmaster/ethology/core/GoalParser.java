// File: src/main/java/com/kjmaster/ethology/core/GoalParser.java
package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Config;
import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
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
        // WrappedGoal usually exposes isRunning() if getRunningGoals() isn't available easily
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.isRunning()) {
                parseGoal(wrapped, info::addCurrentState);
            }
        }
    }

    private static void parseGoal(Goal inputGoal, Consumer<MobTrait> consumer) {
        Goal innerGoal = unwrap(inputGoal);

        // A. Static JSON Lookup
        Optional<MobTrait> traitOpt = Ethology.TRAIT_MANAGER.getTrait(innerGoal.getClass());
        if (traitOpt.isPresent()) {
            consumer.accept(traitOpt.get());
        } else if (Config.DEBUG_MODE.get()) {
            consumer.accept(new MobTrait(
                    ResourceLocation.parse("ethology:debug_" + innerGoal.getClass().getSimpleName().toLowerCase()),
                    new ItemStack(Items.BARRIER),
                    "ethology.trait.goal.unknown",
                    TraitType.GOAL
            ));
        }

        // B. Dynamic Analysis
        GoalParserRegistry.getParser(innerGoal).ifPresent(parser ->
                parser.parse(innerGoal, consumer)
        );
    }

    private static Goal unwrap(Goal goal) {
        Goal current = goal;
        while (current instanceof WrappedGoal wrapped) {
            current = wrapped.getGoal();
        }
        return current;
    }
}