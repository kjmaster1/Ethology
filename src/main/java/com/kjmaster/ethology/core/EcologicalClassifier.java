package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

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

        int score = 0;

        // 1. Base Category Score
        // Monsters start with a bias towards hostility
        if (entity.getType().getCategory() == MobCategory.MONSTER) {
            score += 50;
        }

        // 2. Attribute Score
        // If it can deal damage, it's at least potentially neutral, rarely purely passive.
        if (entity.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            double damage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (damage > 0) {
                score += 20;
            }
        }

        // 3. Goal Analysis Score
        if (entity instanceof Mob mob) {
            for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
                Goal goal = wrapped.getGoal();

                // Aggressive Traits
                if (goal instanceof NearestAttackableTargetGoal<?> targetGoal) {
                    // Direct Player aggression immediately pushes score high
                    if (targetGoal.targetType == Player.class) {
                        score += 100;
                    } else {
                        // Targets other things (e.g. Spiders targeting Iron Golems)
                        score += 10;
                    }
                }

                // Defensive/Neutral Traits
                if (goal instanceof HurtByTargetGoal) {
                    score += 30; // Push towards Neutral
                }

                if (goal instanceof DefendVillageTargetGoal) {
                    score += 10;
                }
            }
        }

        // 4. Threshold Classification
        // >= 80 : Hostile
        // 20-79 : Neutral
        // < 20  : Passive

        if (score >= 80) {
            info.addCapability(createTrait("hostile", Items.IRON_SWORD));
        } else if (score >= 20) {
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
                new ArrayList<>(), // No arguments for stats
                TraitType.STAT
        );
    }
}