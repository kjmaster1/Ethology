package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Set;
import java.util.function.Predicate;

public class GoalParser {

    public static void parse(LivingEntity entity, MobScopedInfo info) {
        if (!(entity instanceof Mob mob)) return;

        // 1. Analyze Main Goals
        // AT Access: public net.minecraft.world.entity.Mob goalSelector
        // AT Access: public net.minecraft.world.entity.ai.goal.GoalSelector availableGoals
        Set<WrappedGoal> goals = mob.goalSelector.availableGoals;

        boolean hasWander = false;

        for (WrappedGoal wrapped : goals) {
            Goal innerGoal = wrapped.getGoal();

            // Check specific types first
            switch (innerGoal) {
                case TemptGoal temptGoal -> parseTempt(temptGoal, info);
                case AvoidEntityGoal avoidEntityGoal -> parseAvoid(avoidEntityGoal, info);
                case BreedGoal ignored8 -> parseBreed(info);
                case PanicGoal ignored7 -> parsePanic(info);
                case FloatGoal ignored6 -> parseFloat(info);
                case OpenDoorGoal ignored5 -> parseDoor(info);
                case BreakDoorGoal ignored4 -> parseBreakDoor(info);
                case EatBlockGoal ignored3 -> parseEatBlock(info);
                case SwellGoal ignored2 -> parseExplode(info); // Creepers
                case MeleeAttackGoal ignored1 -> parseMelee(info); // Zombies, Spiders
                case RangedBowAttackGoal ignored -> parseRanged(info); // Skeletons
                default -> {
                }
            }

            // Track generic wandering to ensure passive mobs get at least one trait
            if (innerGoal instanceof RandomStrollGoal || innerGoal instanceof WaterAvoidingRandomStrollGoal) {
                hasWander = true;
            }

            if (innerGoal instanceof RandomSwimmingGoal) {
                info.addTrait(new MobTrait(new ItemStack(Items.TROPICAL_FISH), Component.literal("Swimmer"), Component.literal("Swims randomly in water.")));
                hasWander = true;
            }
            else if (innerGoal.getClass().getSimpleName().contains("Fly")) {
                // Catch generic flying goals (Phantoms, Bats, Bees, Ghasts)
                info.addTrait(new MobTrait(new ItemStack(Items.FEATHER), Component.literal("Flyer"), Component.literal("Can fly through the air.")));
                hasWander = true;
            }
            else if (innerGoal instanceof BreathAirGoal) {
                // Dolphins, Axolotls
                info.addTrait(new MobTrait(new ItemStack(Items.BUBBLE_CORAL), Component.literal("Amphibious"), Component.literal("Needs air to breathe.")));
            }
        }

        // Add a generic "Wanderer" trait if they have no other interesting traits but do move around
        if (info.getTraits().isEmpty() && hasWander) {
            info.addTrait(new MobTrait(new ItemStack(Items.LEATHER_BOOTS), Component.literal("Wanderer"), Component.literal("Roams the world aimlessly.")));
        }

        // 2. Analyze Targets
        // AT Access: public net.minecraft.world.entity.Mob targetSelector
        Set<WrappedGoal> targets = mob.targetSelector.availableGoals;
        for (WrappedGoal wrapped : targets) {
            Goal innerGoal = wrapped.getGoal();
            if (innerGoal instanceof NearestAttackableTargetGoal) {
                parseTarget((NearestAttackableTargetGoal<?>) innerGoal, info);
            }
        }
    }

    // --- Parsing Logic Methods ---

    private static void parseTempt(TemptGoal goal, MobScopedInfo info) {
        // AT Access: public net.minecraft.world.entity.ai.goal.TemptGoal items
        Predicate<ItemStack> predicate = goal.items;

        if (predicate instanceof Ingredient ingredient && ingredient.getItems().length > 0) {
            info.addTrait(new MobTrait(ingredient.getItems()[0], Component.literal("Temptable"), Component.literal("Follows players holding this.")));
        } else {
            // Fallback for Mobs that use Lambdas (Cows, Sheep, etc.)
            info.addTrait(new MobTrait(new ItemStack(Items.WHEAT), Component.literal("Temptable"), Component.literal("Follows players holding food.")));
        }
    }

    private static void parseAvoid(AvoidEntityGoal<?> goal, MobScopedInfo info) {
        // AT Access: public net.minecraft.world.entity.ai.goal.AvoidEntityGoal avoidClass
        Class<?> scaredOf = goal.avoidClass;
        info.addTrait(new MobTrait(new ItemStack(Items.BARRIER), Component.literal("Fearful"), Component.literal("Flees from " + scaredOf.getSimpleName())));
    }

    private static void parseBreed(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.EGG), Component.literal("Breedable"), Component.literal("Can breed to produce offspring.")));
    }

    private static void parsePanic(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.FEATHER), Component.literal("Skittish"), Component.literal("Runs away when hurt.")));
    }

    private static void parseFloat(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.LILY_PAD), Component.literal("Buoyant"), Component.literal("Floats on water.")));
    }

    private static void parseDoor(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.OAK_DOOR), Component.literal("Smart"), Component.literal("Can open and close doors.")));
    }

    private static void parseBreakDoor(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.IRON_AXE), Component.literal("Breaker"), Component.literal("Breaks down wooden doors.")));
    }

    private static void parseEatBlock(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.GRASS_BLOCK), Component.literal("Grazer"), Component.literal("Eats grass blocks to regain health/wool.")));
    }

    private static void parseExplode(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.TNT), Component.literal("Explosive"), Component.literal("Self-destructs when close to target.")));
    }

    private static void parseMelee(MobScopedInfo info) {
        // Check if we already added a "Hostile" trait to avoid duplicates
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals("Melee"))) {
            info.addTrait(new MobTrait(new ItemStack(Items.IRON_SWORD), Component.literal("Melee"), Component.literal("Attacks targets in close range.")));
        }
    }

    private static void parseRanged(MobScopedInfo info) {
        info.addTrait(new MobTrait(new ItemStack(Items.BOW), Component.literal("Ranged"), Component.literal("Shoots projectiles at targets.")));
    }

    private static void parseTarget(NearestAttackableTargetGoal<?> goal, MobScopedInfo info) {
        // AT Access: public net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal targetType
        Class<?> targetClass = goal.targetType;
        String name = targetClass.getSimpleName();

        // Filter common "Player" target to just be "Hostile"
        if (name.equals("Player") || name.equals("ServerPlayer")) {
            if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals("Hostile"))) {
                info.addTrait(new MobTrait(new ItemStack(Items.RED_DYE), Component.literal("Hostile"), Component.literal("Aggressive towards players.")));
            }
        } else {
            info.addTrait(new MobTrait(new ItemStack(Items.CROSSBOW), Component.literal("Hunter"), Component.literal("Hunts " + name + "s.")));
        }
    }
}