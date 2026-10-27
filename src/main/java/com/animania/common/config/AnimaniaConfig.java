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
    public static final ModConfigSpec.BooleanValue FACILITY_INTERACTIONS, ANIMAL_SOCIAL_BEHAVIOR;
    public static final ModConfigSpec.IntValue MOOD_GRACE_TICKS;

    public static final ModConfigSpec.BooleanValue BEE_DEFENSE, BEE_PROTECTION, BEE_ATTACK_ANIMALS;
    public static final ModConfigSpec.IntValue BEE_PURSUIT_TICKS, BEE_WARNING_TICKS, BEE_SMOKE_TICKS;
    public static final ModConfigSpec.DoubleValue BEE_PURSUIT_RADIUS, BEE_SWARM_DAMAGE;
    public static final ModConfigSpec.DoubleValue BEE_AMBER_DEFENSE, BEE_DARK_DEFENSE, BEE_PALE_DEFENSE;

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
        FACILITY_INTERACTIONS = BUILDER.comment("Use species-specific furniture and enrichment surfaces during quiet periods.").define("facilityInteractions", true);
        ANIMAL_SOCIAL_BEHAVIOR = BUILDER.comment("Remember familiar companions and allow species-specific social contact. Does not create mating bonds.").define("animalSocialBehavior", true);
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
        WILD_HIVE_DAMAGE = BUILDER.comment("Legacy setting retained for old configurations; defensive swarms now use beekeeping.swarmDamage.")
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
        BUILDER.push("beekeeping");
        BEE_DEFENSE = BUILDER.comment("Occupied Animania hives defend against harvesting and breaking. Empty hives never create bees.").define("defensiveSwarms", true);
        BEE_PROTECTION = BUILDER.comment("Beekeeper clothing reduces swarm damage; a complete intact suit prevents stings but wears down.").define("protectiveClothing", true);
        BEE_ATTACK_ANIMALS = BUILDER.comment("Allow a swarm to retaliate against an animal that directly attacks it. Bystanders are never targets.").define("retaliateAgainstAnimals", false);
        BEE_PURSUIT_TICKS = BUILDER.defineInRange("pursuitTicks", 400, 20, 2400);
        BEE_WARNING_TICKS = BUILDER.defineInRange("harvestWarningTicks", 60, 20, 400);
        BEE_SMOKE_TICKS = BUILDER.defineInRange("smokeTicks", 600, 20, 2400);
        BEE_PURSUIT_RADIUS = BUILDER.defineInRange("pursuitRadius", 16.0, 4.0, 48.0);
        BEE_SWARM_DAMAGE = BUILDER.comment("Damage per sting attempt, at most once per second, independent of the number of rendered bees.").defineInRange("swarmDamage", 2.0, 0.0, 20.0);
        BEE_AMBER_DEFENSE = BUILDER.comment("Gameplay temperament multipliers; coat colors are not real bee subspecies. Higher values shorten warning time. All breeds use the same neutral default.").defineInRange("amberDefensiveness", 1.0, 0.25, 4.0);
        BEE_DARK_DEFENSE = BUILDER.defineInRange("darkDefensiveness", 1.0, 0.25, 4.0);
        BEE_PALE_DEFENSE = BUILDER.defineInRange("paleDefensiveness", 1.0, 0.25, 4.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private AnimaniaConfig() {
    }
}
