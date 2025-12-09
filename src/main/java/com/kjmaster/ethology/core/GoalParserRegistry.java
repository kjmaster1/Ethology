package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.IGoalParser;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class GoalParserRegistry {
    private static final Map<Class<? extends Goal>, IGoalParser<?>> REGISTRY = new HashMap<>();

    public static <T extends Goal> void register(Class<T> type, IGoalParser<T> parser) {
        REGISTRY.put(type, parser);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Goal> Optional<IGoalParser<T>> getParser(T goal) {
        // Walk class hierarchy to find a registered parser for the goal or its parents
        Class<?> current = goal.getClass();
        while (current != Object.class && current != null) {
            if (REGISTRY.containsKey(current)) {
                return Optional.of((IGoalParser<T>) REGISTRY.get(current));
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    // Initialize Default Parsers
    static {
        register(TemptGoal.class, (goal, info) -> {
            Predicate<ItemStack> predicate = goal.items;
            if (predicate instanceof Ingredient ingredient && ingredient.getItems().length > 0) {
                addUniqueTrait(info, new MobTrait(ingredient.getItems()[0], Component.literal("Temptable"), Component.literal("Follows players holding this.")));
            } else {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.WHEAT), Component.literal("Temptable"), Component.literal("Follows players holding food.")));
            }
        });

        register(AvoidEntityGoal.class, (goal, info) -> {
            Class<?> scaredOf = goal.avoidClass;
            String name = scaredOf.getSimpleName();
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.BARRIER), Component.literal("Fearful"), Component.literal("Flees from " + name)));
        });

        register(NearestAttackableTargetGoal.class, (goal, info) -> {
            Class<?> targetClass = goal.targetType;
            String name = targetClass.getSimpleName();
            if (Player.class.isAssignableFrom(targetClass)) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.RED_DYE), Component.literal("Hostile"), Component.literal("Aggressive towards players.")));
            } else {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.CROSSBOW), Component.literal("Hunter"), Component.literal("Hunts " + name + "s.")));
            }
        });
    }

    private static void addUniqueTrait(MobScopedInfo info, MobTrait trait) {
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals(trait.title().getString()))) {
            info.addTrait(trait);
        }
    }
}