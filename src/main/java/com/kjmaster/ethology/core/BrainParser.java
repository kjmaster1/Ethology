package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class BrainParser {

    public static void parse(LivingEntity entity, MobScopedInfo info) {
        Brain<?> brain = entity.getBrain();

        parseSensors(brain, info);
        parseMemories(brain, info);
        parseActivities(brain, info);
    }

    private static void parseActivities(Brain<?> brain, MobScopedInfo info) {
        for (Map<Activity, ?> innerMap : brain.availableBehaviorsByPriority.values()) {
            for (Activity activity : innerMap.keySet()) {
                String name = activity.toString();
                if (name.contains("fight")) {
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.IRON_SWORD), Component.literal("Hostile"), Component.literal("Engages in combat.")));
                } else if (name.contains("avoid")) {
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.FEATHER), Component.literal("Skittish"), Component.literal("Flees from threats.")));
                } else if (name.contains("play_dead")) {
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.TROPICAL_FISH_BUCKET), Component.literal("Resilient"), Component.literal("Plays dead to regenerate health.")));
                } else if (name.contains("roar") || name.contains("sonic_boom")) {
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.SCULK_SENSOR), Component.literal("Sonic"), Component.literal("Uses sound waves to attack.")));
                } else if (name.contains("dig")) {
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.BRUSH), Component.literal("Digger"), Component.literal("Digs for items or emerges from ground.")));
                } else if (name.contains("emerge")) {
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.DIRT), Component.literal("Burrower"), Component.literal("Can emerge from the ground.")));
                } else if (name.contains("tongue")) {
                    // Frog
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.SLIME_BALL), Component.literal("Hunter"), Component.literal("Uses tongue to eat small mobs.")));
                } else if (name.contains("ram")) {
                    // Goat
                    addUniqueTrait(info, new MobTrait(new ItemStack(Items.GOAT_HORN), Component.literal("Rammer"), Component.literal("Rams targets aggressively.")));
                }
            }
        }
    }

    private static void parseSensors(Brain<?> brain, MobScopedInfo info) {
        for (SensorType<? extends Sensor<?>> sensorType : brain.sensors.keySet()) {
            String registryName = sensorType.toString();

            if (registryName.contains("nearest_items")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.SPYGLASS), Component.literal("Scavenger"), Component.literal("Searches for items.")));
            } else if (registryName.contains("piglin_specific_sensor")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.GOLD_INGOT), Component.literal("Gold Lover"), Component.literal("Barters for gold.")));
            } else if (registryName.contains("hoglin_specific_sensor")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.CRIMSON_FUNGUS), Component.literal("Territorial"), Component.literal("Defends territory aggressively.")));
            } else if (registryName.contains("warden_entity_sensor")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.SCULK), Component.literal("Blind"), Component.literal("Detects vibrations and smell.")));
            } else if (registryName.contains("breeze_attack_entity_sensor")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.WIND_CHARGE), Component.literal("Windy"), Component.literal("Attacks with bursts of wind.")));
            } else if (registryName.contains("camel_temptations")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.CACTUS), Component.literal("Temptable"), Component.literal("Follows players holding Cactus.")));
            } else if (registryName.contains("frog_attackables")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.MAGMA_CREAM), Component.literal("Glutton"), Component.literal("Eats Magma Cubes and Slimes.")));
            } else if (registryName.contains("armadillo_scare_sensor")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.ARMADILLO_SCUTE), Component.literal("Defensive"), Component.literal("Rolls up when threatened.")));
            }
        }
    }

    private static void parseMemories(Brain<?> brain, MobScopedInfo info) {
        Map<MemoryModuleType<?>, ?> memories = brain.memories;
        for (MemoryModuleType<?> type : memories.keySet()) {
            String key = type.toString();
            if (key.contains("home")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.RED_BED), Component.literal("Homemaker"), Component.literal("Claims a home location.")));
            } else if (key.contains("job_site")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.CRAFTING_TABLE), Component.literal("Employed"), Component.literal("Can take a profession.")));
            } else if (key.contains("liked_player")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.COOKIE), Component.literal("Friendly"), Component.literal("Can befriend players.")));
            } else if (key.contains("sniff_cooldown")) {
                addUniqueTrait(info, new MobTrait(new ItemStack(Items.PITCHER_POD), Component.literal("Forager"), Component.literal("Sniffs out ancient seeds.")));
            }
        }
    }

    private static void addUniqueTrait(MobScopedInfo info, MobTrait trait) {
        if (info.getTraits().stream().noneMatch(t -> t.title().getString().equals(trait.title().getString()))) {
            info.addTrait(trait);
        }
    }
}