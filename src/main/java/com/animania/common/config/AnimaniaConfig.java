package com.animania.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AnimaniaConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SHOW_NEEDS_ON_EMPTY_HAND;
    public static final ForgeConfigSpec.DoubleValue SALT_LICK_HEALING;
    public static final ForgeConfigSpec.IntValue HIVE_HONEY_PER_CYCLE;
    public static final ForgeConfigSpec.DoubleValue WILD_HIVE_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FARM_SPAWNS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_SPAWNS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PET_WILDLIFE_SPAWNS;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.comment("Modern UI additions. Original husbandry rules are in animania-server.toml.")
                .push("husbandry");
        SHOW_NEEDS_ON_EMPTY_HAND = BUILDER.comment("Show an animal's hunger and thirst when it is clicked with an empty hand.")
                .define("showNeedsOnEmptyHand", true);
        SALT_LICK_HEALING = BUILDER.comment("Health restored when an animal uses a salt lick.")
                .defineInRange("saltLickHealing", 2.0D, 0.0D, 100.0D);
        BUILDER.pop();

        BUILDER.comment("Additional farm tuning. Legacy production timers are in animania_farm-server.toml.")
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
