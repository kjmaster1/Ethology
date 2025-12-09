package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.GoalInspector;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenericGoalInspector {

    public static final GoalInspector INSTANCE = goal -> {
        // 1. Check for Item Interaction (Ingredients, ItemStacks, Items)
        // This usually implies tempting, breeding, picking up, or using an item.
        List<String> itemArgs = scanForItems(goal);
        if (!itemArgs.isEmpty()) {
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:generic_interact"),
                    new ItemStack(Items.SPYGLASS), // Symbolizes investigation/interaction
                    "ethology.trait.goal.generic_interact",
                    itemArgs,
                    TraitType.GOAL
            ));
        }

        // 2. Check for Entity Interaction (Targeting, Fearing, Defending)
        // This implies the goal is focused on a specific type of mob.
        List<String> entityArgs = scanForEntities(goal);
        if (!entityArgs.isEmpty()) {
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:generic_target"),
                    new ItemStack(Items.TARGET),
                    "ethology.trait.goal.generic_target",
                    entityArgs,
                    TraitType.GOAL
            ));
        }

        return Optional.empty();
    };

    private static List<String> scanForItems(Object goal) {
        List<String> names = new ArrayList<>();

        // Scan for Ingredients (Common in Tempt/Breed goals)
        List<Ingredient> ingredients = EthologyReflection.getAllFieldsOfType(goal, Ingredient.class);
        for (Ingredient ingredient : ingredients) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                names.add(stacks[0].getHoverName().getString());
            }
        }

        // Scan for direct ItemStacks (Common in Pickup goals)
        List<ItemStack> stacks = EthologyReflection.getAllFieldsOfType(goal, ItemStack.class);
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                names.add(stack.getHoverName().getString());
            }
        }

        // Scan for direct Items
        List<Item> items = EthologyReflection.getAllFieldsOfType(goal, Item.class);
        for (Item item : items) {
            if (item != Items.AIR) {
                names.add(item.getDescription().getString());
            }
        }

        return names.stream().distinct().limit(3).collect(Collectors.toList());
    }

    private static List<String> scanForEntities(Object goal) {
        List<String> names = new ArrayList<>();

        // Scan for Class fields (e.g. Class<T> targetType)
        List<Class> classes = EthologyReflection.getAllFieldsOfType(goal, Class.class);

        for (Class<?> clazz : classes) {
            // We only care if the class is a type of LivingEntity
            if (LivingEntity.class.isAssignableFrom(clazz)) {
                String entityName = getEntityName(clazz);
                names.add(entityName);
            }
        }

        return names.stream().distinct().limit(3).collect(Collectors.toList());
    }

    private static String getEntityName(Class<?> clazz) {
        // Attempt to find the EntityType associated with this class
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(t -> clazz.isAssignableFrom(t.getBaseClass()))
                .findFirst();

        return type.map(t -> t.getDescription().getString())
                .orElseGet(() -> clazz.getSimpleName().replace("Entity", ""));
    }
}