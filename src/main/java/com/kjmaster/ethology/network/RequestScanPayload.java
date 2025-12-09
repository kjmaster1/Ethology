package com.kjmaster.ethology.network;

import com.kjmaster.ethology.Ethology;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record RequestScanPayload(ResourceLocation typeId, Optional<UUID> instanceId) implements CustomPacketPayload {
    public static final Type<RequestScanPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ethology.MODID, "request_scan"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestScanPayload> STREAM_CODEC = StreamCodec.ofMember(
            RequestScanPayload::write,
            RequestScanPayload::read
    );

    private static void write(RequestScanPayload payload, RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(payload.typeId);
        buffer.writeBoolean(payload.instanceId.isPresent());
        payload.instanceId.ifPresent(buffer::writeUUID);
    }

    private static RequestScanPayload read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation typeId = buffer.readResourceLocation();
        boolean hasInstance = buffer.readBoolean();
        UUID uuid = hasInstance ? buffer.readUUID() : null;
        return new RequestScanPayload(typeId, Optional.ofNullable(uuid));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}