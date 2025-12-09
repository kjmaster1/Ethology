package com.kjmaster.ethology.core;

import com.kjmaster.ethology.api.MobScopedInfo;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EthologyDatabase {
    // Stores the analyzed data for every EntityType (for UI display)
    private static final Map<EntityType<?>, MobScopedInfo> DATABASE = new HashMap<>();

    // Cache for specific instances to prevent packet flooding
    private static final Map<UUID, MobScopedInfo> INSTANCE_CACHE = new HashMap<>();
    private static final Map<UUID, Long> CACHE_TIMESTAMPS = new HashMap<>();
    private static final long CACHE_DURATION_MS = 5000L; // 5 Seconds Cache

    private static boolean isScanned = false;

    public static void register(EntityType<?> type, MobScopedInfo info) {
        DATABASE.put(type, info);

        // If this info belongs to a specific instance, cache it
        if (info.getUuid() != null) {
            INSTANCE_CACHE.put(info.getUuid(), info);
            CACHE_TIMESTAMPS.put(info.getUuid(), System.currentTimeMillis());
        }
    }

    public static MobScopedInfo get(EntityType<?> type) {
        return DATABASE.get(type);
    }

    /**
     * Retrieves cached data for a specific UUID if it is fresh ( < 5 seconds old).
     */
    public static MobScopedInfo getFreshInstance(UUID uuid) {
        if (!INSTANCE_CACHE.containsKey(uuid)) return null;

        long lastUpdate = CACHE_TIMESTAMPS.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - lastUpdate < CACHE_DURATION_MS) {
            return INSTANCE_CACHE.get(uuid);
        }
        return null;
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

    public static void clear() {
        DATABASE.clear();
        INSTANCE_CACHE.clear();
        CACHE_TIMESTAMPS.clear();
        isScanned = false;
    }
}