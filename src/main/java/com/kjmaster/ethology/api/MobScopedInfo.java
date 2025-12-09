package com.kjmaster.ethology.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobScopedInfo {

    private final ResourceLocation entityId;

    // Split traits into capabilities and states
    private final List<MobTrait> capabilities = new ArrayList<>();
    private final List<MobTrait> currentStates = new ArrayList<>();

    private UUID uuid;

    private double maxHealth;
    private double attackDamage;
    private double movementSpeed;
    private double armor;

    // StreamCodec definition for networking
    public static final StreamCodec<RegistryFriendlyByteBuf, MobScopedInfo> STREAM_CODEC = StreamCodec.of(
            MobScopedInfo::write,
            MobScopedInfo::read
    );

    public MobScopedInfo(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    public ResourceLocation getEntityId() { return entityId; }

    // --- Capabilities & States ---
    public List<MobTrait> getCapabilities() { return capabilities; }
    public void addCapability(MobTrait trait) {
        if (!hasCapability(trait)) this.capabilities.add(trait);
    }

    public List<MobTrait> getCurrentStates() { return currentStates; }
    public void addCurrentState(MobTrait trait) {
        if (!hasState(trait)) this.currentStates.add(trait);
    }

    private boolean hasCapability(MobTrait trait) {
        return capabilities.stream().anyMatch(t -> t.id().equals(trait.id()));
    }

    private boolean hasState(MobTrait trait) {
        return currentStates.stream().anyMatch(t -> t.id().equals(trait.id()));
    }

    // --- Stats & UUID ---
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

    // --- Networking Logic ---

    public static void write(RegistryFriendlyByteBuf buffer, MobScopedInfo info) {
        buffer.writeResourceLocation(info.entityId);

        // UUID (Optional)
        buffer.writeBoolean(info.uuid != null);
        if (info.uuid != null) {
            buffer.writeUUID(info.uuid);
        }

        buffer.writeDouble(info.maxHealth);
        buffer.writeDouble(info.attackDamage);
        buffer.writeDouble(info.movementSpeed);
        buffer.writeDouble(info.armor);

        // Write Capabilities List using the MobTrait codec
        MobTrait.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, info.capabilities);

        // Write States List
        MobTrait.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, info.currentStates);
    }

    public static MobScopedInfo read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        MobScopedInfo info = new MobScopedInfo(id);

        if (buffer.readBoolean()) {
            info.setUuid(buffer.readUUID());
        }

        info.setMaxHealth(buffer.readDouble());
        info.setAttackDamage(buffer.readDouble());
        info.setMovementSpeed(buffer.readDouble());
        info.setArmor(buffer.readDouble());

        // Read Capabilities
        List<MobTrait> caps = MobTrait.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        caps.forEach(info::addCapability);

        // Read States
        List<MobTrait> states = MobTrait.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        states.forEach(info::addCurrentState);

        return info;
    }
}