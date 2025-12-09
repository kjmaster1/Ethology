package com.kjmaster.ethology.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record MobTrait(ResourceLocation id, ItemStack icon, String translationKey, TraitType type) {

    public static final StreamCodec<RegistryFriendlyByteBuf, MobTrait> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, MobTrait::id,
            ItemStack.STREAM_CODEC, MobTrait::icon,
            ByteBufCodecs.STRING_UTF8, MobTrait::translationKey,
            ByteBufCodecs.VAR_INT.map(i -> TraitType.values()[i], TraitType::ordinal), MobTrait::type,
            MobTrait::new
    );
}