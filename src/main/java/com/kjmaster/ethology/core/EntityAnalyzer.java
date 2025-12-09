package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.EthologyTags;
import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityAnalyzer {

    public static MobScopedInfo analyze(EntityType<?> type, Level level) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (type.is(EthologyTags.NO_ANALYSIS)) return null;

        if (!type.canSummon()) return null;

        try {
            Entity entity = type.create(level);
            if (!(entity instanceof LivingEntity living)) {
                if (entity != null) entity.discard();
                return null;
            }
            MobScopedInfo info = analyze(living);
            living.discard();
            return info;
        } catch (Exception e) {
            Ethology.LOGGER.warn("Failed to instantiate entity archetype for analysis: {}", key, e);
            return null;
        }
    }

    /**
     * Instance Analysis: Analyzes a specific running entity instance.
     */
    public static MobScopedInfo analyze(LivingEntity living) {
        EntityType<?> type = living.getType();
        MobScopedInfo info = new MobScopedInfo(BuiltInRegistries.ENTITY_TYPE.getKey(type));

        // Set UUID for caching
        info.setUuid(living.getUUID());

        // 1. Stats (Current values, not just base)
        extractStats(living, info);

        // 2. Logic Analysis
        boolean hasComplexBrain = !living.getBrain().memories.isEmpty();

        if (hasComplexBrain) {
            BrainParser.parse(living, info);
        }

        GoalParser.parse(living, info);

        // 3. Fallback Classification
        applyFallbacks(living, info);

        return info;
    }

    private static void extractStats(LivingEntity entity, MobScopedInfo info) {
        if (entity.getAttributes().hasAttribute(Attributes.MAX_HEALTH))
            info.setMaxHealth(entity.getAttributeValue(Attributes.MAX_HEALTH));
        if (entity.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
            info.setAttackDamage(entity.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (entity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED))
            info.setMovementSpeed(entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
        if (entity.getAttributes().hasAttribute(Attributes.ARMOR))
            info.setArmor(entity.getAttributeValue(Attributes.ARMOR));
    }

    private static void applyFallbacks(LivingEntity entity, MobScopedInfo info) {
        if ((entity instanceof WaterAnimal || entity.canBreatheUnderwater()) && !(entity instanceof ArmorStand)) {
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.WATER_BUCKET), Component.literal("Aquatic"), Component.literal("Lives and swims in water.")));
        }
        if (entity instanceof FlyingAnimal) {
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.FEATHER), Component.literal("Aerial"), Component.literal("Capable of flight.")));
        }
        if (entity instanceof Enemy && info.getTraits().stream().noneMatch(t -> t.title().getString().equals("Hostile"))) {
            info.addTrait(new MobTrait(new ItemStack(Items.IRON_SWORD), Component.literal("Hostile"), Component.literal("Naturally aggressive monster.")));
        }
        if ((entity instanceof Animal || entity instanceof AmbientCreature) && !(entity instanceof Enemy)) {
            if (info.getTraits().isEmpty()) {
                info.addTrait(new MobTrait(new ItemStack(Items.GRASS_BLOCK), Component.literal("Passive"), Component.literal("Harmless ambient creature.")));
            }
        }
    }

    private static void addUniqueTrait(MobScopedInfo info, MobTrait trait) {
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals(trait.title().getString()))) {
            info.addTrait(trait);
        }
    }
}