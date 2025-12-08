package com.kjmaster.ethology.network;

import com.kjmaster.ethology.Ethology;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RequestScanPayload(ResourceLocation entityId) implements CustomPacketPayload {
    public static final Type<RequestScanPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ethology.MODID, "request_scan"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestScanPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RequestScanPayload::entityId,
            RequestScanPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}