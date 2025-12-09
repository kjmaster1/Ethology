package com.kjmaster.ethology.datagen;

import com.google.gson.JsonObject;
import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.api.TraitType;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EthologyTraitProvider implements DataProvider {
    private final PackOutput output;
    private final Map<ResourceLocation, JsonObject> traitsToSave = new HashMap<>();
    private final Map<String, String> langEntries = new TreeMap<>();

    public EthologyTraitProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        traitsToSave.clear();
        langEntries.clear();

        addTraits();
        addManualTranslations(); // Add the missing manual translations

        List<CompletableFuture<?>> futures = new ArrayList<>();

        // 1. Save Trait JSONs
        for (Map.Entry<ResourceLocation, JsonObject> entry : traitsToSave.entrySet()) {
            Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                    .resolve(Ethology.MODID)
                    .resolve(Ethology.MODID + "/ethology_traits")
                    .resolve(entry.getKey().getPath() + ".json");
            futures.add(DataProvider.saveStable(cache, entry.getValue(), path));
        }

        // 2. Save Lang JSON
        JsonObject langJson = new JsonObject();
        langJson.addProperty("itemGroup.ethology", "Ethology");
        langJson.addProperty("key.ethology.open", "Open Ethology Journal");
        langJson.addProperty("key.category.ethology", "Ethology");

        // Add "Unknown Goal" fallback for debug mode
        langJson.addProperty("ethology.trait.goal.unknown.title", "Unknown Behavior");
        langJson.addProperty("ethology.trait.goal.unknown.static", "Undocumented behavior.");
        langJson.addProperty("ethology.trait.goal.unknown.active", "Performing unknown behavior.");

        langEntries.forEach(langJson::addProperty);

        Path langPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(Ethology.MODID)
                .resolve("lang/en_us.json");
        futures.add(DataProvider.saveStable(cache, langJson, langPath));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void addManualTranslations() {
        // Stats / Ecological Classifier
        manualTrait("ethology.trait.stat.aquatic", "Aquatic", "Naturally adapted to water.", "Swimming.");
        manualTrait("ethology.trait.stat.aerial", "Aerial", "Capable of flight.", "Flying.");
        manualTrait("ethology.trait.stat.passive", "Passive", "Naturally harmless.", "Passive.");
        manualTrait("ethology.trait.stat.neutral", "Neutral", "Neutral until provoked.", "Neutral.");
        manualTrait("ethology.trait.stat.hostile", "Hostile", "Aggressive towards players.", "Hostile.");

        // Dynamic Goals
        manualTrait("ethology.trait.goal.temptable", "Temptable", "Can be tempted with food.", "Being tempted.");
        manualTrait("ethology.trait.goal.fearful", "Fearful", "Avoids certain threats.", "Fleeing threat.");
        manualTrait("ethology.trait.goal.aggressive", "Aggressive", "Aggressive towards targets.", "Attacking target.");
    }

    private void addTraits() {
        // --- Activities ---
        activity(Activity.CORE, Items.HEART_OF_THE_SEA, "Core", "Essential brain function.", "Performing essential brain functions.");
        activity(Activity.IDLE, Items.CLOCK, "Idle", "Waiting for something to happen.", "Currently idle.");
        activity(Activity.WORK, Items.CRAFTING_TABLE, "Working", "Can work at a job site.", "Working at job site.");
        activity(Activity.PLAY, Items.POPPY, "Playing", "Can engage in play with others.", "Playing.");
        activity(Activity.REST, Items.RED_BED, "Resting", "Can Sleep.", "Sleeping.");
        activity(Activity.MEET, Items.BELL, "Meeting", "Can Socialize at a meeting point.", "Socializing.");
        activity(Activity.PANIC, Items.SUGAR, "Panic", "Can Flee from immediate danger.", "Panicking!");
        activity(Activity.RAID, Items.CROSSBOW, "Raiding", "Can participate in a raid.", "Raiding.");
        activity(Activity.PRE_RAID, Items.OMINOUS_BOTTLE, "Pre-Raid", "Can prepare for a raid wave.", "Preparing to raid.");
        activity(Activity.HIDE, Items.SHIELD, "Hiding", "Can take cover from threats.", "Hiding.");
        activity(Activity.FIGHT, Items.NETHERITE_SWORD, "Fighting", "Can engage in combat.", "Fighting target.");
        activity(Activity.CELEBRATE, Items.FIREWORK_ROCKET, "Celebrating", "Can celebrate a victory.", "Celebrating.");
        activity(Activity.ADMIRE_ITEM, Items.GOLD_INGOT, "Admiring", "Can be distracted by a loved item.", "Admiring item.");
        activity(Activity.AVOID, Items.CACTUS, "Avoiding", "Can Keep distance from threats.", "Fleeing threat.");
        activity(Activity.RIDE, Items.SADDLE, "Riding", "Can Ride another entity.", "Riding.");
        activity(Activity.PLAY_DEAD, Items.BONE, "Playing Dead", "Can pretend to be dead.", "Playing dead.");
        activity(Activity.LONG_JUMP, Items.RABBIT_FOOT, "Leaping", "Can perform a long jump.", "Leaping.");
        activity(Activity.RAM, Items.GOAT_HORN, "Ramming", "Can charge at a target.", "Ramming.");
        activity(Activity.TONGUE, Items.SLIME_BALL, "Tongue Attack", "Can use tongue to catch prey.", "Attacking with tongue.");
        activity(Activity.SWIM, Items.WATER_BUCKET, "Swimming", "Can swim in water.", "Swimming.");
        activity(Activity.LAY_SPAWN, Items.TURTLE_EGG, "Laying Spawn", "Can lay eggs or spawn offspring.", "Laying spawn.");
        activity(Activity.SNIFF, Items.MOSS_BLOCK, "Sniffing", "Can searching for scents.", "Sniffing.");
        activity(Activity.INVESTIGATE, Items.SPYGLASS, "Investigating", "Can check out a disturbance.", "Investigating.");
        activity(Activity.ROAR, Items.SCULK_SHRIEKER, "Roaring", "Can emit a loud roar.", "Roaring.");
        activity(Activity.EMERGE, Items.DIRT, "Emerging", "Can emerge out of the ground.", "Emerging from ground.");
        activity(Activity.DIG, Items.IRON_SHOVEL, "Digging", "Can dig into the ground.", "Digging.");

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
        goal(FloatGoal.class, Items.LILY_PAD, "Buoyant", "Floats in water.", "Floating.");
        goal(PanicGoal.class, Items.SUGAR, "Skittish", "Runs away when hurt.", "Fleeing.");
        goal(LookAtPlayerGoal.class, Items.ENDER_EYE, "Watchful", "Watches nearby players.", "Watching player.");
        goal(MeleeAttackGoal.class, Items.IRON_SWORD, "Melee", "Attacks targets in close range.", "Attacking (Melee).");
        goal(RangedBowAttackGoal.class, Items.BOW, "Archer", "Shoots arrows at targets.", "Shooting arrows.");
        goal(TemptGoal.class, Items.WHEAT, "Temptable", "Follows players holding food.", "Being tempted.");
        goal(BreedGoal.class, Items.EGG, "Breedable", "Can produce offspring.", "Breeding.");
        goal(AvoidEntityGoal.class, Items.BARRIER, "Fearful", "Avoids certain entities.", "Avoiding threat.");
        goal(FleeSunGoal.class, Items.ROTTEN_FLESH, "Photosensitive", "Burns in sunlight.", "Fleeing sun.");
        goal(EatBlockGoal.class, Items.GRASS_BLOCK, "Grazer", "Eats grass to regain health.", "Grazing.");
        goal(OpenDoorGoal.class, Items.OAK_DOOR, "Door User", "Can open doors.", "Opening door.");
        goal(BreakDoorGoal.class, Items.IRON_AXE, "Door Breaker", "Breaks down doors.", "Breaking door.");
        goal(SwellGoal.class, Items.TNT, "Explosive", "Detonates near targets.", "Exploding.");
        goal(SitWhenOrderedToGoal.class, Items.LEAD, "Obedient", "Sits when ordered.", "Sitting.");
        goal(FollowOwnerGoal.class, Items.BONE, "Loyal", "Follows its owner.", "Following owner.");
        goal(DefendVillageTargetGoal.class, Items.SHIELD, "Guardian", "Defends the village.", "Defending village.");
        goal(HurtByTargetGoal.class, Items.IRON_CHESTPLATE, "Vengeful", "Attacks those who hurt it.", "Seeking vengeance.");
        goal(NearestAttackableTargetGoal.class, Items.CROSSBOW, "Aggressive", "Hunts specific enemies.", "Hunting target.");
        goal(RandomStrollGoal.class, Items.LEATHER_BOOTS, "Wanderer", "Roams randomly.", "Wandering.");
        goal(WaterAvoidingRandomStrollGoal.class, Items.LEATHER_BOOTS, "Land Wanderer", "Roams without entering water.", "Wandering (Land).");
    }

    // --- Helpers ---

    private void manualTrait(String key, String title, String staticDesc, String activeDesc) {
        langEntries.put(key + ".title", title);
        langEntries.put(key + ".static", staticDesc);
        langEntries.put(key + ".active", activeDesc);
    }

    private void activity(Activity activity, Item icon, String title, String staticDesc, String activeDesc) {
        ResourceLocation key = BuiltInRegistries.ACTIVITY.getKey(activity);
        createJson(TraitType.ACTIVITY, key.toString(), icon, title, staticDesc, activeDesc);
    }

    private void sensor(SensorType<?> sensor, Item icon, String title, String staticDesc) {
        ResourceLocation key = BuiltInRegistries.SENSOR_TYPE.getKey(sensor);
        createJson(TraitType.SENSOR, key.toString(), icon, title, staticDesc, null);
    }

    private void memory(MemoryModuleType<?> memory, Item icon, String title, String staticDesc) {
        ResourceLocation key = BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memory);
        createJson(TraitType.MEMORY, key.toString(), icon, title, staticDesc, null);
    }

    private void goal(Class<?> goalClass, Item icon, String title, String staticDesc, String activeDesc) {
        createJson(TraitType.GOAL, goalClass.getName(), icon, title, staticDesc, activeDesc);
    }

    private void createJson(TraitType type, String target, Item icon, String title, String staticDesc, String activeDesc) {
        String keyBase = target.replace(":", ".").replace(".", "_").toLowerCase(Locale.ROOT);
        String translationKey = "ethology.trait." + type.name().toLowerCase() + "." + keyBase;

        langEntries.put(translationKey + ".title", title);
        langEntries.put(translationKey + ".static", staticDesc);
        if (activeDesc != null) {
            langEntries.put(translationKey + ".active", activeDesc);
        } else {
            langEntries.put(translationKey + ".active", staticDesc);
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", type.name().toLowerCase());
        json.addProperty("target", target);
        json.addProperty("translation_key", translationKey);
        json.addProperty("icon", BuiltInRegistries.ITEM.getKey(icon).toString());

        String filename = type.name().toLowerCase() + "_" + target.replace(":", "_").replace(".", "_")
                .toLowerCase(Locale.ROOT);
        traitsToSave.put(ResourceLocation.fromNamespaceAndPath(Ethology.MODID, filename), json);
    }

    @Override
    public @NotNull String getName() {
        return "Ethology Traits & Lang";
    }
}