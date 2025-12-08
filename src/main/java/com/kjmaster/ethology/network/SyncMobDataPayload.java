package com.kjmaster.ethology.network;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobScopedInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncMobDataPayload(MobScopedInfo info) implements CustomPacketPayload {
    public static final Type<SyncMobDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ethology.MODID, "sync_mob_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMobDataPayload> STREAM_CODEC = StreamCodec.composite(
            MobScopedInfo.STREAM_CODEC,
            SyncMobDataPayload::info,
            SyncMobDataPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}