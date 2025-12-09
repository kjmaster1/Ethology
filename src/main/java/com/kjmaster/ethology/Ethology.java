package com.kjmaster.ethology;

import com.kjmaster.ethology.api.RegisterEthologyInspectorsEvent;
import com.kjmaster.ethology.core.VanillaBrainInspectors;
import com.kjmaster.ethology.core.VanillaGoalInspectors;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(Ethology.MODID)
public class Ethology {

    public static final String MODID = "ethology";
    public static final Logger LOGGER = LogUtils.getLogger();
    private final IEventBus modEventBus;

    public Ethology(IEventBus modEventBus, ModContainer modContainer) {
        this.modEventBus = modEventBus;
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerInspectors);
    }

    private void addReloadListener(AddReloadListenerEvent event) {

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Post the custom registration event to the Mod Event Bus.
        // This allows Ethology (and other mods) to register their inspectors safely.
        event.enqueueWork(() -> this.modEventBus.post(new RegisterEthologyInspectorsEvent()));
    }

    private void registerInspectors(RegisterEthologyInspectorsEvent event) {
        LOGGER.debug("Registering Ethology Inspectors...");

        // --- GOALS ---
        // Interaction
        event.registerGoal(TemptGoal.class, VanillaGoalInspectors.TEMPT_INSPECTOR);
        event.registerGoal(BreedGoal.class, VanillaGoalInspectors.BREED_INSPECTOR);
        event.registerGoal(AvoidEntityGoal.class, VanillaGoalInspectors.AVOID_ENTITY_INSPECTOR);

        // Movement
        event.registerGoal(RandomStrollGoal.class, VanillaGoalInspectors.RANDOM_STROLL_INSPECTOR);
        event.registerGoal(WaterAvoidingRandomStrollGoal.class, VanillaGoalInspectors.WATER_AVOIDING_STROLL_INSPECTOR);
        event.registerGoal(FloatGoal.class, VanillaGoalInspectors.FLOAT_INSPECTOR);

        // Actions
        event.registerGoal(EatBlockGoal.class, VanillaGoalInspectors.EAT_BLOCK_INSPECTOR);
        event.registerGoal(OpenDoorGoal.class, VanillaGoalInspectors.OPEN_DOOR_INSPECTOR);
        event.registerGoal(BreakDoorGoal.class, VanillaGoalInspectors.BREAK_DOOR_INSPECTOR);
        event.registerGoal(SitWhenOrderedToGoal.class, VanillaGoalInspectors.SIT_INSPECTOR);

        // Combat
        event.registerGoal(MeleeAttackGoal.class, VanillaGoalInspectors.MELEE_ATTACK_INSPECTOR);
        event.registerGoal(RangedBowAttackGoal.class, VanillaGoalInspectors.RANGED_BOW_INSPECTOR);
        event.registerGoal(SwellGoal.class, VanillaGoalInspectors.SWELL_INSPECTOR);

        // Targeting
        event.registerGoal(NearestAttackableTargetGoal.class, VanillaGoalInspectors.AGGRESSIVE_TARGET_INSPECTOR);
        event.registerGoal(HurtByTargetGoal.class, VanillaGoalInspectors.HURT_BY_TARGET_INSPECTOR);
        event.registerGoal(DefendVillageTargetGoal.class, VanillaGoalInspectors.DEFEND_VILLAGE_INSPECTOR);

        // --- SENSORS ---
        event.registerSensor(SensorType.HOGLIN_SPECIFIC_SENSOR, VanillaBrainInspectors.HOGLIN_SENSOR);
        event.registerSensor(SensorType.WARDEN_ENTITY_SENSOR, VanillaBrainInspectors.WARDEN_SENSOR);

        // --- MEMORIES ---
        event.registerMemory(MemoryModuleType.HOME, VanillaBrainInspectors.HOME_INSPECTOR);
        event.registerMemory(MemoryModuleType.JOB_SITE, VanillaBrainInspectors.JOB_SITE_INSPECTOR);
        event.registerMemory(MemoryModuleType.MEETING_POINT, VanillaBrainInspectors.MEETING_POINT_INSPECTOR);
        event.registerMemory(MemoryModuleType.ANGRY_AT, VanillaBrainInspectors.ANGRY_AT_INSPECTOR);
    }
}
