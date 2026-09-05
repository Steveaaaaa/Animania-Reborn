package com.animania.common.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NeoForge representation of every user-facing option in Animania 1.12's
 * CommonConfig, FarmConfig, ExtraConfig and CatsDogsConfig.
 */
public final class LegacyConfig {
    public static final ModConfigSpec BASE_SPEC;
    public static final ModConfigSpec FARM_SPEC;
    public static final ModConfigSpec EXTRA_SPEC;
    public static final ModConfigSpec CATSDOGS_SPEC;

    public static final Map<String, ModConfigSpec.ConfigValue<List<? extends String>>> FOOD_LISTS = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.ConfigValue<String>> PREFERRED_BEDS = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.ConfigValue<String>> BACKUP_BEDS = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.ConfigValue<List<? extends String>>> BIOME_TYPES = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.BooleanValue> SPAWN_ENABLED = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.IntValue> SPAWN_PROBABILITY = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.IntValue> SPAWN_LIMIT = new LinkedHashMap<>();
    public static final Map<String, ModConfigSpec.IntValue> FAMILY_COUNT = new LinkedHashMap<>();

    // Base: game rules
    public static final ModConfigSpec.BooleanValue FOODS_GIVE_BONUS_EFFECTS;
    public static final ModConfigSpec.BooleanValue SHOW_MOD_UPDATE_NOTIFICATION;
    public static final ModConfigSpec.BooleanValue SHOW_PARTS;
    public static final ModConfigSpec.BooleanValue SHOW_UNHAPPY_PARTICLES;
    public static final ModConfigSpec.BooleanValue ALLOW_SEED_DISPENSER_PLACEMENT;
    public static final ModConfigSpec.BooleanValue SHIFT_SEED_PLACEMENT;
    public static final ModConfigSpec.BooleanValue ANIMALS_STARVE;
    public static final ModConfigSpec.BooleanValue ALLOW_MOB_RIDING;
    public static final ModConfigSpec.BooleanValue ALLOW_TROUGH_AUTOMATION;
    public static final ModConfigSpec.DoubleValue FALL_DAMAGE_REDUCE_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue WATER_REMOVED_AFTER_DRINKING;
    public static final ModConfigSpec.BooleanValue PLANTS_REMOVED_AFTER_EATING;
    public static final ModConfigSpec.BooleanValue AMBIANCE_MODE;
    public static final ModConfigSpec.BooleanValue ANIMALS_SLEEP;
    public static final ModConfigSpec.BooleanValue ANIMALS_CAN_ATTACK_OTHERS;
    public static final ModConfigSpec.IntValue TICKS_BETWEEN_AI_FIRINGS;
    public static final ModConfigSpec.BooleanValue TAMED_ANIMALS_TELEPORT;
    public static final ModConfigSpec.BooleanValue FANCY_EGGS;
    public static final ModConfigSpec.BooleanValue FANCY_EGGS_ROTATE;
    public static final ModConfigSpec.BooleanValue EAT_FOOD_ANYTIME;
    public static final ModConfigSpec.BooleanValue BIRDS_DROP_FEATHERS;
    public static final ModConfigSpec.IntValue AI_BLOCK_SEARCH_RANGE;
    public static final ModConfigSpec.IntValue ANIMAL_CAP_SEARCH_RANGE;
    public static final ModConfigSpec.BooleanValue REQUIRE_ANIMAL_INTERACTION_FOR_AI;
    public static final ModConfigSpec.BooleanValue SPAWN_FRESH_WATER_SQUIDS;

