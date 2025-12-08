package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import net.minecraft.world.entity.EntityType;
import java.util.HashMap;
import java.util.Map;

public class EthologyDatabase {
    // Stores the analyzed data for every EntityType
    private static final Map<EntityType<?>, MobScopedInfo> DATABASE = new HashMap<>();
    private static boolean isScanned = false;

    public static void register(EntityType<?> type, MobScopedInfo info) {
        DATABASE.put(type, info);
    }

    public static MobScopedInfo get(EntityType<?> type) {
        return DATABASE.get(type);
    }

    public static Map<EntityType<?>, MobScopedInfo> getAll() {
        return DATABASE;
    }

    public static boolean isScanned() {
        return isScanned;
    }

    public static void setScanned(boolean scanned) {
        isScanned = scanned;
    }
}