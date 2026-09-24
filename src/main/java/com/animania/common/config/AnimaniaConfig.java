package com.animania.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AnimaniaConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SALT_LICK_HEALING;
    public static final ModConfigSpec.IntValue HIVE_HONEY_PER_CYCLE;
    public static final ModConfigSpec.DoubleValue WILD_HIVE_DAMAGE;
    public static final ModConfigSpec.BooleanValue ENABLE_FARM_SPAWNS;
    public static final ModConfigSpec.BooleanValue ENABLE_EXTRA_SPAWNS;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_WILDLIFE_SPAWNS;
    public static final ModConfigSpec.BooleanValue REPLACE_MODERN_FOXES;
    public static final ModConfigSpec.BooleanValue REPLACE_MOUNTAIN_GOATS;
    public static final ModConfigSpec.BooleanValue REPLACE_BEES;
    public static final ModConfigSpec.BooleanValue REPLACE_AXOLOTLS;

    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> HUNGER_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> THIRST_BLACKLIST;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_ANIMALS;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_CATS;

    public static final ModConfigSpec.BooleanValue ANIMAL_MOOD, MOOD_PENALTIES;
    public static final ModConfigSpec.IntValue MOOD_GRACE_TICKS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Modern UI additions. Original husbandry rules are in animania-server.toml.")
                .push("husbandry");
        HUNGER_BLACKLIST = BUILDER.comment("Disable hunger for these Animania animals. Use entity IDs, * wildcards, or groups:",
                "cats (including ocelots), dogs, wolves, foxes, cows, pigs, sheep, goats, horses, chickens, peafowl, rabbits, hamsters, hedgehogs, ferrets, axolotls.",
                "Examples: [\"cats\", \"animania:*_draft\"]. Use [\"*\"] for all. Feeding, taming and breeding remain available.")
                .defineListAllowEmpty("hungerBlacklist", java.util.List.<String>of(), value -> value instanceof String text && !text.isBlank());
        THIRST_BLACKLIST = BUILDER.comment("Disable thirst for the same entity IDs, wildcards or groups supported by hungerBlacklist.")
                .defineListAllowEmpty("thirstBlacklist", java.util.List.<String>of(), value -> value instanceof String text && !text.isBlank());
        ANIMAL_MOOD = BUILDER.comment("Species-specific mood for player-fed, tamed or penned animals. Wild animals are unaffected.")
                .define("animalMood", true);
        MOOD_PENALTIES = BUILDER.comment("Apply reduced production and breeding readiness when husbandry mood is low.")
                .define("moodPenalties", true);
        MOOD_GRACE_TICKS = BUILDER.comment("No mood bonuses or penalties during the initial settling-in period, in world calendar ticks. Sleeping through the night counts. Default: 72000 (three Minecraft days).")
                .defineInRange("moodGraceTicks", 72000, 0, 720000);
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
        REPLACE_VANILLA_ANIMALS = BUILDER.comment("Master switch for replacing vanilla animals. False keeps vanilla spawning alongside enabled Animania spawns.",
                "Does not restore previously replaced animals or add extra spawns for replacement-only breeds. Restart the world after changing spawning options.")
                .define("replaceVanillaAnimals", true);
        REPLACE_VANILLA_CATS = BUILDER.comment("Replace newly spawned vanilla cats, including village cats. Independent of the legacy ocelot switch.")
                .define("replaceVanillaCats", true);
        ENABLE_FARM_SPAWNS = BUILDER.define("enableFarmSpawns", true);
        ENABLE_EXTRA_SPAWNS = BUILDER.define("enableExtraSpawns", true);
        ENABLE_PET_WILDLIFE_SPAWNS = BUILDER.define("enablePetWildlifeSpawns", true);
        REPLACE_AXOLOTLS = BUILDER.comment("Replace new natural axolotls; existing animals and bucket releases are kept.")
                .define("replaceAxolotls", true);
        REPLACE_BEES = BUILDER.comment("Replace newly generated bees, including occupants of new bee nests; saved bees are kept.")
                .define("replaceBees", true);
        REPLACE_MODERN_FOXES = BUILDER.comment("Replace newly spawned wild foxes; saved and player-created animals are kept.")
                .define("replaceModernFoxes", true);
        REPLACE_MOUNTAIN_GOATS = BUILDER.comment("Replace newly spawned mountain goats with cared-for mountain goats.")
                .define("replaceMountainGoats", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private AnimaniaConfig() {
    }
}
