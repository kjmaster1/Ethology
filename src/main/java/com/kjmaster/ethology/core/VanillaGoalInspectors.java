package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.GoalInspector;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class VanillaGoalInspectors {

    public static final GoalInspector TEMPT_INSPECTOR = goal -> {
        if (!(goal instanceof TemptGoal temptGoal)) return Optional.empty();

        // Access the 'items' field via Access Transformer
        // It is a Predicate<ItemStack>, so we must check if it is an Ingredient
        Predicate<ItemStack> itemsPredicate = temptGoal.items;
        List<String> args = getArgs(itemsPredicate);

        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:vanilla_tempt"),
                new ItemStack(Items.WHEAT), // Generic icon, or specific if needed
                "ethology.trait.goal.temptable",
                args,
                TraitType.GOAL
        ));
    };

    private static @NotNull List<String> getArgs(Predicate<ItemStack> itemsPredicate) {
        List<String> args = new ArrayList<>();

        if (itemsPredicate instanceof Ingredient ingredient) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                // Use the first valid item stack name as the display argument
                args.add(stacks[0].getHoverName().getString());
            } else {
                args.add("Food");
            }
        } else {
            // Fallback if the predicate is a lambda or unknown implementation
            args.add("Items");
        }
        return args;
    }

    public static final GoalInspector BREED_INSPECTOR = goal -> {
        if (!(goal instanceof BreedGoal breedGoal)) return Optional.empty();

        // Access 'partnerClass' via Access Transformer
        Class<?> partnerClass = breedGoal.partnerClass;
        String partnerName = getEntityNameFromClass(partnerClass);

        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:vanilla_breed"),
                new ItemStack(Items.HEART_OF_THE_SEA),
                "ethology.trait.goal.breed_goal", // Reusing existing key or generic
                List.of(partnerName),
                TraitType.GOAL
        ));
    };

    public static final GoalInspector AVOID_ENTITY_INSPECTOR = goal -> {
        if (!(goal instanceof AvoidEntityGoal<?> avoidGoal)) return Optional.empty();

        // Access 'avoidClass' via Access Transformer
        Class<?> avoidClass = avoidGoal.avoidClass;
        String fearName = getEntityNameFromClass(avoidClass);

        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:vanilla_avoid"),
                new ItemStack(Items.BARRIER),
                "ethology.trait.goal.fearful",
                List.of(fearName),
                TraitType.GOAL
        ));
    };

    public static final GoalInspector AGGRESSIVE_TARGET_INSPECTOR = goal -> {
        if (!(goal instanceof NearestAttackableTargetGoal<?> targetGoal)) return Optional.empty();

        Class<?> targetType = targetGoal.targetType;
        String targetName = getEntityNameFromClass(targetType);

        // Customize icon/id based on severity
        boolean isPlayer = Player.class.isAssignableFrom(targetType);
        ResourceLocation id = isPlayer ? ResourceLocation.parse("ethology:vanilla_aggro_player") : ResourceLocation.parse("ethology:vanilla_aggro_mob");
        ItemStack icon = isPlayer ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.CROSSBOW);

        return Optional.of(new MobTrait(
                id,
                icon,
                "ethology.trait.goal.aggressive",
                List.of(targetName),
                TraitType.GOAL
        ));
    };

    /**
     * Helper to resolve a Class<?> to a readable Entity Name.
     */
    private static String getEntityNameFromClass(Class<?> clazz) {
        if (clazz == null) return "Unknown";
        if (Player.class.isAssignableFrom(clazz)) return "Players";

        // Try to find the EntityType that matches this class
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(t -> {
                    // This is an approximation; create(level) would be exact but expensive.
                    // We simply check if the base class of the entity type matches the target class.
                    // Note: This works for 90% of vanilla cases.
                    return clazz.isAssignableFrom(t.getBaseClass()); // e.g. AbstractSkeleton.class is assignable from Skeleton.class
                })
                .findFirst();

        return type.map(entityType -> entityType.getDescription().getString()).orElseGet(() -> clazz.getSimpleName().replaceAll("Entity$", ""));
    }
}