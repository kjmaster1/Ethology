package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EcologicalClassifier {

    public static void classify(LivingEntity entity, MobScopedInfo info) {
        analyzeEnvironment(entity, info);
        analyzeHostility(entity, info);
    }

    private static void analyzeEnvironment(LivingEntity entity, MobScopedInfo info) {
        // 1. Aquatic Analysis

        if (entity instanceof PathfinderMob pathfinderMob) {
            boolean isAquaticNavigation = pathfinderMob.getNavigation() instanceof WaterBoundPathNavigation
                    || pathfinderMob.getNavigation() instanceof AmphibiousPathNavigation;

            if (isAquaticNavigation) {
                info.addCapability(createTrait("aquatic", Items.WATER_BUCKET));
            }
        }

        // 2. Aerial Analysis
        if (entity instanceof FlyingAnimal) {
            info.addCapability(createTrait("aerial", Items.FEATHER));
        }
    }

    private static void analyzeHostility(LivingEntity entity, MobScopedInfo info) {
        if (!entity.getType().canSummon()) return;

        // Inspect TargetSelector to determine aggression level
        // We look for specific goal classes that define behavior
        boolean hasPlayerAggro = false;
        boolean hasRevenge = false;

        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
                Goal goal = wrapped.getGoal();
                if (goal instanceof NearestAttackableTargetGoal<?> targetGoal) {
                    if (targetGoal.targetType == Player.class) {
                        hasPlayerAggro = true;
                    }
                }
                if (goal instanceof HurtByTargetGoal) {
                    hasRevenge = true;
                }
            }
        }

        if (hasPlayerAggro) {
            info.addCapability(createTrait("hostile", Items.IRON_SWORD));
        } else if (hasRevenge) {
            info.addCapability(createTrait("neutral", Items.SHIELD));
        } else {
            info.addCapability(createTrait("passive", Items.GRASS_BLOCK));
        }
    }

    private static MobTrait createTrait(String keySuffix, net.minecraft.world.item.Item iconItem) {
        return new MobTrait(
                ResourceLocation.parse("ethology:classifier_" + keySuffix),
                new ItemStack(iconItem),
                "ethology.trait.stat." + keySuffix, // e.g. ethology.trait.stat.aquatic
                TraitType.STAT
        );
    }
}