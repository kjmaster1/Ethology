package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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
        Entity entity = type.create(level);
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }

        MobScopedInfo info = new MobScopedInfo(BuiltInRegistries.ENTITY_TYPE.getKey(type));

        // 1. Stats
        extractStats(living, info);

        // 2. Logic Analysis
        boolean hasComplexBrain = !living.getBrain().memories.isEmpty();

        if (hasComplexBrain) {
            BrainParser.parse(living, info);
        }

        GoalParser.parse(living, info);

        // 3. Fallback Classification
        // If specific AI analysis didn't find enough info, fallback to class hierarchy.
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
        // If we already have a specific Hostile/Passive trait, we might skip generic ones,
        // but adding "Aquatic" or "Flying" is always useful.

        // 1. Aquatic
        if ((entity instanceof WaterAnimal || entity.canBreatheUnderwater()) && !(entity instanceof ArmorStand)) {
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.WATER_BUCKET), Component.literal("Aquatic"), Component.literal("Lives and swims in water.")));
        }

        // 2. Flying
        if (entity instanceof FlyingAnimal) {
            addUniqueTrait(info, new MobTrait(new ItemStack(Items.FEATHER), Component.literal("Aerial"), Component.literal("Capable of flight.")));
        }

        // 3. Hostility (Fallback)
        // If we haven't detected specific attack goals but it implements Enemy (Monster)
        if (entity instanceof Enemy && info.getTraits().stream().noneMatch(t -> t.title().getString().equals("Hostile"))) {
            info.addTrait(new MobTrait(new ItemStack(Items.IRON_SWORD), Component.literal("Hostile"), Component.literal("Naturally aggressive monster.")));
        }

        // 4. Passive (Fallback)
        // If it's an Animal/Ambient and NOT an Enemy, and we have no other traits
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