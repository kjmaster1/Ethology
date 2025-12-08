package com.kjmaster.ethology.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record MobTrait(ItemStack icon, Component title, Component description) {}
