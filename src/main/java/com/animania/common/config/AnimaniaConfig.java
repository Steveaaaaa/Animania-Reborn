package com.animania.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AnimaniaConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_NEEDS_ON_EMPTY_HAND;
    public static final ModConfigSpec.DoubleValue SALT_LICK_HEALING;
    public static final ModConfigSpec.IntValue HIVE_HONEY_PER_CYCLE;
    public static final ModConfigSpec.DoubleValue WILD_HIVE_DAMAGE;
    public static final ModConfigSpec.BooleanValue ENABLE_FARM_SPAWNS;
    public static final ModConfigSpec.BooleanValue ENABLE_EXTRA_SPAWNS;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_WILDLIFE_SPAWNS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Modern UI additions. Original husbandry rules are in animania-server.toml.")
                .push("husbandry");
        SHOW_NEEDS_ON_EMPTY_HAND = BUILDER.comment("Show an animal's hunger and thirst when it is clicked with an empty hand.")
                .define("showNeedsOnEmptyHand", true);
        SALT_LICK_HEALING = BUILDER.comment("Health restored when an animal uses a salt lick.")
                .defineInRange("saltLickHealing", 2.0D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.comment("NeoForge-only farm tuning. Legacy production timers are in animania_farm-server.toml.")
                .push("farm");
        HIVE_HONEY_PER_CYCLE = BUILDER.comment("Honey produced per successful cycle, in millibuckets.")
                .defineInRange("hiveHoneyPerCycle", 25, 1, 1000);
        WILD_HIVE_DAMAGE = BUILDER.comment("Damage dealt by a disturbed wild hive.")
                .defineInRange("wildHiveDamage", 2.5D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.comment("Natural spawning switches. Spawn weights and biome lists remain data-pack configurable.")
                .push("spawning");
        ENABLE_FARM_SPAWNS = BUILDER.define("enableFarmSpawns", true);
        ENABLE_EXTRA_SPAWNS = BUILDER.define("enableExtraSpawns", true);
        ENABLE_PET_WILDLIFE_SPAWNS = BUILDER.define("enablePetWildlifeSpawns", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private AnimaniaConfig() {
    }
}
