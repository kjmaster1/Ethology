package com.kjmaster.ethology;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> DENY_LIST = BUILDER
            .comment("A list of Entity Resource Locations (e.g., 'minecraft:ender_dragon') that should NEVER be instantiated for analysis.")
            .defineListAllowEmpty(
                    "denyList",
                    Collections::emptyList,       // Default value (empty list)
                    () -> "minecraft:pig",        // New element default (for UI)
                    obj -> obj instanceof String  // Validator
            );

    static final ModConfigSpec SPEC = BUILDER.build();
}
