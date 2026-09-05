package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.registry.ModEntities;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.AnimaniaSheep;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.extra.amphibian.AnimaniaAmphibian;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.dog.AnimaniaDog;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = Animania.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntityEvents {
    private ModEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        ModEntities.ALL_CHICKENS.values().forEach(type ->
                event.put(type.get(), AnimaniaChicken.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 2.0D).build()));
        ModEntities.ALL_COWS.values().forEach(type ->
                event.put(type.get(), AnimaniaCow.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 18.0)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 4.0D).build()));
        ModEntities.ALL_GOATS.values().forEach(type ->
                event.put(type.get(), AnimaniaGoat.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20.0).build()));
        ModEntities.ALL_PIGS.values().forEach(type ->
                event.put(type.get(), AnimaniaPig.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 16.0).build()));
        ModEntities.ALL_SHEEP.values().forEach(type ->
                event.put(type.get(), AnimaniaSheep.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 15.0).build()));
        ModEntities.ALL_HORSES.values().forEach(type ->
                event.put(type.get(), AnimaniaHorse.createBaseHorseAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20.0)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.285).build()));
        ModEntities.ALL_AMPHIBIANS.values().forEach(type ->
                event.put(type.get(), Frog.createAttributes().build()));
        ModEntities.ALL_RODENTS.values().forEach(type ->
                event.put(type.get(), AnimaniaRodent.createAttributes().build()));
        ModEntities.ALL_RABBITS.forEach((name, type) ->
                event.put(type.get(), AnimaniaRabbit.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                                name.startsWith("kit_") ? 3.0D : 9.0D)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                                name.startsWith("kit_") ? 0.315D : 0.265D).build()));
        ModEntities.ALL_PEAFOWL.values().forEach(type ->
                event.put(type.get(), AnimaniaPeafowl.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 7.0D)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.25D).build()));
        ModEntities.ALL_CATS.forEach((name, type) ->
                event.put(type.get(), AnimaniaCat.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                                name.startsWith("kitten_") ? 12.0D : 18.0D)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                                name.startsWith("kitten_") ? 0.315D : 0.30D).build()));
        ModEntities.ALL_DOGS.forEach((name, type) ->
                event.put(type.get(), AnimaniaDog.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                                name.startsWith("puppy_") ? 12.0D : 18.0D)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                                name.startsWith("puppy_") ? 0.315D : 0.30D).build()));
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        ModEntities.ALL_CHICKENS.forEach((name, type) -> {
            if (name.startsWith("hen_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_COWS.forEach((name, type) -> {
            if (name.startsWith("cow_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_GOATS.forEach((name, type) -> {
            if (name.startsWith("doe_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_PIGS.forEach((name, type) -> {
            if (name.startsWith("sow_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_SHEEP.forEach((name, type) -> {
            if (name.startsWith("ewe_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_HORSES.forEach((name, type) -> {
            if (name.startsWith("mare_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_AMPHIBIANS.values().forEach(type ->
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Frog::checkFrogSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE));
        ModEntities.ALL_RODENTS.forEach((name, type) ->
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        name.equals("hamster") ? Mob::checkMobSpawnRules : Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE));
        ModEntities.ALL_RABBITS.forEach((name, type) -> {
            if (name.startsWith("doe_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AnimaniaRabbit::checkSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_PEAFOWL.forEach((name, type) -> {
            if (name.startsWith("peahen_")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_CATS.forEach((name, type) -> {
            if (name.equals("queen_ocelot")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
        ModEntities.ALL_DOGS.forEach((name, type) -> {
            if (name.equals("female_fox") || name.equals("female_wolf")) {
                event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
    }
}
