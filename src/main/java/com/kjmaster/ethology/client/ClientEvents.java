package com.kjmaster.ethology.client;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.core.EthologyScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Ethology.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        // We schedule the scan to run on the main thread after login is complete.
        // We enqueue it to ensure the Level is fully ready.
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            Level level = mc.level;

            if (mc.hasSingleplayerServer()) {
                MinecraftServer server = mc.getSingleplayerServer();
                if (server != null) {
                    // Use the Overworld from the internal server for analysis
                    level = server.overworld();
                }
            }

            if (level == null) return;

            EthologyScanner.scanAll(level);
        });
    }
}