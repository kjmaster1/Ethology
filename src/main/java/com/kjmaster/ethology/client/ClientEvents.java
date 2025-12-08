package com.kjmaster.ethology.client;

import com.kjmaster.ethology.Ethology;
import com.kjmaster.ethology.core.EthologyDatabase;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Ethology.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Clear static state to prevent data persisting between worlds/servers.
        EthologyDatabase.clear();
        Ethology.LOGGER.debug("Ethology Database cleared on logout.");
    }
}