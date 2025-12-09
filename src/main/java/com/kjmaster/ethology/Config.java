package com.kjmaster.ethology;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_MODE = BUILDER
            .comment("If true, any AI Goal that does not have a registered Trait JSON will be displayed in the UI as 'Unknown Goal'. Useful for development.")
            .define("debugMode", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
