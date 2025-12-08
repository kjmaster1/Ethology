package com.kjmaster.ethology.api;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MobScopedInfo {
    private final ResourceLocation entityId;
    private final List<MobTrait> traits = new ArrayList<>();
    private double maxHealth;
    private double attackDamage;
    private double movementSpeed;
    private double armor;

    public MobScopedInfo(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    public void addTrait(MobTrait trait) {
        this.traits.add(trait);
    }

    // Getters and Setters for stats...
    public List<MobTrait> getTraits() {
        return traits;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public ResourceLocation getEntityId() {
        return entityId;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public double getArmor() {
        return armor;
    }

    public void setArmor(double armor) {
        this.armor = armor;
    }
}