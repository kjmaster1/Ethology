package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MemoryInspector;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.SensorInspector;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VanillaBrainInspectors {

    // --- SENSORS ---

    public static final SensorInspector HOGLIN_SENSOR = (type, sensor) -> {
        // Hoglin sensor detects piglins and repellent
        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:sensor_hoglin"),
                new ItemStack(Items.WARPED_FUNGUS),
                "ethology.trait.sensor.hoglin_sensing",
                List.of(),
                TraitType.SENSOR
        ));
    };

    public static final SensorInspector WARDEN_SENSOR = (type, sensor) -> {
        // Warden sensor detects vibrations
        return Optional.of(new MobTrait(
                ResourceLocation.parse("ethology:sensor_warden"),
                new ItemStack(Items.SCULK_SENSOR),
                "ethology.trait.sensor.warden_sensing",
                List.of(),
                TraitType.SENSOR
        ));
    };

    // --- MEMORIES ---

    public static final MemoryInspector HOME_INSPECTOR = (type, value) -> {
        if (type != MemoryModuleType.HOME) return Optional.empty();

        if (value.isPresent()) {
            GlobalPos pos = (GlobalPos) value.get();
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_home"),
                    new ItemStack(Items.RED_BED),
                    "ethology.trait.memory.home_set",
                    List.of(pos.pos().toShortString()),
                    TraitType.MEMORY
            ));
        } else {
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_home_capability"),
                    new ItemStack(Items.RED_BED),
                    "ethology.trait.memory.can_have_home",
                    List.of(),
                    TraitType.MEMORY
            ));
        }
    };

    public static final MemoryInspector JOB_SITE_INSPECTOR = (type, value) -> {
        if (type != MemoryModuleType.JOB_SITE) return Optional.empty();

        if (value.isPresent()) {
            GlobalPos pos = (GlobalPos) value.get();
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_job"),
                    new ItemStack(Items.EMERALD),
                    "ethology.trait.memory.has_job",
                    List.of(pos.pos().toShortString()),
                    TraitType.MEMORY
            ));
        } else {
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_job_capability"),
                    new ItemStack(Items.EMERALD),
                    "ethology.trait.memory.can_have_job",
                    List.of(),
                    TraitType.MEMORY
            ));
        }
    };

    public static final MemoryInspector MEETING_POINT_INSPECTOR = (type, value) -> {
        if (type != MemoryModuleType.MEETING_POINT) return Optional.empty();

        if (value.isPresent()) {
            GlobalPos pos = (GlobalPos) value.get();
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_meeting"),
                    new ItemStack(Items.BELL),
                    "ethology.trait.memory.meeting_point",
                    List.of(pos.pos().toShortString()),
                    TraitType.MEMORY
            ));
        } else {
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_meeting_capability"),
                    new ItemStack(Items.BELL),
                    "ethology.trait.memory.can_meet",
                    List.of(),
                    TraitType.MEMORY
            ));
        }
    };

    public static final MemoryInspector ANGRY_AT_INSPECTOR = (type, value) -> {
        if (type != MemoryModuleType.ANGRY_AT) return Optional.empty();

        if (value.isPresent()) {
            UUID target = (UUID) value.get();
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_angry"),
                    new ItemStack(Items.TNT),
                    "ethology.trait.memory.angry_at",
                    List.of(target.toString().substring(0, 8)), // Shorten UUID for display
                    TraitType.MEMORY
            ));
        } else {
            return Optional.of(new MobTrait(
                    ResourceLocation.parse("ethology:memory_angry_capability"),
                    new ItemStack(Items.TNT),
                    "ethology.trait.memory.can_get_angry",
                    List.of(),
                    TraitType.MEMORY
            ));
        }
    };
}