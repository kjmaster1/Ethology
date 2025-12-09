package com.kjmaster.ethology;

import com.kjmaster.ethology.core.EthologyTraitManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(Ethology.MODID)
public class Ethology {

    public static final String MODID = "ethology";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final EthologyTraitManager TRAIT_MANAGER = new EthologyTraitManager();

    public Ethology(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(TRAIT_MANAGER);
    }
}