    // Base: care and feeding
    public static final ModConfigSpec.IntValue CHILD_GROWTH_TICK;
    public static final ModConfigSpec.IntValue FEED_TIMER;
    public static final ModConfigSpec.IntValue WATER_TIMER;
    public static final ModConfigSpec.IntValue PLAY_TIMER;
    public static final ModConfigSpec.IntValue LAID_TIMER;
    public static final ModConfigSpec.IntValue FEATHER_TIMER;
    public static final ModConfigSpec.IntValue GESTATION_TIMER;
    public static final ModConfigSpec.BooleanValue FEED_TO_BREED;
    public static final ModConfigSpec.IntValue WOOL_REGROWTH_TIMER;
    public static final ModConfigSpec.IntValue STARVATION_TIMER;
    public static final ModConfigSpec.IntValue EGG_HATCH_CHANCE;
    public static final ModConfigSpec.IntValue SALT_LICK_TICK;
    public static final ModConfigSpec.IntValue SALT_LICK_MAX_USES;
    public static final ModConfigSpec.IntValue ENTITY_BREEDING_LIMIT;
    public static final ModConfigSpec.BooleanValue MALES_MATE_MULTIPLE_FEMALES;
    public static final ModConfigSpec.DoubleValue BIRTH_MULTIPLE_CHANCE;
    public static final ModConfigSpec.DoubleValue ANIMAL_LOSS_CHANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FOOD_VALUE_OVERRIDES;

    // Farm settings
    public static final ModConfigSpec.BooleanValue ALLOW_EGG_THROWING;
    public static final ModConfigSpec.IntValue CHEESE_MATURITY_TIME;
    public static final ModConfigSpec.BooleanValue COWS_MILKABLE_AT_SPAWN;
    public static final ModConfigSpec.BooleanValue SLEEP_ALLOWED_WAGON;
    public static final ModConfigSpec.BooleanValue DISABLE_SALT_CREATION;
    public static final ModConfigSpec.IntValue SALT_CREATION_AMOUNT;
    public static final ModConfigSpec.BooleanValue DISABLE_ROLLING_VEHICLES;
    public static final ModConfigSpec.BooleanValue CHICKENS_DROP_EGGS;
    public static final ModConfigSpec.BooleanValue HIVE_SPAWNING;
    public static final ModConfigSpec.IntValue HIVE_SPAWNING_FREQUENCY;
    public static final ModConfigSpec.IntValue HIVE_WILD_HONEY_RATE;
    public static final ModConfigSpec.IntValue HIVE_PLAYERMADE_HONEY_RATE;
    public static final ModConfigSpec.BooleanValue ROOSTERS_FIGHT;

    // Vanilla replacement switches
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_COWS;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_PIGS;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_CHICKENS;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_SHEEP;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_HORSES;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_RABBITS;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_WOLVES;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_OCELOTS;

    // Extra/Cats & Dogs utilities
    public static final ModConfigSpec.IntValue HAMSTER_WHEEL_CAPACITY;
    public static final ModConfigSpec.IntValue HAMSTER_WHEEL_RF_GENERATION;
    public static final ModConfigSpec.IntValue HAMSTER_WHEEL_USE_TIME;

