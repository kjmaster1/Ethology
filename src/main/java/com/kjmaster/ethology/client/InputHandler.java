package com.kjmaster.ethology.client;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.client.gui.EthologyScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Ethology.MODID, value = Dist.CLIENT)
public class InputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.OPEN_ETHOLOGY.consumeClick()) {
            Minecraft.getInstance().setScreen(new EthologyScreen());
        }
    }
}