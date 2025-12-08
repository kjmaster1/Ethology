package com.kjmaster.ethology.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MobTrait(ItemStack icon, Component title, Component description) {

    public static final StreamCodec<RegistryFriendlyByteBuf, MobTrait> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, MobTrait::icon,
            ComponentSerialization.STREAM_CODEC, MobTrait::title,
            ComponentSerialization.STREAM_CODEC, MobTrait::description,
            MobTrait::new
    );
}
