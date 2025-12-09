package com.kjmaster.ethology;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class EthologyTags {
    public static final TagKey<EntityType<?>> NO_ANALYSIS = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Ethology.MODID, "no_analysis")
    );
}