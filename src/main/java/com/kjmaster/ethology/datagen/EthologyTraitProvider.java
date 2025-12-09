package com.kjmaster.ethology.datagen;

import com.google.gson.JsonObject;
import com.kjmaster.ethology.Ethology;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EthologyTraitProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final Map<ResourceLocation, JsonObject> toSerialize = new HashMap<>();

    public EthologyTraitProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        toSerialize.clear();
        addTraits();

        // Save all generated JSONs
        return CompletableFuture.allOf(toSerialize.entrySet().stream().map(entry -> {
            Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                    .resolve(Ethology.MODID)
                    .resolve(Ethology.MODID + "/ethology_traits")
                    .resolve(entry.getKey().getPath() + ".json");
            return DataProvider.saveStable(cache, entry.getValue(), path);
        }).toArray(CompletableFuture[]::new));
    }

    private void addTraits() {
        // --- Activities ---
        activity(Activity.CORE, Items.HEART_OF_THE_SEA, "Core", "Essential brain function.");
        activity(Activity.IDLE, Items.CLOCK, "Idle", "Waiting for something to happen.");
        activity(Activity.WORK, Items.CRAFTING_TABLE, "Working", "Performing a profession task.");
        activity(Activity.PLAY, Items.POPPY, "Playing", "Engaging in play with others.");
        activity(Activity.REST, Items.RED_BED, "Resting", "Sleeping or recovering.");
        activity(Activity.MEET, Items.BELL, "Meeting", "Socializing at a meeting point.");
        activity(Activity.PANIC, Items.SUGAR, "Panic", "Fleeing from immediate danger.");
        activity(Activity.RAID, Items.CROSSBOW, "Raiding", "Actively participating in a raid.");
        activity(Activity.PRE_RAID, Items.OMINOUS_BOTTLE, "Pre-Raid", "Preparing for a raid wave.");
        activity(Activity.HIDE, Items.SHIELD, "Hiding", "Taking cover from threats.");
        activity(Activity.FIGHT, Items.NETHERITE_SWORD, "Fighting", "Engaging in combat.");
        activity(Activity.CELEBRATE, Items.FIREWORK_ROCKET, "Celebrating", "Celebrating a victory.");
        activity(Activity.ADMIRE_ITEM, Items.GOLD_INGOT, "Admiring", "Distracted by a loved item.");
        activity(Activity.AVOID, Items.CACTUS, "Avoiding", "Keeping distance from threats.");
        activity(Activity.RIDE, Items.SADDLE, "Riding", "Riding another entity.");
        activity(Activity.PLAY_DEAD, Items.BONE, "Playing Dead", "Pretending to be dead.");
        activity(Activity.LONG_JUMP, Items.RABBIT_FOOT, "Leaping", "Performing a long jump.");
        activity(Activity.RAM, Items.GOAT_HORN, "Ramming", "Charging at a target.");
        activity(Activity.TONGUE, Items.SLIME_BALL, "Tongue Attack", "Using tongue to catch prey.");
        activity(Activity.SWIM, Items.WATER_BUCKET, "Swimming", "Swimming in water.");
        activity(Activity.LAY_SPAWN, Items.TURTLE_EGG, "Laying Spawn", "Laying eggs or spawning offspring.");
        activity(Activity.SNIFF, Items.MOSS_BLOCK, "Sniffing", "Searching for scents.");
        activity(Activity.INVESTIGATE, Items.SPYGLASS, "Investigating", "Checking out a disturbance.");
        activity(Activity.ROAR, Items.SCULK_SHRIEKER, "Roaring", "Emitting a loud roar.");
        activity(Activity.EMERGE, Items.DIRT, "Emerging", "Digging out of the ground.");
        activity(Activity.DIG, Items.IRON_SHOVEL, "Digging", "Digging into the ground.");

        // --- Sensors ---
        sensor(SensorType.NEAREST_ITEMS, Items.SPYGLASS, "Item Sense", "Can detect nearby items.");
        sensor(SensorType.NEAREST_LIVING_ENTITIES, Items.PLAYER_HEAD, "Mob Sense", "Can detect nearby mobs.");
        sensor(SensorType.NEAREST_PLAYERS, Items.PLAYER_HEAD, "Player Sense", "Can detect nearby players.");
        sensor(SensorType.NEAREST_BED, Items.RED_BED, "Bed Sense", "Can locate beds.");
        sensor(SensorType.HURT_BY, Items.IRON_AXE, "Damage Sense", "Remembers who hurt it.");
        sensor(SensorType.VILLAGER_HOSTILES, Items.ZOMBIE_HEAD, "Threat Sense", "Detects zombies and raiders.");
        sensor(SensorType.GOLEM_DETECTED, Items.IRON_BLOCK, "Golem Sense", "Detects Iron Golems.");
        sensor(SensorType.PIGLIN_SPECIFIC_SENSOR, Items.GOLD_INGOT, "Piglin Logic", "Senses gold and hoglins.");
        sensor(SensorType.HOGLIN_SPECIFIC_SENSOR, Items.CRIMSON_FUNGUS, "Hoglin Logic", "Senses piglins and repellents.");
        sensor(SensorType.WARDEN_ENTITY_SENSOR, Items.SCULK_SENSOR, "Vibration Sense", "Detects vibrations.");
        sensor(SensorType.IS_IN_WATER, Items.WATER_BUCKET, "Hydro Sense", "Knows if it is in water.");

        // --- Memories ---
        memory(MemoryModuleType.HOME, Items.RED_BED, "Home", "Remembers home location.");
        memory(MemoryModuleType.JOB_SITE, Items.EMERALD, "Job Site", "Remembers workplace.");
        memory(MemoryModuleType.MEETING_POINT, Items.BELL, "Meeting Point", "Remembers gathering spot.");
        memory(MemoryModuleType.ATTACK_TARGET, Items.DIAMOND_SWORD, "Target", "Focusing on an enemy.");
        memory(MemoryModuleType.WALK_TARGET, Items.COMPASS, "Destination", "Moving towards a specific spot.");
        memory(MemoryModuleType.ANGRY_AT, Items.BLAZE_POWDER, "Anger", "Angry at a specific entity.");
        memory(MemoryModuleType.LIKED_PLAYER, Items.COOKIE, "Friendship", "Likes a specific player.");
        memory(MemoryModuleType.HURT_BY, Items.DAMAGED_ANVIL, "Grudge", "Remembers recent damage source.");
        memory(MemoryModuleType.BREED_TARGET, Items.HEART_OF_THE_SEA, "Mate", "Focusing on a partner.");

        // --- Goals ---
        // Basic
        goal(FloatGoal.class, Items.LILY_PAD, "Buoyant", "Floats in water.");
        goal(PanicGoal.class, Items.SUGAR, "Skittish", "Runs away when hurt.");
        goal(LookAtPlayerGoal.class, Items.ENDER_EYE, "Watchful", "Watches nearby players.");
        goal(MeleeAttackGoal.class, Items.IRON_SWORD, "Melee", "Attacks targets in close range.");
        goal(RangedBowAttackGoal.class, Items.BOW, "Archer", "Shoots arrows at targets.");
        goal(TemptGoal.class, Items.WHEAT, "Temptable", "Follows players holding food.");
        goal(BreedGoal.class, Items.EGG, "Breedable", "Can produce offspring.");
        goal(AvoidEntityGoal.class, Items.BARRIER, "Fearful", "Avoids certain entities.");
        goal(FleeSunGoal.class, Items.ROTTEN_FLESH, "Photosensitive", "Burns in sunlight.");
        goal(EatBlockGoal.class, Items.GRASS_BLOCK, "Grazer", "Eats grass to regain health.");

        // Doors
        goal(OpenDoorGoal.class, Items.OAK_DOOR, "Door User", "Can open doors.");
        goal(BreakDoorGoal.class, Items.IRON_AXE, "Door Breaker", "Breaks down doors.");

        // Advanced
        goal(SwellGoal.class, Items.TNT, "Explosive", "Detonates near targets.");
        goal(SitWhenOrderedToGoal.class, Items.LEAD, "Obedient", "Sits when ordered.");
        goal(FollowOwnerGoal.class, Items.BONE, "Loyal", "Follows its owner.");
        goal(DefendVillageTargetGoal.class, Items.SHIELD, "Guardian", "Defends the village.");
        goal(HurtByTargetGoal.class, Items.IRON_CHESTPLATE, "Vengeful", "Attacks those who hurt it.");
        goal(NearestAttackableTargetGoal.class, Items.CROSSBOW, "Aggressive", "Hunts specific enemies.");
        goal(RandomStrollGoal.class, Items.LEATHER_BOOTS, "Wanderer", "Roams randomly.");
        goal(WaterAvoidingRandomStrollGoal.class, Items.LEATHER_BOOTS, "Land Wanderer", "Roams without entering water.");

        // Add more from your list as needed using the goal() helper
    }

    // --- Helpers ---

    private void activity(Activity activity, Item icon, String title, String description) {
        ResourceLocation key = BuiltInRegistries.ACTIVITY.getKey(activity);
        if (key == null) {
            Ethology.LOGGER.warn("No key found for activity: {}", activity.getName());
            return;
        }
        createJson("activity", key.toString(), icon, title, description);
    }

    private void sensor(SensorType<?> sensor, Item icon, String title, String description) {
        ResourceLocation key = BuiltInRegistries.SENSOR_TYPE.getKey(sensor);
        createJson("sensor", key.toString(), icon, title, description);
    }

    private void memory(MemoryModuleType<?> memory, Item icon, String title, String description) {
        ResourceLocation key = BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memory);
        createJson("memory", key.toString(), icon, title, description);
    }

    private void goal(Class<?> goalClass, Item icon, String title, String description) {
        createJson("goal", goalClass.getName(), icon, title, description);
    }

    private void createJson(String type, String target, Item icon, String title, String description) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("target", target);
        json.addProperty("icon", BuiltInRegistries.ITEM.getKey(icon).toString());
        json.addProperty("title", title);
        json.addProperty("description", description);

        // Save as <type>_<target_path>.json to avoid name collisions
        String filename = type + "_" + target.replace(":", "_").replace(".", "_")
                .toLowerCase(Locale.ROOT);
        toSerialize.put(ResourceLocation.fromNamespaceAndPath(Ethology.MODID, filename), json);
    }

    @Override
    public @NotNull String getName() {
        return "Ethology Traits";
    }
}