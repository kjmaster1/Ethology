package com.kjmaster.ethology.core;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.EthologyTags;
import com.kjmaster.ethology.api.MobScopedInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class EntityAnalyzer {

    /**
     * Archetype Analysis: Static capabilities only.
     */
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

            MobScopedInfo info = new MobScopedInfo(key);

            // 1. Stats (Base)
            extractStats(living, info);

            // 2. Capabilities
            BrainParser.parseCapabilities(living, info);
            GoalParser.parseCapabilities(living, info);
            EcologicalClassifier.classify(living, info);

            living.discard();
            return info;
        } catch (Exception e) {
            Ethology.LOGGER.warn("Failed to instantiate entity archetype for analysis: {}", key, e);
            return null;
        }
    }

    /**
     * Instance Analysis: Capabilities + Current State.
     */
    public static MobScopedInfo analyze(LivingEntity living) {
        MobScopedInfo info = new MobScopedInfo(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()));
        info.setUuid(living.getUUID());

        // 1. Stats (Current)
        extractStats(living, info);

        // 2. Capabilities
        BrainParser.parseCapabilities(living, info);
        GoalParser.parseCapabilities(living, info);
        EcologicalClassifier.classify(living, info);

        // 3. Current State
        BrainParser.parseCurrentState(living, info);
        GoalParser.parseCurrentState(living, info);

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
}