package com.kjmaster.ethology.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobTrait;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EthologyTraitManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Storage for our mappings
    private final Map<ResourceLocation, MobTrait> ACTIVITY_TRAITS = new HashMap<>();
    private final Map<ResourceLocation, MobTrait> SENSOR_TRAITS = new HashMap<>();
    private final Map<ResourceLocation, MobTrait> MEMORY_TRAITS = new HashMap<>();
    // Goal mapping is tricky because Goals are classes, not registry objects.
    // We will map Class Name Strings -> Trait.
    private final Map<String, MobTrait> GOAL_TRAITS = new HashMap<>();

    public EthologyTraitManager() {
        super(GSON, Ethology.MODID + "/ethology_traits");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        ACTIVITY_TRAITS.clear();
        SENSOR_TRAITS.clear();
        MEMORY_TRAITS.clear();
        GOAL_TRAITS.clear();

        object.forEach((location, jsonElement) -> {
            try {
                JsonObject json = jsonElement.getAsJsonObject();
                String type = json.get("type").getAsString(); // "activity", "sensor", "memory", or "goal"
                String target = json.get("target").getAsString(); // Registry ID or Class Name

                MobTrait trait = parseTrait(json);

                switch (type) {
                    case "activity" -> ACTIVITY_TRAITS.put(ResourceLocation.parse(target), trait);
                    case "sensor" -> SENSOR_TRAITS.put(ResourceLocation.parse(target), trait);
                    case "memory" -> MEMORY_TRAITS.put(ResourceLocation.parse(target), trait);
                    case "goal" -> GOAL_TRAITS.put(target, trait);
                    default -> Ethology.LOGGER.warn("Unknown trait type '{}' in {}", type, location);
                }

            } catch (Exception e) {
                Ethology.LOGGER.error("Failed to parse ethology trait: {}", location, e);
            }
        });

        Ethology.LOGGER.info("Loaded {} Ethology traits.", ACTIVITY_TRAITS.size() + SENSOR_TRAITS.size() + MEMORY_TRAITS.size() + GOAL_TRAITS.size());
    }

    private MobTrait parseTrait(JsonObject json) {
        // 1. Icon
        ResourceLocation itemLoc = ResourceLocation.parse(json.get("icon").getAsString());
        ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(itemLoc));
        if (icon.isEmpty()) icon = new ItemStack(Items.BARRIER);

        // 2. Title & Description (Support translation keys or raw string)
        Component title = Component.translatable(json.get("title").getAsString());
        Component description = Component.translatable(json.get("description").getAsString());

        return new MobTrait(icon, title, description);
    }

    // --- Public API for Parsers ---

    public Optional<MobTrait> getTrait(Activity activity) {
        return Optional.ofNullable(ACTIVITY_TRAITS.get(BuiltInRegistries.ACTIVITY.getKey(activity)));
    }

    public Optional<MobTrait> getTrait(SensorType<?> sensor) {
        return Optional.ofNullable(SENSOR_TRAITS.get(BuiltInRegistries.SENSOR_TYPE.getKey(sensor)));
    }

    public Optional<MobTrait> getTrait(MemoryModuleType<?> memory) {
        return Optional.ofNullable(MEMORY_TRAITS.get(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memory)));
    }

    /**
     * Finds a trait for a Goal class.
     * Checks the exact class name, then walks up the hierarchy to find mapped superclasses.
     */
    public Optional<MobTrait> getTrait(Class<?> goalClass) {
        Class<?> current = goalClass;
        while (current != Object.class && current != null) {
            String className = current.getName(); // e.g., "net.minecraft.world.entity.ai.goal.MeleeAttackGoal"
            if (GOAL_TRAITS.containsKey(className)) {
                return Optional.of(GOAL_TRAITS.get(className));
            }
            // Also check Simple Name for convenience if you prefer, but Full Name is safer for mods.
            current = current.getSuperclass();
        }
        return Optional.empty();
    }
}