    static {
        ModConfigSpec.Builder base = new ModConfigSpec.Builder();
        base.push("gameRules");
        FOODS_GIVE_BONUS_EFFECTS = bool(base, "foodsGiveBonusEffects", true, "Foods give bonus effects");
        SHOW_MOD_UPDATE_NOTIFICATION = bool(base, "showModUpdateNotification", true, "Show mod update notification at startup");
        SHOW_PARTS = bool(base, "showParts", false, "Show male parts (modesty flag)");
        SHOW_UNHAPPY_PARTICLES = bool(base, "showUnhappyParticles", true, "Show particles when animals are hungry or thirsty");
        ALLOW_SEED_DISPENSER_PLACEMENT = bool(base, "allowSeedDispenserPlacement", true, "Allow dispensers to place seeds");
        SHIFT_SEED_PLACEMENT = bool(base, "shiftSeedPlacement", false, "Require shift-right-click for seed placement");
        ANIMALS_STARVE = bool(base, "animalsStarve", false, "Animals take starvation damage when not fed and watered");
        ALLOW_MOB_RIDING = bool(base, "allowMobRiding", true, "Allow random mobs to ride Animania animals");
        ALLOW_TROUGH_AUTOMATION = bool(base, "allowTroughAutomation", true, "Allow trough automation with hoppers and capabilities");
        FALL_DAMAGE_REDUCE_MULTIPLIER = decimal(base, "fallDamageReduceMultiplier", 0.45, 0, 1, "Leashed-animal fall damage multiplier");
        WATER_REMOVED_AFTER_DRINKING = bool(base, "waterRemovedAfterDrinking", true, "Remove source water after large animals drink");
        PLANTS_REMOVED_AFTER_EATING = bool(base, "plantsRemovedAfterEating", true, "Remove plant blocks after animals eat them");
        AMBIANCE_MODE = bool(base, "ambianceMode", false, "Animals do not need food or water");
        ANIMALS_SLEEP = bool(base, "animalsSleep", true, "Animals sleep");
        ANIMALS_CAN_ATTACK_OTHERS = bool(base, "animalsCanAttackOthers", true, "Animals can attack other entities");
        FOOD_LISTS.put("trough", strings(base, "troughFood", List.of("animania:block_straw", "minecraft:wheat", "simplecorn:corncob", "harvestcraft:barleyitem", "harvestcraft:oatsitem", "harvestcraft:ryeitem", "harvestcraft:cornitem", "minecraft:apple", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato", "minecraft:poisonous_potato", "minecraft:wheat_seeds", "minecraft:melon_seeds", "minecraft:beetroot_seeds", "minecraft:pumpkin_seeds", "biomesoplenty:turnip_seeds", "minecraft:egg", "animania:brown_egg", "listAllbeefraw", "minecraft:fish"), "Items accepted by troughs"));
        TICKS_BETWEEN_AI_FIRINGS = integer(base, "ticksBetweenAIFirings", 100, 1, 72_000, "AI tick countdown timer");
        TAMED_ANIMALS_TELEPORT = bool(base, "tamedAnimalsTeleport", true, "Tamed animals teleport to their owner");
        FANCY_EGGS = bool(base, "fancyEggs", false, "Use fancy entity spawn eggs");
        FANCY_EGGS_ROTATE = bool(base, "fancyEggsRotate", false, "Rotate fancy entity eggs");
        EAT_FOOD_ANYTIME = bool(base, "eatFoodAnytime", true, "Allow Animania food to be eaten at full hunger");
        BIRDS_DROP_FEATHERS = bool(base, "birdsDropFeathers", true, "Birds shed feathers naturally");
        AI_BLOCK_SEARCH_RANGE = integer(base, "aiBlockSearchRange", 16, 1, 64, "Range animals search for food, water and beds");
        ANIMAL_CAP_SEARCH_RANGE = integer(base, "animalCapSearchRange", 80, 8, 256, "Range used by per-species spawn caps");
        REQUIRE_ANIMAL_INTERACTION_FOR_AI = bool(base, "requireAnimalInteractionForAI", true, "Untouched naturally spawned animals do not eat or breed");
        SPAWN_FRESH_WATER_SQUIDS = bool(base, "spawnFreshWaterSquids", true, "Allow squids to spawn in fresh water");
        base.pop();

        base.push("careAndFeeding");
        CHILD_GROWTH_TICK = integer(base, "childGrowthTick", 200, 1, 72_000, "Ticks before incremental child growth");
        FEED_TIMER = integer(base, "feedTimer", 12_000, 1, 2_400_000, "Ticks between feedings");
        WATER_TIMER = integer(base, "waterTimer", 12_000, 1, 2_400_000, "Ticks between drinking");
        PLAY_TIMER = integer(base, "playTimer", 12_000, 1, 2_400_000, "Ticks between playing");
        LAID_TIMER = integer(base, "laidTimer", 2_000, 1, 2_400_000, "Ticks between laying eggs");
        FEATHER_TIMER = integer(base, "featherTimer", 12_000, 1, 2_400_000, "Ticks between feather drops");
        GESTATION_TIMER = integer(base, "gestationTimer", 20_000, 1, 2_400_000, "Ticks between mating and birth");
        FEED_TO_BREED = bool(base, "feedToBreed", true, "Mammals mate only after hand-feeding");
        WOOL_REGROWTH_TIMER = integer(base, "woolRegrowthTimer", 8_000, 1, 2_400_000, "Ticks before wool regrows");
        STARVATION_TIMER = integer(base, "starvationTimer", 400, 1, 72_000, "Ticks between starvation damage");
        EGG_HATCH_CHANCE = integer(base, "eggHatchChance", 2, 1, 10_000, "Egg hatch chance denominator (1/x)");
        SALT_LICK_TICK = integer(base, "saltLickTick", 8_000, 1, 2_400_000, "Ticks between salt lick uses");
        SALT_LICK_MAX_USES = integer(base, "saltLickMaxUses", 200, 1, 1_000_000, "Maximum salt lick uses");
        FOOD_LISTS.put("slop", strings(base, "slopIngredients", List.of("minecraft:carrot", "minecraft:beetroot", "minecraft:potato", "minecraft:poisonous_potato", "minecraft:bread"), "Ingredients used to make slop"));
        ENTITY_BREEDING_LIMIT = integer(base, "entityBreedingLimit", 15, 0, 10_000, "Nearby same-type breeding limit");
        MALES_MATE_MULTIPLE_FEMALES = bool(base, "malesMateMultipleFemales", false, "Allow males to have multiple mates");
        BIRTH_MULTIPLE_CHANCE = decimal(base, "birthMultipleChance", 0.1, 0, 1, "Geometric chance of additional offspring");
        ANIMAL_LOSS_CHANCE = decimal(base, "animalLossChance", 0, 0, 1, "Chance poorly cared-for pregnant animals lose the pregnancy");
        base.pop();
        base.push("foodValues");
        FOOD_VALUE_OVERRIDES = strings(base, "foodValueOverrides", List.of(), "modid:item(hunger,saturationMultiplier) overrides");
        base.pop();
        BASE_SPEC = base.build();

        ModConfigSpec.Builder farm = new ModConfigSpec.Builder();
        farm.push("farm");
        ALLOW_EGG_THROWING = bool(farm, "allowEggThrowing", false, "Allow eggs to be thrown");
        CHEESE_MATURITY_TIME = integer(farm, "cheeseMaturityTime", 24_000, 20, 2_400_000, "Ticks cheese takes to mature");
        COWS_MILKABLE_AT_SPAWN = bool(farm, "cowsMilkableAtSpawn", false, "Adult female cows are milkable when spawned");
        SLEEP_ALLOWED_WAGON = bool(farm, "sleepAllowedWagon", true, "Allow sleeping using a wagon");
        DISABLE_SALT_CREATION = bool(farm, "disableSaltCreation", false, "Disable salt creation in cheese molds");
        SALT_CREATION_AMOUNT = integer(farm, "saltCreationAmount", 16, 1, 64, "Salt items produced by a cheese mold");
        DISABLE_ROLLING_VEHICLES = bool(farm, "disableRollingVehicles", false, "Disable carts and wagons");
        CHICKENS_DROP_EGGS = bool(farm, "chickensDropEggs", false, "Birds may lay eggs without a nest");
        HIVE_SPAWNING = bool(farm, "hiveSpawning", true, "Generate wild beehives");
        HIVE_SPAWNING_FREQUENCY = integer(farm, "hiveSpawningFrequency", 3, 1, 10, "Wild hive generation frequency");
        HIVE_WILD_HONEY_RATE = integer(farm, "hiveWildHoneyRate", 700, 20, 72_000, "Wild hive honey creation rate");
        HIVE_PLAYERMADE_HONEY_RATE = integer(farm, "hivePlayermadeHoneyRate", 450, 20, 72_000, "Crafted hive honey creation rate");
        BIOME_TYPES.put("hive", strings(farm, "hiveValidBiomeTypes", List.of("JUNGLE", "CONIFEROUS", "SWAMP", "FOREST", "PLAINS"), "Legacy biome categories valid for hives"));
        addFood(farm, "chicken", List.of("minecraft:wheat_seeds", "minecraft:melon_seeds", "minecraft:beetroot_seeds", "minecraft:pumpkin_seeds", "simplecorn:corncob", "biomesoplenty:turnip_seeds", "harvestcraft:cornitem"));
        addFood(farm, "cow", List.of("minecraft:wheat", "simplecorn:corncob", "harvestcraft:barleyitem", "harvestcraft:oatsitem", "harvestcraft:ryeitem", "harvestcraft:cornitem"));
        addFood(farm, "goat", List.of("minecraft:wheat", "minecraft:string", "minecraft:stick", "minecraft:apple", "simplecorn:corncob", "harvestcraft:barleyitem", "harvestcraft:oatsitem", "harvestcraft:ryeitem", "harvestcraft:cornitem"));
        addFood(farm, "horse", List.of("minecraft:wheat", "harvestcraft:barleyitem", "harvestcraft:oatsitem", "harvestcraft:ryeitem", "minecraft:apple", "minecraft:carrot"));
        addFood(farm, "sheep", List.of("minecraft:wheat", "harvestcraft:barleyitem", "harvestcraft:oatsitem", "harvestcraft:ryeitem"));
        addFood(farm, "pig", List.of("minecraft:carrot", "minecraft:beetroot", "minecraft:potato", "minecraft:poisonous_potato", "minecraft:bread"));
        for (String animal : List.of("chicken", "cow", "goat", "horse", "pig", "sheep")) {
            addBeds(farm, animal, "animania:block_straw", "minecraft:grass");
        }
        ROOSTERS_FIGHT = bool(farm, "roostersFight", false, "Roosters fight other roosters");

        farm.push("spawning_and_breeding");
        REPLACE_VANILLA_COWS = bool(farm, "replaceVanillaCows", true, "Remove naturally spawning vanilla cows and mooshrooms");
        REPLACE_VANILLA_PIGS = bool(farm, "replaceVanillaPigs", true, "Remove naturally spawning vanilla pigs");
        REPLACE_VANILLA_CHICKENS = bool(farm, "replaceVanillaChickens", true, "Remove naturally spawning vanilla chickens");
        REPLACE_VANILLA_SHEEP = bool(farm, "replaceVanillaSheep", true, "Remove naturally spawning vanilla sheep");
        REPLACE_VANILLA_HORSES = bool(farm, "replaceVanillaHorses", false, "Remove naturally spawning vanilla horses");
        addSpawnGroup(farm, "chickens", true, 9, 40, 2);
        addSpawnGroup(farm, "cows", true, 9, 40, 2);
        addSpawnGroup(farm, "pigs", true, 9, 40, 2);
        addSpawnGroup(farm, "horses", true, 8, 40, 2);
        addSpawnGroup(farm, "goats", true, 8, 40, 1);
        addSpawnGroup(farm, "sheep", true, 8, 40, 3);
        addBiome(farm, "chickenPlymouthRock", "MOUNTAIN");
        addBiome(farm, "chickenLeghorn", "PLAINS");
        addBiome(farm, "chickenOrpington", "JUNGLE", "SWAMP");
        addBiome(farm, "chickenWyandotte", "FOREST");
        addBiome(farm, "chickenRhodeIslandRed", "FOREST");
        addBiome(farm, "cowHolstein", "FOREST"); addBiome(farm, "cowFriesian", "PLAINS");
        addBiome(farm, "cowAngus", "JUNGLE", "MESA", "SWAMP"); addBiome(farm, "cowHereford", "MOUNTAIN", "HILLS");
        addBiome(farm, "cowHighland", "MOUNTAIN", "HILLS"); addBiome(farm, "cowJersey", "WASTELAND", "SWAMP");
        addBiome(farm, "cowLonghorn", "SAVANNA"); addBiome(farm, "cowMooshroom", "MUSHROOM", "MAGICAL");
        addBiome(farm, "draftHorse", "PLAINS", "SAVANNA", "MESA");
        addBiome(farm, "pigYorkshire", "PLAINS"); addBiome(farm, "pigOldSpot", "FOREST");
        addBiome(farm, "pigLargeBlack", "SWAMP", "DENSE"); addBiome(farm, "pigLargeWhite", "FOREST");
        addBiome(farm, "pigDuroc", "JUNGLE"); addBiome(farm, "pigHampshire", "MOUNTAIN", "HILLS");
        addBiome(farm, "goatAlpine", "MOUNTAIN", "HILLS"); addBiome(farm, "goatAngora", "PLAINS");
        addBiome(farm, "goatFainting", "PLAINS"); addBiome(farm, "goatKiko", "MOUNTAIN", "HILLS");
        addBiome(farm, "goatKinder", "SAVANNA", "MESA"); addBiome(farm, "goatNigerianDwarf", "SANDY");
        addBiome(farm, "goatPygmy", "SAVANNA", "MESA");
        addBiome(farm, "sheepDorset", "HILLS"); addBiome(farm, "sheepFriesian", "PLAINS");
        addBiome(farm, "sheepJacob", "FOREST"); addBiome(farm, "sheepMerino", "PLAINS");
        addBiome(farm, "sheepSuffolk", "SAVANNA", "MESA"); addBiome(farm, "sheepDorper", "SAVANNA");
        farm.pop(2);
        FARM_SPEC = farm.build();

        ModConfigSpec.Builder extra = new ModConfigSpec.Builder();
        extra.push("extra");
        REPLACE_VANILLA_RABBITS = bool(extra, "replaceVanillaRabbits", true, "Remove naturally spawning vanilla rabbits");
        HAMSTER_WHEEL_CAPACITY = integer(extra, "hamsterWheelCapacity", 200_000, 1_000, 20_000_000, "Hamster wheel energy capacity");
        HAMSTER_WHEEL_RF_GENERATION = integer(extra, "hamsterWheelRFGeneration", 20, 1, 100_000, "Energy generated per tick");
        HAMSTER_WHEEL_USE_TIME = integer(extra, "hamsterWheelUseTime", 2_000, 20, 72_000, "Ticks a hamster runs before needing food");
        addFood(extra, "ferret", List.of("minecraft:mutton", "minecraft:egg", "animania:brown_egg", "animania:peacock_egg_blue", "animania:peacock_egg_white", "animania:prime_mutton", "animania:prime_rabbit", "minecraft:rabbit", "minecraft:chicken", "animania:prime_chicken"));
        addFood(extra, "hamster", List.of("animania:hamster_food", "minecraft:wheat_seeds", "minecraft:melon_seeds", "minecraft:beetroot_seeds", "minecraft:pumpkin_seeds", "simplecorn:corncob", "biomesoplenty:turnip_seeds", "harvestcraft:cornitem", "minecraft:apple"));
        addFood(extra, "hedgehog", List.of("minecraft:carrot", "minecraft:beetroot", "minecraft:egg", "animania:brown_egg", "animania:peacock_egg_blue", "animania:peacock_egg_white", "animania:prime_mutton", "animania:prime_rabbit", "minecraft:rabbit", "minecraft:chicken", "animania:prime_chicken", "minecraft:apple"));
        addFood(extra, "peacock", List.of("minecraft:wheat_seeds", "minecraft:melon_seeds", "minecraft:beetroot_seeds", "minecraft:pumpkin_seeds", "simplecorn:corncob", "biomesoplenty:turnip_seeds", "harvestcraft:cornitem"));
        addFood(extra, "rabbit", List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:apple"));
        addBeds(extra, "ferret", "animania:block_straw", "minecraft:grass");
        addBeds(extra, "hamster", "animania:block_straw", "");
        addBeds(extra, "hedgehog", "animania:block_straw", "minecraft:grass");
        addBeds(extra, "peacock", "animania:block_straw", "minecraft:grass");
        addBeds(extra, "rabbit", "animania:block_straw", "minecraft:grass");
        extra.push("spawning_and_breeding");
        SPAWN_ENABLED.put("rodents", bool(extra, "spawnAnimaniaRodents", true, "Spawn Animania rodents in the world"));
        SPAWN_ENABLED.put("peacocks", bool(extra, "spawnAnimaniaPeacocks", true, "Spawn Animania peafowl in the world"));
        SPAWN_ENABLED.put("amphibians", bool(extra, "spawnAnimaniaAmphibians", true, "Spawn Animania amphibians in the world"));
        SPAWN_ENABLED.put("rabbits", bool(extra, "spawnAnimaniaRabbits", true, "Spawn Animania rabbits in the world"));
        FAMILY_COUNT.put("rabbits", integer(extra, "numberRabbitFamilies", 2, 1, 64, "Maximum rabbit family group size"));
        SPAWN_PROBABILITY.put("peacocks", integer(extra, "spawnProbabilityPeacocks", 8, 0, 1000, "Legacy spawn weight"));
        SPAWN_PROBABILITY.put("amphibians", integer(extra, "spawnProbabilityAmphibians", 8, 0, 1000, "Legacy spawn weight"));
        SPAWN_PROBABILITY.put("rabbits", integer(extra, "spawnProbabilityRabbits", 8, 0, 1000, "Legacy spawn weight"));
        SPAWN_LIMIT.put("peacocks", integer(extra, "spawnLimitPeacocks", 40, 0, 10_000, "Nearby spawn limit"));
        SPAWN_LIMIT.put("amphibians", integer(extra, "spawnLimitAmphibians", 40, 0, 10_000, "Nearby spawn limit"));
        SPAWN_LIMIT.put("rabbits", integer(extra, "spawnLimitRabbits", 40, 0, 10_000, "Nearby spawn limit"));
        SPAWN_PROBABILITY.put("hedgehogs", integer(extra, "spawnProbabilityHedgehogs", 8, 0, 1000, "Legacy spawn weight"));
        SPAWN_PROBABILITY.put("ferrets", integer(extra, "spawnProbabilityFerrets", 8, 0, 1000, "Legacy spawn weight"));
        SPAWN_PROBABILITY.put("hamsters", integer(extra, "spawnProbabilityHamsters", 8, 0, 1000, "Legacy spawn weight"));
        SPAWN_LIMIT.put("hedgehogs", integer(extra, "spawnLimitHedgehogs", 40, 0, 10_000, "Nearby spawn limit"));
        SPAWN_LIMIT.put("ferrets", integer(extra, "spawnLimitFerrets", 40, 0, 10_000, "Nearby spawn limit"));
        SPAWN_LIMIT.put("hamsters", integer(extra, "spawnLimitHamsters", 40, 0, 10_000, "Nearby spawn limit"));
        addBiome(extra, "toad", "SWAMP", "FOREST"); addBiome(extra, "frog", "SWAMP", "RIVER");
        addBiome(extra, "dartFrog", "JUNGLE", "FOREST"); addBiome(extra, "hamster", "BEACH", "SANDY");
        addBiome(extra, "ferretGray", "SAVANNA"); addBiome(extra, "ferretWhite", "SAVANNA");
        addBiome(extra, "hedgehog", "FOREST"); addBiome(extra, "hedgehogAlbino", "SWAMP");
        addBiome(extra, "rabbitCottontail", "FOREST"); addBiome(extra, "rabbitChinchilla", "SAVANNA");
        addBiome(extra, "rabbitDutch", "PLAINS"); addBiome(extra, "rabbitHavana", "MOUNTAIN", "HILLS");
        addBiome(extra, "rabbitJack", "SAVANNA", "SANDY"); addBiome(extra, "rabbitNewZealand", "FOREST");
        addBiome(extra, "rabbitRex", "SAVANNA"); addBiome(extra, "rabbitLop", "PLAINS", "FOREST");
        for (String color : List.of("Charcoal", "Opal", "Peach", "Purple", "Taupe", "Blue", "White")) {
            addBiome(extra, "peafowl" + color, "SWAMP", "JUNGLE");
        }
        extra.pop(2);
        EXTRA_SPEC = extra.build();

        ModConfigSpec.Builder pets = new ModConfigSpec.Builder();
        pets.push("catsdogs");
        addFood(pets, "cat", List.of("minecraft:fish"));
        addBeds(pets, "cat", "animania:cat_bed_1", "animania:cat_bed_2");
        SPAWN_LIMIT.put("cats", integer(pets, "spawnLimitCats", 20, 0, 10_000, "Nearby cat spawn limit"));
        SPAWN_LIMIT.put("dogs", integer(pets, "spawnLimitDogs", 20, 0, 10_000, "Nearby dog spawn limit"));
        SPAWN_PROBABILITY.put("cats", integer(pets, "spawnProbabilityCats", 4, 0, 1000, "Legacy cat spawn weight"));
        SPAWN_PROBABILITY.put("dogs", integer(pets, "spawnProbabilityDogs", 5, 0, 1000, "Legacy dog spawn weight"));
        FAMILY_COUNT.put("dogs", integer(pets, "numberDogFamilies", 2, 1, 64, "Maximum dog family group size"));
        FAMILY_COUNT.put("cats", integer(pets, "numberCatFamilies", 2, 1, 64, "Maximum cat family group size"));
        addFood(pets, "dog", List.of("listAllbeefraw"));
        FOOD_LISTS.put("petBowl", strings(pets, "petBowlFood", List.of("minecraft:fish", "listAllbeefraw", "animania:hamster_food"), "Items accepted by pet bowls"));
        addBeds(pets, "dog", "animania:dog_pillow", "animania:block_straw");
        addBiome(pets, "wolf", "MOUNTAIN", "FOREST", "SNOWY", "COLD");
        addBiome(pets, "fox", "FOREST", "SNOWY", "COLD");
        addBiome(pets, "ocelot", "HOT", "JUNGLE", "SAVANNA");
        REPLACE_VANILLA_WOLVES = bool(pets, "replaceVanillaWolves", true, "Replace naturally spawning vanilla wolves");
        REPLACE_VANILLA_OCELOTS = bool(pets, "replaceVanillaOcelots", true, "Replace naturally spawning vanilla ocelots");
        pets.pop();
        CATSDOGS_SPEC = pets.build();
    }

    private LegacyConfig() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, BASE_SPEC, "animania-server.toml");
        container.registerConfig(ModConfig.Type.SERVER, FARM_SPEC, "animania_farm-server.toml");
        container.registerConfig(ModConfig.Type.SERVER, EXTRA_SPEC, "animania_extra-server.toml");
        container.registerConfig(ModConfig.Type.SERVER, CATSDOGS_SPEC, "animania_cats_dogs-server.toml");
    }

    private static ModConfigSpec.BooleanValue bool(ModConfigSpec.Builder b, String key, boolean value, String comment) {
        return b.comment(comment).define(key, value);
    }
    private static ModConfigSpec.IntValue integer(ModConfigSpec.Builder b, String key, int value, int min, int max, String comment) {
        return b.comment(comment).defineInRange(key, value, min, max);
    }
    private static ModConfigSpec.DoubleValue decimal(ModConfigSpec.Builder b, String key, double value, double min, double max, String comment) {
        return b.comment(comment).defineInRange(key, value, min, max);
    }
    private static ModConfigSpec.ConfigValue<List<? extends String>> strings(ModConfigSpec.Builder b, String key, List<String> values, String comment) {
        return b.comment(comment).defineListAllowEmpty(key, values, value -> value instanceof String);
    }
    private static ModConfigSpec.ConfigValue<String> string(ModConfigSpec.Builder b, String key, String value, String comment) {
        return b.comment(comment).define(key, value);
    }
    private static void addFood(ModConfigSpec.Builder b, String animal, List<String> values) {
        FOOD_LISTS.put(animal, strings(b, animal + "Food", values, "Food items accepted by " + animal));
    }
    private static void addBeds(ModConfigSpec.Builder b, String animal, String preferred, String backup) {
        PREFERRED_BEDS.put(animal, string(b, animal + "Bed", preferred, "Preferred bed block for " + animal));
        BACKUP_BEDS.put(animal, string(b, animal + "Bed2", backup, "Backup bed block for " + animal));
    }
    private static void addSpawnGroup(ModConfigSpec.Builder b, String group, boolean enabled, int probability, int limit, int families) {
        String title = title(group);
        SPAWN_ENABLED.put(group, bool(b, "spawnAnimania" + title, enabled, "Spawn Animania " + group + " in the world"));
        SPAWN_PROBABILITY.put(group, integer(b, "spawnProbability" + title, probability, 0, 1000, "Legacy spawn weight"));
        SPAWN_LIMIT.put(group, integer(b, "spawnLimit" + title, limit, 0, 10_000, "Nearby spawn limit"));
        FAMILY_COUNT.put(group, integer(b, "number" + singularTitle(group) + "Families", families, 1, 64, "Maximum family group size"));
    }
    private static void addBiome(ModConfigSpec.Builder b, String key, String... types) {
        BIOME_TYPES.put(key, strings(b, key + "BiomeTypes", List.of(types), "Legacy biome categories for " + key));
    }
    private static String title(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
    private static String singularTitle(String value) {
        String singular = value.endsWith("ies") ? value.substring(0, value.length() - 3) + "y"
                : value.endsWith("s") ? value.substring(0, value.length() - 1) : value;
        return title(singular);
    }
}
