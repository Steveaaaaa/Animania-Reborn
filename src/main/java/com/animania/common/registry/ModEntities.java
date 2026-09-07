package com.animania.common.registry;

import com.animania.Animania;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.chicken.ChickenBreed;
import com.animania.farm.chicken.ChickenRole;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.CowBreed;
import com.animania.farm.livestock.FarmAnimalRole;
import com.animania.farm.livestock.GoatBreed;
import com.animania.farm.livestock.PigBreed;
import com.animania.farm.livestock.AnimaniaSheep;
import com.animania.farm.livestock.SheepBreed;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.vehicle.FarmVehicleEntity;
import com.animania.extra.amphibian.AnimaniaAmphibian;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.rabbit.RabbitBreed;
import com.animania.extra.rabbit.RabbitRole;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.peafowl.PeafowlBreed;
import com.animania.extra.peafowl.PeafowlRole;
import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.cat.CatBreed;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Animania.MOD_ID);

    private static final Map<ChickenRole, Map<ChickenBreed, RegistryObject<EntityType<AnimaniaChicken>>>> CHICKENS =
            new EnumMap<>(ChickenRole.class);
    public static final Map<String, RegistryObject<EntityType<AnimaniaChicken>>> ALL_CHICKENS =
            new LinkedHashMap<>();
    private static final Map<FarmAnimalRole, Map<CowBreed, RegistryObject<EntityType<AnimaniaCow>>>> COWS =
            new EnumMap<>(FarmAnimalRole.class);
    private static final Map<FarmAnimalRole, Map<GoatBreed, RegistryObject<EntityType<AnimaniaGoat>>>> GOATS =
            new EnumMap<>(FarmAnimalRole.class);
    private static final Map<FarmAnimalRole, Map<PigBreed, RegistryObject<EntityType<AnimaniaPig>>>> PIGS =
            new EnumMap<>(FarmAnimalRole.class);
    private static final Map<FarmAnimalRole, Map<SheepBreed, RegistryObject<EntityType<AnimaniaSheep>>>> SHEEP =
            new EnumMap<>(FarmAnimalRole.class);
    public static final Map<String, RegistryObject<EntityType<AnimaniaCow>>> ALL_COWS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<AnimaniaGoat>>> ALL_GOATS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<AnimaniaPig>>> ALL_PIGS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<AnimaniaSheep>>> ALL_SHEEP = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<AnimaniaHorse>>> ALL_HORSES = new LinkedHashMap<>();
    public static final RegistryObject<EntityType<FarmVehicleEntity>> CART = vehicle("cart", FarmVehicleEntity.Kind.CART, 2.0F, 1.2F);
    public static final RegistryObject<EntityType<FarmVehicleEntity>> WAGON = vehicle("wagon", FarmVehicleEntity.Kind.WAGON, 2.5F, 1.2F);
    public static final RegistryObject<EntityType<FarmVehicleEntity>> TILLER = vehicle("tiller", FarmVehicleEntity.Kind.TILLER, 2.0F, 1.2F);
    public static final Map<String, RegistryObject<EntityType<AnimaniaAmphibian>>> ALL_AMPHIBIANS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<AnimaniaRodent>>> ALL_RODENTS = new LinkedHashMap<>();
    private static final Map<RabbitRole, Map<RabbitBreed, RegistryObject<EntityType<AnimaniaRabbit>>>> RABBITS =
            new EnumMap<>(RabbitRole.class);
    public static final Map<String, RegistryObject<EntityType<AnimaniaRabbit>>> ALL_RABBITS = new LinkedHashMap<>();
    private static final Map<PeafowlRole, Map<PeafowlBreed, RegistryObject<EntityType<AnimaniaPeafowl>>>> PEAFOWL =
            new EnumMap<>(PeafowlRole.class);
    public static final Map<String, RegistryObject<EntityType<AnimaniaPeafowl>>> ALL_PEAFOWL = new LinkedHashMap<>();
    private static final Map<CatRole, Map<CatBreed, RegistryObject<EntityType<AnimaniaCat>>>> CATS =
            new EnumMap<>(CatRole.class);
    public static final Map<String, RegistryObject<EntityType<AnimaniaCat>>> ALL_CATS = new LinkedHashMap<>();
    private static final Map<DogRole, Map<DogBreed, RegistryObject<EntityType<AnimaniaDog>>>> DOGS =
            new EnumMap<>(DogRole.class);
    public static final Map<String, RegistryObject<EntityType<AnimaniaDog>>> ALL_DOGS = new LinkedHashMap<>();

    static {
        for (ChickenRole role : ChickenRole.values()) {
            CHICKENS.put(role, new EnumMap<>(ChickenBreed.class));
            for (ChickenBreed breed : ChickenBreed.values()) {
                registerChicken(role, breed);
            }
        }
        for (FarmAnimalRole role : FarmAnimalRole.values()) {
            COWS.put(role, new EnumMap<>(CowBreed.class));
            for (CowBreed breed : CowBreed.values()) registerCow(role, breed);
            GOATS.put(role, new EnumMap<>(GoatBreed.class));
            for (GoatBreed breed : GoatBreed.values()) registerGoat(role, breed);
            PIGS.put(role, new EnumMap<>(PigBreed.class));
            for (PigBreed breed : PigBreed.values()) registerPig(role, breed);
            SHEEP.put(role, new EnumMap<>(SheepBreed.class));
            for (SheepBreed breed : SheepBreed.values()) registerSheep(role, breed);
            registerHorse(role);
        }
        registerAmphibian("frog", AnimaniaAmphibian.Kind.FROG);
        registerAmphibian("dartfrog", AnimaniaAmphibian.Kind.DART_FROG);
        registerAmphibian("toad", AnimaniaAmphibian.Kind.TOAD);
        registerRodent("hamster", AnimaniaRodent.Kind.HAMSTER, 0.5F, 0.3F);
        registerRodent("hedgehog", AnimaniaRodent.Kind.HEDGEHOG, 0.5F, 0.5F);
        registerRodent("hedgehog_albino", AnimaniaRodent.Kind.HEDGEHOG_ALBINO, 0.5F, 0.5F);
        registerRodent("ferret_grey", AnimaniaRodent.Kind.FERRET_GREY, 0.75F, 0.4F);
        registerRodent("ferret_white", AnimaniaRodent.Kind.FERRET_WHITE, 0.75F, 0.4F);
        for (RabbitRole role : RabbitRole.values()) {
            RABBITS.put(role, new EnumMap<>(RabbitBreed.class));
            for (RabbitBreed breed : RabbitBreed.values()) registerRabbit(role, breed);
        }
        for (PeafowlRole role : PeafowlRole.values()) {
            PEAFOWL.put(role, new EnumMap<>(PeafowlBreed.class));
            for (PeafowlBreed breed : PeafowlBreed.values()) registerPeafowl(role, breed);
        }
        for (CatRole role : CatRole.values()) {
            CATS.put(role, new EnumMap<>(CatBreed.class));
            for (CatBreed breed : CatBreed.values()) registerCat(role, breed);
        }
        for (DogRole role : DogRole.values()) {
            DOGS.put(role, new EnumMap<>(DogBreed.class));
            for (DogBreed breed : DogBreed.values()) registerDog(role, breed);
        }
    }

    private ModEntities() {
    }

    private static void registerChicken(ChickenRole role, ChickenBreed breed) {
        String name = role.name().toLowerCase() + "_" + breed.getSerializedName();
        float width = role == ChickenRole.CHICK ? 0.30F : role == ChickenRole.HEN ? 0.50F : 0.60F;
        float height = role == ChickenRole.CHICK ? 0.35F : role == ChickenRole.HEN ? 0.70F : 0.80F;
        RegistryObject<EntityType<AnimaniaChicken>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaChicken::new, MobCategory.CREATURE)
                        .sized(width, height)
                        .clientTrackingRange(10)
                        .build(name));
        CHICKENS.get(role).put(breed, holder);
        ALL_CHICKENS.put(name, holder);
    }

    public static EntityType<AnimaniaChicken> chicken(ChickenRole role, ChickenBreed breed) {
        return CHICKENS.get(role).get(breed).get();
    }

    private static void registerCow(FarmAnimalRole role, CowBreed breed) {
        String prefix = role == FarmAnimalRole.YOUNG ? "calf" : role == FarmAnimalRole.FEMALE ? "cow" : "bull";
        String name = prefix + "_" + breed.getSerializedName();
        float width = role == FarmAnimalRole.YOUNG ? 0.70F : 1.40F;
        float height = role == FarmAnimalRole.YOUNG ? 0.90F : 1.80F;
        RegistryObject<EntityType<AnimaniaCow>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaCow::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(10).build(name));
        COWS.get(role).put(breed, holder);
        ALL_COWS.put(name, holder);
    }

    private static void registerGoat(FarmAnimalRole role, GoatBreed breed) {
        String prefix = role == FarmAnimalRole.YOUNG ? "kid" : role == FarmAnimalRole.FEMALE ? "doe" : "buck";
        String name = prefix + "_" + breed.getSerializedName();
        float size = role == FarmAnimalRole.YOUNG ? 0.55F : 0.90F;
        float height = role == FarmAnimalRole.YOUNG ? 0.65F : 1.30F;
        RegistryObject<EntityType<AnimaniaGoat>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaGoat::new, MobCategory.CREATURE)
                        .sized(size, height).clientTrackingRange(10).build(name));
        GOATS.get(role).put(breed, holder);
        ALL_GOATS.put(name, holder);
    }

    public static EntityType<AnimaniaCow> cow(FarmAnimalRole role, CowBreed breed) {
        return COWS.get(role).get(breed).get();
    }

    public static EntityType<AnimaniaGoat> goat(FarmAnimalRole role, GoatBreed breed) {
        return GOATS.get(role).get(breed).get();
    }

    private static void registerPig(FarmAnimalRole role, PigBreed breed) {
        String prefix = role == FarmAnimalRole.YOUNG ? "piglet" : role == FarmAnimalRole.FEMALE ? "sow" : "hog";
        String name = prefix + "_" + breed.getSerializedName();
        float width = role == FarmAnimalRole.YOUNG ? 0.45F : 0.90F;
        float height = role == FarmAnimalRole.YOUNG ? 0.45F : 0.90F;
        RegistryObject<EntityType<AnimaniaPig>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaPig::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(10).build(name));
        PIGS.get(role).put(breed, holder);
        ALL_PIGS.put(name, holder);
    }

    public static EntityType<AnimaniaPig> pig(FarmAnimalRole role, PigBreed breed) {
        return PIGS.get(role).get(breed).get();
    }

    private static void registerSheep(FarmAnimalRole role, SheepBreed breed) {
        String prefix = role == FarmAnimalRole.YOUNG ? "lamb" : role == FarmAnimalRole.FEMALE ? "ewe" : "ram";
        String name = prefix + "_" + breed.getSerializedName();
        float width = role == FarmAnimalRole.YOUNG ? 0.45F : 0.90F;
        float height = role == FarmAnimalRole.YOUNG ? 0.65F : 1.30F;
        RegistryObject<EntityType<AnimaniaSheep>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaSheep::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(10).build(name));
        SHEEP.get(role).put(breed, holder);
        ALL_SHEEP.put(name, holder);
    }

    public static EntityType<AnimaniaSheep> sheep(FarmAnimalRole role, SheepBreed breed) {
        return SHEEP.get(role).get(breed).get();
    }

    private static void registerHorse(FarmAnimalRole role) {
        String prefix = role == FarmAnimalRole.YOUNG ? "foal" : role == FarmAnimalRole.FEMALE ? "mare" : "stallion";
        String name = prefix + "_draft";
        float width = role == FarmAnimalRole.YOUNG ? 0.80F : 1.40F;
        float height = role == FarmAnimalRole.YOUNG ? 1.20F : 1.90F;
        RegistryObject<EntityType<AnimaniaHorse>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaHorse::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(10).build(name));
        ALL_HORSES.put(name, holder);
    }

    public static EntityType<AnimaniaHorse> horse(FarmAnimalRole role) {
        String prefix = role == FarmAnimalRole.YOUNG ? "foal" : role == FarmAnimalRole.FEMALE ? "mare" : "stallion";
        return ALL_HORSES.get(prefix + "_draft").get();
    }

    private static RegistryObject<EntityType<FarmVehicleEntity>> vehicle(
            String name, FarmVehicleEntity.Kind kind, float width, float height) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder
                .<FarmVehicleEntity>of((type, level) -> new FarmVehicleEntity(type, level, kind), MobCategory.MISC)
                .sized(width, height).clientTrackingRange(10).updateInterval(1).build(name));
    }

    private static void registerAmphibian(String name, AnimaniaAmphibian.Kind kind) {
        RegistryObject<EntityType<AnimaniaAmphibian>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.<AnimaniaAmphibian>of(
                                (type, level) -> new AnimaniaAmphibian(type, level, kind), MobCategory.CREATURE)
                        .sized(0.5F, 0.5F).clientTrackingRange(8).build(name));
        ALL_AMPHIBIANS.put(name, holder);
    }

    private static void registerRodent(String name, AnimaniaRodent.Kind kind, float width, float height) {
        RegistryObject<EntityType<AnimaniaRodent>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.<AnimaniaRodent>of(
                                (type, level) -> new AnimaniaRodent(type, level, kind), MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(8).build(name));
        ALL_RODENTS.put(name, holder);
    }

    private static void registerRabbit(RabbitRole role, RabbitBreed breed) {
        String name = role.name().toLowerCase() + "_" + breed.getSerializedName();
        float width = role == RabbitRole.KIT ? 0.35F : 0.7F;
        float height = role == RabbitRole.KIT ? 0.35F : 0.6F;
        RegistryObject<EntityType<AnimaniaRabbit>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaRabbit::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(8).build(name));
        RABBITS.get(role).put(breed, holder);
        ALL_RABBITS.put(name, holder);
    }

    public static EntityType<AnimaniaRabbit> rabbit(RabbitRole role, RabbitBreed breed) {
        return RABBITS.get(role).get(breed).get();
    }

    private static void registerPeafowl(PeafowlRole role, PeafowlBreed breed) {
        String name = role.name().toLowerCase() + "_" + breed.getSerializedName();
        float width = role == PeafowlRole.PEACHICK ? 0.45F : role == PeafowlRole.PEAHEN ? 0.6F : 0.8F;
        float height = role == PeafowlRole.PEACHICK ? 0.65F : role == PeafowlRole.PEAHEN ? 1.2F : 1.6F;
        RegistryObject<EntityType<AnimaniaPeafowl>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaPeafowl::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(10).build(name));
        PEAFOWL.get(role).put(breed, holder);
        ALL_PEAFOWL.put(name, holder);
    }

    public static EntityType<AnimaniaPeafowl> peafowl(PeafowlRole role, PeafowlBreed breed) {
        return PEAFOWL.get(role).get(breed).get();
    }

    private static void registerCat(CatRole role, CatBreed breed) {
        String name = role.prefix() + "_" + breed.getSerializedName();
        float width = role == CatRole.KITTEN ? 0.40F : 0.80F;
        float height = role == CatRole.KITTEN ? 0.40F : 0.80F;
        RegistryObject<EntityType<AnimaniaCat>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaCat::new, MobCategory.CREATURE)
                        .sized(width, height).clientTrackingRange(10).build(name));
        CATS.get(role).put(breed, holder);
        ALL_CATS.put(name, holder);
    }

    public static EntityType<AnimaniaCat> cat(CatRole role, CatBreed breed) {
        return CATS.get(role).get(breed).get();
    }

    private static void registerDog(DogRole role, DogBreed breed) {
        String name = role.prefix() + "_" + breed.getSerializedName();
        float breedWidth = switch (breed) {
            case CHIHUAHUA, POMERANIAN -> 0.45F;
            case GREAT_DANE -> 0.95F;
            default -> 0.75F;
        };
        float breedHeight = switch (breed) {
            case CHIHUAHUA, POMERANIAN -> 0.50F;
            case GREAT_DANE -> 1.15F;
            default -> 0.85F;
        };
        float scale = role == DogRole.PUPPY ? 0.55F : role == DogRole.FEMALE ? 0.90F : 1.0F;
        RegistryObject<EntityType<AnimaniaDog>> holder = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(AnimaniaDog::new, MobCategory.CREATURE)
                        .sized(breedWidth * scale, breedHeight * scale).clientTrackingRange(10).build(name));
        DOGS.get(role).put(breed, holder);
        ALL_DOGS.put(name, holder);
    }

    public static EntityType<AnimaniaDog> dog(DogRole role, DogBreed breed) {
        return DOGS.get(role).get(breed).get();
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
