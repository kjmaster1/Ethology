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

    // --- INTERACTION / COMPLEX ---

    public static final GoalInspector TEMPT_INSPECTOR = goal -> {
        if (!(goal instanceof TemptGoal temptGoal)) return Optional.empty();
        // Access 'items' via AT
        Predicate<ItemStack> itemsPredicate = temptGoal.items;
        List<String> args = getArgs(itemsPredicate);
        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:vanilla_tempt"),
                new ItemStack(Items.WHEAT),
                "ethology.trait.goal.temptable",
                args,
                TraitType.GOAL
        ));
    };

    public static final GoalInspector BREED_INSPECTOR = goal -> {
        if (!(goal instanceof BreedGoal breedGoal)) return Optional.empty();
        // Access 'partnerClass' via AT
        Class<?> partnerClass = breedGoal.partnerClass;
        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:vanilla_breed"),
                new ItemStack(Items.HEART_OF_THE_SEA),
                "ethology.trait.goal.breed_goal",
                List.of(getEntityNameFromClass(partnerClass)),
                TraitType.GOAL
        ));
    };

    public static final GoalInspector AVOID_ENTITY_INSPECTOR = goal -> {
        if (!(goal instanceof AvoidEntityGoal<?> avoidGoal)) return Optional.empty();
        // Access 'avoidClass' via AT
        Class<?> avoidClass = avoidGoal.avoidClass;
        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:vanilla_avoid"),
                new ItemStack(Items.BARRIER),
                "ethology.trait.goal.fearful",
                List.of(getEntityNameFromClass(avoidClass)),
                TraitType.GOAL
        ));
    };

    // --- MOVEMENT ---

    public static final GoalInspector RANDOM_STROLL_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_stroll"),
            new ItemStack(Items.LEATHER_BOOTS),
            "ethology.trait.goal.wanders",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector WATER_AVOIDING_STROLL_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_stroll_land"),
            new ItemStack(Items.LEATHER_BOOTS),
            "ethology.trait.goal.wanders_land",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector FLOAT_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_float"),
            new ItemStack(Items.OAK_BOAT),
            "ethology.trait.goal.floats",
            List.of(),
            TraitType.GOAL
    ));

    // --- ACTION ---

    public static final GoalInspector EAT_BLOCK_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_eat_block"),
            new ItemStack(Items.GRASS_BLOCK),
            "ethology.trait.goal.eats_blocks",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector OPEN_DOOR_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_open_door"),
            new ItemStack(Items.OAK_DOOR),
            "ethology.trait.goal.opens_doors",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector BREAK_DOOR_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_break_door"),
            new ItemStack(Items.IRON_AXE),
            "ethology.trait.goal.breaks_doors",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector SIT_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_sit"),
            new ItemStack(Items.OAK_STAIRS),
            "ethology.trait.goal.sits",
            List.of(),
            TraitType.GOAL
    ));

    // --- COMBAT ---

    public static final GoalInspector MELEE_ATTACK_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_melee"),
            new ItemStack(Items.IRON_SWORD),
            "ethology.trait.goal.melee_attack",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector RANGED_BOW_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_bow"),
            new ItemStack(Items.BOW),
            "ethology.trait.goal.ranged_attack",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector SWELL_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_swell"),
            new ItemStack(Items.TNT),
            "ethology.trait.goal.explodes",
            List.of(),
            TraitType.GOAL
    ));

    // --- TARGETING ---

    public static final GoalInspector AGGRESSIVE_TARGET_INSPECTOR = goal -> {
        if (!(goal instanceof NearestAttackableTargetGoal<?> targetGoal)) return Optional.empty();
        // Access 'targetType' via AT
        Class<?> targetType = targetGoal.targetType;
        boolean isPlayer = Player.class.isAssignableFrom(targetType);
        ResourceLocation id = isPlayer ? ResourceLocation.parse("ethology:vanilla_aggro_player") : ResourceLocation.parse("ethology:vanilla_aggro_mob");
        ItemStack icon = isPlayer ? new ItemStack(Items.IRON_SWORD) : new ItemStack(Items.CROSSBOW);

        return Optional.of(new MobTrait(
                id,
                icon,
                "ethology.trait.goal.aggressive",
                List.of(getEntityNameFromClass(targetType)),
                TraitType.GOAL
        ));
    };

    public static final GoalInspector HURT_BY_TARGET_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_hurt_by"),
            new ItemStack(Items.SHIELD),
            "ethology.trait.goal.retaliates",
            List.of(),
            TraitType.GOAL
    ));

    public static final GoalInspector DEFEND_VILLAGE_INSPECTOR = goal -> Optional.of(new MobTrait(
            ResourceLocation.parse("ethology:vanilla_defend_village"),
            new ItemStack(Items.BELL),
            "ethology.trait.goal.defends_village",
            List.of(),
            TraitType.GOAL
    ));

    // --- UTILS ---

    private static @NotNull List<String> getArgs(Predicate<ItemStack> itemsPredicate) {
        List<String> args = new ArrayList<>();
        if (itemsPredicate instanceof Ingredient ingredient) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                args.add(stacks[0].getHoverName().getString());
            } else {
                args.add("Food");
            }
        } else {
            args.add("Items");
        }
        return args;
    }

    private static String getEntityNameFromClass(Class<?> clazz) {
        if (clazz == null) return "Unknown";
        if (Player.class.isAssignableFrom(clazz)) return "Players";
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(t -> clazz.isAssignableFrom(t.getBaseClass()))
                .findFirst();
        return type.map(entityType -> entityType.getDescription().getString())
                .orElseGet(() -> clazz.getSimpleName().replaceAll("Entity$", ""));
    }
}