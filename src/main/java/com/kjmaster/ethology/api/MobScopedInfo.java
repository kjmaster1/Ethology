package com.kjmaster.ethology.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobScopedInfo {

    private final ResourceLocation entityId;
    private final List<MobTrait> traits = new ArrayList<>();
    // Added UUID to track specific instances
    private UUID uuid;

    private double maxHealth;
    private double attackDamage;
    private double movementSpeed;
    private double armor;

    public static final StreamCodec<RegistryFriendlyByteBuf, MobScopedInfo> STREAM_CODEC = StreamCodec.of(
            MobScopedInfo::write,
            MobScopedInfo::read
    );

    public MobScopedInfo(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    public List<MobTrait> getTraits() { return traits; }
    public void addTrait(MobTrait mobTrait) { traits.add(mobTrait); }
    public ResourceLocation getEntityId() { return entityId; }

    // UUID Getters/Setters
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
    public double getAttackDamage() { return attackDamage; }
    public void setAttackDamage(double attackDamage) { this.attackDamage = attackDamage; }
    public double getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(double movementSpeed) { this.movementSpeed = movementSpeed; }
    public double getArmor() { return armor; }
    public void setArmor(double armor) { this.armor = armor; }

    public static void write(RegistryFriendlyByteBuf buffer, MobScopedInfo info) {
        buffer.writeResourceLocation(info.entityId);

        // Write UUID (optional)
        buffer.writeBoolean(info.uuid != null);
        if (info.uuid != null) {
            buffer.writeUUID(info.uuid);
        }

        buffer.writeDouble(info.maxHealth);
        buffer.writeDouble(info.attackDamage);
        buffer.writeDouble(info.movementSpeed);
        buffer.writeDouble(info.armor);

        buffer.writeInt(info.traits.size());
        for (MobTrait trait : info.traits) {
            MobTrait.STREAM_CODEC.encode(buffer, trait);
        }
    }

    public static MobScopedInfo read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        MobScopedInfo info = new MobScopedInfo(id);

        // Read UUID
        if (buffer.readBoolean()) {
            info.setUuid(buffer.readUUID());
        }

        info.setMaxHealth(buffer.readDouble());
        info.setAttackDamage(buffer.readDouble());
        info.setMovementSpeed(buffer.readDouble());
        info.setArmor(buffer.readDouble());

        int traitCount = buffer.readInt();
        for (int i = 0; i < traitCount; i++) {
            info.addTrait(MobTrait.STREAM_CODEC.decode(buffer));
        }

        return info;
    }
}