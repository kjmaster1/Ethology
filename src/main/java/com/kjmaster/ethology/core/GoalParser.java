package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Config;
import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class GoalParser {

    public static void parse(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        // 1. Analyze Task Goals (Behaviors)
        for (WrappedGoal wrapped : mob.goalSelector.availableGoals) {
            parseGoal(wrapped, info);
        }

        // 2. Analyze Target Goals (Agro)
        for (WrappedGoal wrapped : mob.targetSelector.availableGoals) {
            parseGoal(wrapped, info);
        }
    }

    private static void parseGoal(Goal inputGoal, MobScopedInfo info) {
        // Recursive Unwrapping ensures we find the real logic even if wrapped by other mods
        Goal innerGoal = unwrap(inputGoal);

        // A. Registry Lookup (Static JSON Traits)
        // This handles standard goals defined in JSON (e.g., Breed, Panic, Float)
        Optional<MobTrait> traitOpt = Ethology.TRAIT_MANAGER.getTrait(innerGoal.getClass());

        if (traitOpt.isPresent()) {
            addUniqueTrait(info, traitOpt.get());
        } else if (Config.DEBUG_MODE.get()) {
            // Debug Mode: Render unknown goals so developers know what JSONs to create
            addUniqueTrait(info, new MobTrait(
                    new ItemStack(Items.BARRIER),
                    Component.literal("Unknown Goal"),
                    Component.literal(innerGoal.getClass().getName())
            ));
        }

        // B. Dynamic Goal Analysis via Registry
        // This allows code-based extraction of data (e.g., extracting specific items from TemptGoal)
        GoalParserRegistry.getParser(innerGoal).ifPresent(parser ->
                parser.parse(innerGoal, info)
        );
    }

    /**
     * Recursively unwraps the goal to handle nested wrappers (e.g. from AI optimization mods).
     */
    private static Goal unwrap(Goal goal) {
        Goal current = goal;
        while (current instanceof WrappedGoal wrapped) {
            current = wrapped.getGoal();
        }
        return current;
    }

    private static void addUniqueTrait(MobScopedInfo info, MobTrait trait) {
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals(trait.title().getString()))) {
            info.addTrait(trait);
        }
    }
}