package com.kjmaster.ethology.core;

import com.kjmaster.ethology.network.RequestScanPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class EthologyScanner {

    // Archetype Scan
    public static void scanEntity(EntityType<?> type) {
        // Client-Side Cache / Prediction
        // Check if we already have data for this entity type in the client-side database.
        // If present, we skip the network request to make the UI feel instant.
        // The EthologyDatabase is cleared on logout, ensuring this cache is transient.
        if (EthologyDatabase.get(type) != null) {
            return;
        }

        // Enforce Server-Side Analysis: Always request data from the server.
        // We no longer perform local analysis on the client thread to avoid incomplete data
        // (Client-side entities often lack full AI goals and Brain memories).
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new RequestScanPayload(BuiltInRegistries.ENTITY_TYPE.getKey(type), Optional.empty()));
        }
    }

    // Targeted Scan
    public static void scanTargetedEntity(LivingEntity target) {
        // Enforce Server-Side Analysis: Request specific instance data from the server.
        // Even for targeted entities, we trust the server's data (capabilities + state) over the client's.
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new RequestScanPayload(
                    BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()),
                    Optional.of(target.getUUID())
            ));
        }
    }
}