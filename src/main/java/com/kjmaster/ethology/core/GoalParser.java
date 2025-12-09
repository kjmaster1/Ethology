package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Set;
import java.util.function.Predicate;

public class GoalParser {

    public static void parse(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        // 1. Analyze Task Goals (Behaviors)
        Set<WrappedGoal> goals = mob.goalSelector.availableGoals;
        for (WrappedGoal wrapped : goals) {
            Goal innerGoal = wrapped.getGoal();

            // A. Registry Lookup
            // Handles standard static goals defined in JSON (e.g., Breed, Panic, Float, Melee, Wander, etc.)
            Ethology.TRAIT_MANAGER.getTrait(innerGoal.getClass()).ifPresent(trait ->
                    addUniqueTrait(info, trait)
            );

            // B. Dynamic Goal Analysis
            // Handles complex goals that require extracting specific instance data
            if (innerGoal instanceof TemptGoal temptGoal) {
                parseTempt(temptGoal, info);
            } else if (innerGoal instanceof AvoidEntityGoal<?> avoidEntityGoal) {
                parseAvoid(avoidEntityGoal, info);
            }
        }

        // 2. Analyze Target Goals (Agro)
        Set<WrappedGoal> targets = mob.targetSelector.availableGoals;
        for (WrappedGoal wrapped : targets) {
            Goal innerGoal = wrapped.getGoal();

            // A. Registry Lookup for targets (e.g., HurtByTargetGoal defined in JSON)
            Ethology.TRAIT_MANAGER.getTrait(innerGoal.getClass()).ifPresent(trait ->
                    addUniqueTrait(info, trait)
            );

            // B. Dynamic Target Analysis
            if (innerGoal instanceof NearestAttackableTargetGoal<?> targetGoal) {
                parseTarget(targetGoal, info);
            }
        }
    }

    private static void addUniqueTrait(MobScopedInfo info, MobTrait trait) {
        // Prevent duplicate traits (e.g., if a generic JSON trait conflicts with a dynamic one)
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals(trait.title().getString()))) {
            info.addTrait(trait);
        }
    }

    // --- Dynamic Parsing Logic ---

    private static void parseTempt(TemptGoal goal, MobScopedInfo info) {
        Predicate<ItemStack> predicate = goal.items;
        // Attempt to inspect the ingredient inside the predicate
        if (predicate instanceof Ingredient ingredient && ingredient.getItems().length > 0) {
            addUniqueTrait(info, new MobTrait(ingredient.getItems()[0], Component.literal("Temptable"), Component.literal("Follows players holding this.")));
        } else {
            // Fallback if we can't extract the specific item
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.WHEAT), Component.literal("Temptable"), Component.literal("Follows players holding food.")));
        }
    }

    private static void parseAvoid(AvoidEntityGoal<?> goal, MobScopedInfo info) {
        Class<?> scaredOf = goal.avoidClass;
        addUniqueTrait(info, new MobTrait(new ItemStack(Items.BARRIER), Component.literal("Fearful"), Component.literal("Flees from " + scaredOf.getSimpleName())));
    }

    private static void parseTarget(NearestAttackableTargetGoal<?> goal, MobScopedInfo info) {
        Class<?> targetClass = goal.targetType;
        String name = targetClass.getSimpleName();

        if (Player.class.isAssignableFrom(targetClass)) {
            // Check for "Hostile" trait to avoid redundancy if strictly defined in JSON
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.RED_DYE), Component.literal("Hostile"), Component.literal("Aggressive towards players.")));
        } else {
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.CROSSBOW), Component.literal("Hunter"), Component.literal("Hunts " + name + "s.")));
        }
    }
}