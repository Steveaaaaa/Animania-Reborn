package com.animania.common.registry;

import com.animania.Animania;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import com.animania.farm.dairy.MilkType;
import com.animania.farm.vehicle.VehicleItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.animania.extra.peafowl.PeafowlBreed;
import com.animania.extra.rodent.HamsterBallItem;
import com.animania.catsdogs.item.RandomPetEggItem;
import com.animania.common.item.AnimaniaManualItem;
import com.animania.common.item.AnimaniaSpawnEggItem;
import com.animania.common.item.BonusFoodItem;
import com.animania.common.item.CarvingKnifeItem;
import com.animania.common.item.ContainerFoodItem;
import com.animania.common.item.RandomAnimalEggItem;
import com.animania.farm.item.RidingCropItem;
import com.animania.farm.item.BrownEggItem;
import com.animania.catsdogs.cat.CatBreed;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import net.minecraft.world.item.DyeColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.LinkedHashMap;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Animania.MOD_ID);

    public static final DeferredItem<BlockItem> TROUGH = ITEMS.register(
            "trough",
            () -> new BlockItem(ModBlocks.TROUGH.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> NEST = ITEMS.register(
            "block_nest",
            () -> new BlockItem(ModBlocks.NEST.get(), new Item.Properties())
    );
    public static final DeferredItem<BlockItem> MUD = ITEMS.register("block_mud",
            () -> new BlockItem(ModBlocks.MUD.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> STRAW = blockItem("block_straw", ModBlocks.STRAW);
    public static final DeferredItem<BlockItem> SALT_LICK = ITEMS.register("salt_lick",
            () -> new BlockItem(ModBlocks.SALT_LICK.get(), new Item.Properties().durability(200)));
    public static final DeferredItem<BlockItem> HAMSTER_WHEEL = ITEMS.register("block_hamster_wheel",
            () -> new BlockItem(ModBlocks.HAMSTER_WHEEL.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> PET_BOWL = blockItem("pet_bowl", ModBlocks.PET_BOWL);
    public static final DeferredItem<BlockItem> HIVE = blockItem("bee_hive", ModBlocks.HIVE);
    public static final DeferredItem<BlockItem> WILD_HIVE = blockItem("wild_hive", ModBlocks.WILD_HIVE);
    public static final DeferredItem<BlockItem> CAT_BED_1 = blockItem("cat_bed_1", ModBlocks.CAT_BED_1);
    public static final DeferredItem<BlockItem> CAT_BED_2 = blockItem("cat_bed_2", ModBlocks.CAT_BED_2);
    public static final DeferredItem<BlockItem> CAT_TOWER = blockItem("cat_tower", ModBlocks.CAT_TOWER);
    public static final DeferredItem<BlockItem> DOG_HOUSE = blockItem("dog_house", ModBlocks.DOG_HOUSE);
    public static final DeferredItem<BlockItem> DOG_PILLOW = blockItem("dog_pillow", ModBlocks.DOG_PILLOW);
    public static final DeferredItem<BlockItem> LITTER_BOX = blockItem("litter_box", ModBlocks.LITTER_BOX);

    public static final DeferredItem<Item> BROWN_EGG = ITEMS.register(
            "brown_egg",
            () -> new BrownEggItem(new Item.Properties().stacksTo(16))
    );
    public static final DeferredItem<Item> RAW_PRIME_BEEF = rawFood("raw_prime_beef");
    public static final DeferredItem<Item> COOKED_PRIME_BEEF = effectFood("cooked_prime_beef", 12, 0.5F,
            new MobEffectInstance(MobEffects.HEAL, 1, 0));
    public static final DeferredItem<Item> RAW_PRIME_STEAK = rawFood("raw_prime_steak");
    public static final DeferredItem<Item> COOKED_PRIME_STEAK = effectFood("cooked_prime_steak", 8, 0.5F,
            new MobEffectInstance(MobEffects.HEAL, 1, 0));
    public static final DeferredItem<Item> RAW_PRIME_CHEVON = rawFood("raw_prime_chevon");
    public static final DeferredItem<Item> COOKED_PRIME_CHEVON = effectFood("cooked_prime_chevon", 10, 0.5F,
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
    public static final DeferredItem<Item> RAW_PRIME_PORK = rawFood("raw_prime_pork");
    public static final DeferredItem<Item> COOKED_PRIME_PORK = effectFood("cooked_prime_pork", 12, 0.5F,
            new MobEffectInstance(MobEffects.ABSORPTION, 3000, 0));
    public static final DeferredItem<Item> RAW_PRIME_BACON = rawFood("raw_prime_bacon");
    public static final DeferredItem<Item> COOKED_PRIME_BACON = effectFood("cooked_prime_bacon", 8, 0.5F,
            new MobEffectInstance(MobEffects.ABSORPTION, 1800, 0));
    public static final DeferredItem<Item> RAW_PRIME_CHICKEN = rawFood("raw_prime_chicken");
    public static final DeferredItem<Item> COOKED_PRIME_CHICKEN = effectFood("cooked_prime_chicken", 8, 0.5F,
            new MobEffectInstance(MobEffects.DIG_SPEED, 3000, 0));
    public static final DeferredItem<Item> RAW_CHEVON = rawFood("raw_chevon");
    public static final DeferredItem<Item> COOKED_CHEVON = effectFood("cooked_chevon", 5, 0.5F,
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
    public static final DeferredItem<Item> CHEESE_SANDWICH = ITEMS.register("cheese_sandwich",
            () -> new BonusFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationModifier(0.7F).build())));
    public static final DeferredItem<ContainerFoodItem> TRUFFLE_RISOTTO = ITEMS.register("truffle_risotto",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(12).saturationModifier(0.8F).build()), ContainerFoodItem.Container.BOWL));
    public static final DeferredItem<ContainerFoodItem> CHEVON_STEW = ITEMS.register("chevon_stew",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(12).saturationModifier(0.75F).build()), ContainerFoodItem.Container.BOWL));
    public static final DeferredItem<ContainerFoodItem> PEACOCK_PILAF = ITEMS.register("peacock_pilaf",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(14).saturationModifier(0.8F).build()), ContainerFoodItem.Container.BOWL));
    public static final DeferredItem<ContainerFoodItem> THREE_CHEESE_PASTA = ITEMS.register("three_cheese_pasta",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(12).saturationModifier(0.8F).build()), ContainerFoodItem.Container.BOWL));
    public static final DeferredItem<Item> TRUFFLE = food("truffle", 2, 0.7F);
    public static final DeferredItem<Item> PLAIN_OMELETTE = food("plain_omelette", 5, 0.6F);
    public static final DeferredItem<Item> CHEESE_OMELETTE = effectFood("cheese_omelette", 5, 0.7F,
            new MobEffectInstance(MobEffects.HEAL, 1, 1));
    public static final DeferredItem<Item> BACON_OMELETTE = effectFood("bacon_omelette", 5, 0.7F,
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
    public static final DeferredItem<Item> TRUFFLE_OMELETTE = effectFood("truffle_omelette", 5, 0.8F,
            new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
    public static final DeferredItem<Item> SUPER_OMELETTE = ITEMS.register("super_omelette", () -> new BonusFoodItem(
            new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.9F).build()),
            new MobEffectInstance(MobEffects.REGENERATION, 600, 1),
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1)));
    public static final DeferredItem<ContainerFoodItem> TRUFFLE_SOUP = ITEMS.register("truffle_soup",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(10).saturationModifier(0.6F).build()), ContainerFoodItem.Container.BOWL, false,
                    new MobEffectInstance(MobEffects.REGENERATION, 1200, 1)));
    public static final DeferredItem<Item> CHOCOLATE_TRUFFLE = ITEMS.register("chocolate_truffle",
            () -> new BonusFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6)
                    .saturationModifier(0.7F).alwaysEdible().build()),
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 3)));
    public static final DeferredItem<Item> RAW_PRIME_MUTTON = rawFood("raw_prime_mutton");
    public static final DeferredItem<Item> COOKED_PRIME_MUTTON = effectFood("cooked_prime_mutton", 12, 0.5F,
            new MobEffectInstance(MobEffects.HEAL, 1, 0));
    public static final DeferredItem<Item> RAW_HORSE = rawFood("raw_horse");
    public static final DeferredItem<Item> COOKED_HORSE = effectFood("cooked_horse", 12, 0.5F,
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
    public static final DeferredItem<Item> RAW_FROG_LEGS = rawFood("raw_frog_legs");
    public static final DeferredItem<Item> COOKED_FROG_LEGS = effectFood("cooked_frog_legs", 7, 0.5F,
            new MobEffectInstance(MobEffects.JUMP, 1200, 2));
    public static final DeferredItem<Item> RAW_PRIME_RABBIT = rawFood("raw_prime_rabbit");
    public static final DeferredItem<Item> COOKED_PRIME_RABBIT = effectFood("cooked_prime_rabbit", 8, 0.5F,
            new MobEffectInstance(MobEffects.JUMP, 600, 3));
    public static final DeferredItem<Item> PEACOCK_EGG_BLUE = ITEMS.registerSimpleItem("peacock_egg_blue",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> PEACOCK_EGG_WHITE = ITEMS.registerSimpleItem("peacock_egg_white",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> RAW_PEACOCK = rawFood("raw_peacock");
    public static final DeferredItem<Item> COOKED_PEACOCK = effectFood("cooked_peacock", 6, 0.5F,
            new MobEffectInstance(MobEffects.LUCK, 600, 0));
    public static final DeferredItem<Item> RAW_PRIME_PEACOCK = rawFood("raw_prime_peacock");
    public static final DeferredItem<Item> COOKED_PRIME_PEACOCK = effectFood("cooked_prime_peacock", 9, 0.5F,
            new MobEffectInstance(MobEffects.LUCK, 1200, 1));
    private static final Map<PeafowlBreed, DeferredItem<Item>> PEACOCK_FEATHERS = new EnumMap<>(PeafowlBreed.class);
    public static final DeferredItem<Item> HAMSTER_FOOD = ITEMS.registerSimpleItem("hamster_food");
    public static final DeferredItem<HamsterBallItem> HAMSTER_BALL_CLEAR = ITEMS.register("hamster_ball_clear",
            () -> new HamsterBallItem(null, new Item.Properties()));
    private static final Map<DyeColor, DeferredItem<HamsterBallItem>> HAMSTER_BALLS = new EnumMap<>(DyeColor.class);
    public static final DeferredItem<RandomPetEggItem> RANDOM_CAT_EGG = ITEMS.register("entity_egg_cat_random",
            () -> new RandomPetEggItem(RandomPetEggItem.Kind.CAT, new Item.Properties()));
    public static final DeferredItem<RandomPetEggItem> RANDOM_DOG_EGG = ITEMS.register("entity_egg_dog_random",
            () -> new RandomPetEggItem(RandomPetEggItem.Kind.DOG, new Item.Properties()));
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_ANIMAL_EGG = randomEgg("entity_egg_random", RandomAnimalEggItem.Kind.ALL);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_CHICKEN_EGG = randomEgg("entity_egg_chicken_random", RandomAnimalEggItem.Kind.CHICKEN);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_COW_EGG = randomEgg("entity_egg_cow_random", RandomAnimalEggItem.Kind.COW);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_GOAT_EGG = randomEgg("entity_egg_goat_random", RandomAnimalEggItem.Kind.GOAT);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_PIG_EGG = randomEgg("entity_egg_pig_random", RandomAnimalEggItem.Kind.PIG);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_SHEEP_EGG = randomEgg("entity_egg_sheep_random", RandomAnimalEggItem.Kind.SHEEP);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_RABBIT_EGG = randomEgg("entity_egg_rabbit_random", RandomAnimalEggItem.Kind.RABBIT);
    public static final DeferredItem<RandomAnimalEggItem> RANDOM_PEAFOWL_EGG = randomEgg("entity_egg_peacock_random", RandomAnimalEggItem.Kind.PEAFOWL);
    private static final Map<String, DeferredItem<Item>> CAT_SPAWN_EGGS = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<Item>> DOG_SPAWN_EGGS = new LinkedHashMap<>();
    public static final DeferredItem<Item> WHEEL = ITEMS.registerSimpleItem("wheel");
    public static final DeferredItem<Item> CART = ITEMS.register("item_cart",
            () -> new VehicleItem(ModEntities.CART, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> WAGON = ITEMS.register("item_wagon",
            () -> new VehicleItem(ModEntities.WAGON, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> TILLER = ITEMS.register("item_tiller",
            () -> new VehicleItem(ModEntities.TILLER, new Item.Properties().stacksTo(1)));
    private static final Map<MilkType, DeferredItem<BucketItem>> MILK_BUCKETS = new EnumMap<>(MilkType.class);
    private static final Map<MilkType, DeferredItem<BlockItem>> CHEESE_WHEELS = new EnumMap<>(MilkType.class);
    private static final Map<MilkType, DeferredItem<Item>> CHEESE_WEDGES = new EnumMap<>(MilkType.class);
    private static final Map<String, DeferredItem<BlockItem>> ANIMANIA_WOOL = new LinkedHashMap<>();
    public static final DeferredItem<BlockItem> CHEESE_MOLD = ITEMS.register("cheese_mold",
            () -> new BlockItem(ModBlocks.CHEESE_MOLD.get(), new Item.Properties()));
    public static final DeferredItem<BucketItem> HONEY_BUCKET = ITEMS.register("bucket_honey",
            () -> new BucketItem(ModFluids.HONEY.source(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> SLOP_BUCKET = ITEMS.register("bucket_slop",
            () -> new BucketItem(ModFluids.SLOP.source(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<ContainerFoodItem> HONEY_BOTTLE = ITEMS.register("honey_bottle",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(4).food(new FoodProperties.Builder()
                    .nutrition(10).saturationModifier(1.5F).build()), ContainerFoodItem.Container.BOTTLE, false,
                    new MobEffectInstance(MobEffects.REGENERATION, 100, 1)));
    public static final DeferredItem<ContainerFoodItem> MILK_BOTTLE = ITEMS.register("milk_bottle",
            () -> new ContainerFoodItem(new Item.Properties().stacksTo(4).food(new FoodProperties.Builder()
                    .nutrition(4).saturationModifier(1.0F).alwaysEdible().build()),
                    ContainerFoodItem.Container.BOTTLE, true));
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<CarvingKnifeItem> CARVING_KNIFE = ITEMS.register("carving_knife",
            () -> new CarvingKnifeItem(Tiers.IRON, new Item.Properties().durability(100)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 2, -2.4F))));
    public static final DeferredItem<RidingCropItem> RIDING_CROP = ITEMS.register("riding_crop",
            () -> new RidingCropItem(new Item.Properties().stacksTo(1).durability(100)));
    public static final DeferredItem<AnimaniaManualItem> MANUAL = ITEMS.register("animania_manual",
            () -> new AnimaniaManualItem(new Item.Properties().stacksTo(1)));

    static {
        for (MilkType type : MilkType.values()) {
            MILK_BUCKETS.put(type, ITEMS.register(type.getSerializedName() + "_bucket_milk",
                    () -> new BucketItem(ModFluids.milk(type).source(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))));
            CHEESE_WHEELS.put(type, ITEMS.register(type.getSerializedName() + "_cheese_wheel",
                    () -> new BlockItem(ModBlocks.cheeseBlock(type).get(), new Item.Properties())));
            MobEffectInstance cheeseEffect = type == MilkType.GOAT
                    ? new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0)
                    : new MobEffectInstance(MobEffects.HEAL, 1, type == MilkType.SHEEP ? 0 : 1);
            CHEESE_WEDGES.put(type, effectFood(type.getSerializedName() + "_cheese_wedge", 3, 0.9F,
                    cheeseEffect));
        }
        ModBlocks.animaniaWool().forEach((name, block) -> ANIMANIA_WOOL.put(name,
                blockItem("wool_" + name, block)));
        for (PeafowlBreed breed : PeafowlBreed.values()) {
            PEACOCK_FEATHERS.put(breed, ITEMS.registerSimpleItem(
                    breed.getSerializedName() + "_peacock_feather"));
        }
        for (DyeColor color : DyeColor.values()) {
            HAMSTER_BALLS.put(color, ITEMS.register("hamster_ball_" + color.getName(),
                    () -> new HamsterBallItem(color, new Item.Properties())));
        }
        ModEntities.ALL_CHICKENS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0xD7C59A, name.startsWith("rooster_") ? 0xA52A2A : 0xF3E4B3,
                        new Item.Properties())
        ));
        ModEntities.ALL_COWS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0x6B4A2B, name.startsWith("bull_") ? 0x3B2417 : 0xE8D9BF,
                        new Item.Properties())
        ));
        ModEntities.ALL_GOATS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0xD8D0BC, name.startsWith("buck_") ? 0x6F6557 : 0xF3EFE5,
                        new Item.Properties())
        ));
        ModEntities.ALL_PIGS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0xE8A0A8, name.startsWith("hog_") ? 0x7A4A3A : 0xF5C3C8,
                        new Item.Properties())
        ));
        ModEntities.ALL_SHEEP.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0xE7E2D3, name.startsWith("ram_") ? 0x5C5146 : 0xB8A98F,
                        new Item.Properties())
        ));
        ModEntities.ALL_HORSES.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0x833B1C, 0xC3C3C3, new Item.Properties())
        ));
        ModEntities.ALL_AMPHIBIANS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type,
                        name.equals("dartfrog") ? 0x236FCC : name.equals("toad") ? 0xD39E74 : 0x1C634C,
                        name.equals("dartfrog") ? 0xF2D13D : 0x563A25, new Item.Properties())
        ));
        ModEntities.ALL_RODENTS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type,
                        name.startsWith("ferret") ? 0x8B6B55 : name.startsWith("hedgehog") ? 0x69503A : 0xDECFB8,
                        name.endsWith("white") || name.endsWith("albino") ? 0xF4EEE2 : 0x594033,
                        new Item.Properties())
        ));
        ModEntities.ALL_RABBITS.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0xB9A58E,
                        name.startsWith("buck_") ? 0x5E4937 : name.startsWith("kit_") ? 0xE2D6C5 : 0x8C725B,
                        new Item.Properties())
        ));
        ModEntities.ALL_PEAFOWL.forEach((name, type) -> ITEMS.register(
                "entity_egg_" + name,
                () -> new AnimaniaSpawnEggItem(type, 0x466F65,
                        name.startsWith("peacock_") ? 0x234FA0 : name.startsWith("peachick_") ? 0xD4C69A : 0x88755C,
                        new Item.Properties())
        ));
        ModEntities.ALL_CATS.forEach((name, type) -> CAT_SPAWN_EGGS.put(name, ITEMS.register(
                "entity_egg_" + name, () -> new AnimaniaSpawnEggItem(type, 0x8B7355,
                        name.startsWith("kitten_") ? 0xD7C2A2 : name.startsWith("tom_") ? 0x4F4034 : 0xB89C7B,
                        new Item.Properties())
        )));
        ModEntities.ALL_DOGS.forEach((name, type) -> DOG_SPAWN_EGGS.put(name, ITEMS.register(
                "entity_egg_" + name, () -> new AnimaniaSpawnEggItem(type, 0x8E6A48,
                        name.startsWith("puppy_") ? 0xD9C2A2 : name.startsWith("male_") ? 0x3E3027 : 0xB59571,
                        new Item.Properties())
        )));
    }

    private ModItems() {
    }

    public static DeferredItem<BucketItem> milkBucket(MilkType type) {
        return MILK_BUCKETS.get(type);
    }

    public static DeferredItem<BlockItem> cheeseWheel(MilkType type) {
        return CHEESE_WHEELS.get(type);
    }

    public static DeferredItem<Item> cheeseWedge(MilkType type) {
        return CHEESE_WEDGES.get(type);
    }

    public static DeferredItem<Item> peacockFeather(PeafowlBreed breed) {
        return PEACOCK_FEATHERS.get(breed);
    }

    public static DeferredItem<BlockItem> animaniaWool(String type) {
        return ANIMANIA_WOOL.get(type);
    }

    public static DeferredItem<? extends HamsterBallItem> hamsterBall(int color) {
        return color < 0 ? HAMSTER_BALL_CLEAR : HAMSTER_BALLS.get(DyeColor.byId(color));
    }

    public static DeferredItem<Item> catSpawnEgg(CatRole role, CatBreed breed) {
        return CAT_SPAWN_EGGS.get(role.prefix() + "_" + breed.getSerializedName());
    }

    public static DeferredItem<Item> dogSpawnEgg(DogRole role, DogBreed breed) {
        return DOG_SPAWN_EGGS.get(role.prefix() + "_" + breed.getSerializedName());
    }

    private static DeferredItem<Item> food(String name, int nutrition, float saturation) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().food(
                new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build())));
    }

    private static DeferredItem<Item> rawFood(String name) {
        return effectFood(name, 1, 1.0F, new MobEffectInstance(MobEffects.CONFUSION, 200, 3));
    }

    private static DeferredItem<Item> effectFood(String name, int nutrition, float saturation,
                                                  MobEffectInstance effect) {
        return ITEMS.register(name, () -> new BonusFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition).saturationModifier(saturation).build()), effect));
    }

    private static DeferredItem<RandomAnimalEggItem> randomEgg(String name, RandomAnimalEggItem.Kind kind) {
        return ITEMS.register(name, () -> new RandomAnimalEggItem(kind, new Item.Properties()));
    }

    private static <T extends net.minecraft.world.level.block.Block> DeferredItem<BlockItem> blockItem(
            String name, net.neoforged.neoforge.registries.DeferredBlock<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
