package com.kjmaster.ethology.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.MobTrait;
import com.kjmaster.ethology.api.TraitType;
import net.minecraft.core.registries.BuiltInRegistries;
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

    private final Map<ResourceLocation, MobTrait> ACTIVITY_TRAITS = new HashMap<>();
    private final Map<ResourceLocation, MobTrait> SENSOR_TRAITS = new HashMap<>();
    private final Map<ResourceLocation, MobTrait> MEMORY_TRAITS = new HashMap<>();
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
                String typeStr = json.get("type").getAsString();
                String target = json.get("target").getAsString();

                // Parse Type
                TraitType type = switch (typeStr.toLowerCase()) {
                    case "activity" -> TraitType.ACTIVITY;
                    case "sensor" -> TraitType.SENSOR;
                    case "memory" -> TraitType.MEMORY;
                    case "goal" -> TraitType.GOAL;
                    default -> throw new IllegalArgumentException("Unknown type: " + typeStr);
                };

                MobTrait trait = parseTrait(location, json, type);

                switch (type) {
                    case ACTIVITY -> ACTIVITY_TRAITS.put(ResourceLocation.parse(target), trait);
                    case SENSOR -> SENSOR_TRAITS.put(ResourceLocation.parse(target), trait);
                    case MEMORY -> MEMORY_TRAITS.put(ResourceLocation.parse(target), trait);
                    case GOAL -> GOAL_TRAITS.put(target, trait);
                    default -> Ethology.LOGGER.warn("Unhandled trait type '{}' in {}", type, location);
                }

            } catch (Exception e) {
                Ethology.LOGGER.error("Failed to parse ethology trait: {}", location, e);
            }
        });

        Ethology.LOGGER.info("Loaded {} Ethology traits.", ACTIVITY_TRAITS.size() + SENSOR_TRAITS.size() + MEMORY_TRAITS.size() + GOAL_TRAITS.size());
    }

    private MobTrait parseTrait(ResourceLocation location, JsonObject json, TraitType type) {
        ResourceLocation itemLoc = ResourceLocation.parse(json.get("icon").getAsString());
        ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(itemLoc));
        if (icon.isEmpty()) icon = new ItemStack(Items.BARRIER);

        // Read translation key; fallback to generated if missing (though data gen should provide it)
        String translationKey;
        if (json.has("translation_key")) {
            translationKey = json.get("translation_key").getAsString();
        } else {
            // Fallback: ethology.trait.[type].[path]
            translationKey = "ethology.trait." + type.name().toLowerCase() + "." + location.getPath();
        }

        return new MobTrait(location, icon, translationKey, type);
    }

    // --- Public API ---

    public Optional<MobTrait> getTrait(Activity activity) {
        return Optional.ofNullable(ACTIVITY_TRAITS.get(BuiltInRegistries.ACTIVITY.getKey(activity)));
    }

    public Optional<MobTrait> getTrait(SensorType<?> sensor) {
        return Optional.ofNullable(SENSOR_TRAITS.get(BuiltInRegistries.SENSOR_TYPE.getKey(sensor)));
    }

    public Optional<MobTrait> getTrait(MemoryModuleType<?> memory) {
        return Optional.ofNullable(MEMORY_TRAITS.get(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memory)));
    }

    public Optional<MobTrait> getTrait(Class<?> goalClass) {
        Class<?> current = goalClass;
        while (current != Object.class && current != null) {
            String className = current.getName();
            if (GOAL_TRAITS.containsKey(className)) {
                return Optional.of(GOAL_TRAITS.get(className));
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }
}