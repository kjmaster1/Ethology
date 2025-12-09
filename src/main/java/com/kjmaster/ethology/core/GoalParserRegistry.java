package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.IGoalParser;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.resources.ResourceLocation;
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
        Class<?> current = goal.getClass();
        while (current != Object.class && current != null) {
            if (REGISTRY.containsKey(current)) {
                return Optional.of((IGoalParser<T>) REGISTRY.get(current));
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    static {
        register(TemptGoal.class, (goal, consumer) -> {
            Predicate<ItemStack> predicate = goal.items;
            MobTrait trait;
            ResourceLocation id = ResourceLocation.parse("ethology:generated_tempt");

            // Note: We use dynamic translation keys here or reuse generic ones with arguments in a real scenario.
            // For now, we fit into the existing scheme using TraitType.GOAL

            if (predicate instanceof Ingredient ingredient && ingredient.getItems().length > 0) {
                trait = new MobTrait(id, ingredient.getItems()[0], "ethology.trait.goal.temptable", TraitType.GOAL);
            } else {
                trait = new MobTrait(id, new ItemStack(Items.WHEAT), "ethology.trait.goal.temptable", TraitType.GOAL);
            }
            consumer.accept(trait);
        });

        register(AvoidEntityGoal.class, (goal, consumer) -> {
            Class<?> scaredOf = goal.avoidClass;
            ResourceLocation id = ResourceLocation.parse("ethology:generated_fear");
            // In a full implementation, we might want dynamic names, but for now we map to the generic "Fearful" trait
            consumer.accept(new MobTrait(id, new ItemStack(Items.BARRIER), "ethology.trait.goal.fearful", TraitType.GOAL));
        });

        register(NearestAttackableTargetGoal.class, (goal, consumer) -> {
            Class<?> targetClass = goal.targetType;
            ResourceLocation id = ResourceLocation.parse("ethology:generated_aggro");
            if (Player.class.isAssignableFrom(targetClass)) {
                consumer.accept(new MobTrait(id, new ItemStack(Items.RED_DYE), "ethology.trait.goal.aggressive", TraitType.GOAL));
            } else {
                consumer.accept(new MobTrait(id, new ItemStack(Items.CROSSBOW), "ethology.trait.goal.aggressive", TraitType.GOAL));
            }
        });
    }
}