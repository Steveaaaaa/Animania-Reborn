package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyBiomeMatcher;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class NaturalSpawnConfigHandler {
    private NaturalSpawnConfigHandler() {}

    @SubscribeEvent
    public static void replaceStructureAnimal(MobSpawnEvent.FinalizeSpawn event) {
        EntityType<?> original = event.getEntity().getType();
        boolean villageCat = original == EntityType.CAT && LegacyConfig.REPLACE_VANILLA_OCELOTS.get();
        if (event.getSpawnType() != MobSpawnType.STRUCTURE
                && !(villageCat && (event.getSpawnType() == MobSpawnType.EVENT
                || event.getSpawnType() == MobSpawnType.NATURAL))) return;
        if (!villageCat && !replacedVanillaType(original) || event.getEntity().hasCustomName()) return;
        java.util.Map<String, ? extends java.util.function.Supplier<? extends EntityType<?>>> types;
        if (original == EntityType.COW || original == EntityType.MOOSHROOM) types = ModEntities.ALL_COWS;
        else if (original == EntityType.PIG) types = ModEntities.ALL_PIGS;
        else if (original == EntityType.SHEEP) types = ModEntities.ALL_SHEEP;
        else if (original == EntityType.CHICKEN) types = ModEntities.ALL_CHICKENS;
        else if (original == EntityType.HORSE) types = ModEntities.ALL_HORSES;
        else if (original == EntityType.RABBIT) types = ModEntities.ALL_RABBITS;
        else if (original == EntityType.WOLF) types = ModEntities.ALL_DOGS;
        else if (original == EntityType.OCELOT || villageCat) types = ModEntities.ALL_CATS;
        else return;
        java.util.List<EntityType<?>> candidates = new java.util.ArrayList<>();
        var biome = event.getLevel().getBiome(event.getEntity().blockPosition());
        for (var holder : types.values()) {
            EntityType<?> type = holder.get();
            String key = biomeConfigKey(type);
            String path = entityPath(type);
            if (villageCat) {
                if (!path.startsWith("queen_") || path.equals("queen_ocelot")) continue;
            } else {
                if (key == null) continue;
                if (original == EntityType.WOLF && !path.equals("female_wolf")) continue;
                if (original == EntityType.OCELOT && !path.equals("queen_ocelot")) continue;
                var filter = LegacyConfig.BIOME_TYPES.get(key);
                if (filter != null && !LegacyBiomeMatcher.matches(biome, filter.get())) continue;
            }
            String group = spawnGroup(type);
            if (LegacyConfig.SPAWN_ENABLED.containsKey(group) && !LegacyConfig.SPAWN_ENABLED.get(group).get()) continue;
            if (isFarm(type) && !AnimaniaConfig.ENABLE_FARM_SPAWNS.get()
                    || isExtra(type) && !AnimaniaConfig.ENABLE_EXTRA_SPAWNS.get()
                    || isPetWildlife(type) && !AnimaniaConfig.ENABLE_PET_WILDLIFE_SPAWNS.get()) continue;
            candidates.add(type);
        }
        if (candidates.isEmpty()) return;
        Entity replacement = candidates.get(event.getEntity().getRandom().nextInt(candidates.size()))
                .create(event.getLevel().getLevel());
        if (!(replacement instanceof net.minecraft.world.entity.Mob mob)) return;
        mob.moveTo(event.getX(), event.getY(), event.getZ(), event.getEntity().getYRot(), event.getEntity().getXRot());
        // Initialize one replacement without creating a second family inside the structure.
        mob.finalizeSpawn(event.getLevel(), event.getDifficulty(), MobSpawnType.CONVERSION, null, null);
        if (mob instanceof net.minecraft.world.entity.AgeableMob young
                && event.getEntity() instanceof net.minecraft.world.entity.AgeableMob originalAnimal)
            young.setAge(originalAnimal.getAge());
        mob.setPersistenceRequired();
        if (event.getLevel().addFreshEntity(mob)) event.setSpawnCancelled(true);
    }

    @SubscribeEvent
    public static void checkPlacement(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        EntityType<?> type = event.getEntityType();
        if (type == EntityType.SQUID && !LegacyConfig.SPAWN_FRESH_WATER_SQUIDS.get()
                && !event.getLevel().getBiome(event.getPos()).is(BiomeTags.IS_OCEAN)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }
        if (replacedVanillaType(type)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }

        String group = spawnGroup(type);
        if (group == null) return;
        boolean disabled = (!AnimaniaConfig.ENABLE_FARM_SPAWNS.get() && isFarm(type))
                || (!AnimaniaConfig.ENABLE_EXTRA_SPAWNS.get() && isExtra(type))
                || (!AnimaniaConfig.ENABLE_PET_WILDLIFE_SPAWNS.get() && isPetWildlife(type))
                || (LegacyConfig.SPAWN_ENABLED.containsKey(group) && !LegacyConfig.SPAWN_ENABLED.get(group).get());
        if (disabled || !allowedBiome(event, type) || overCap(event, group, type)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    private static boolean replacedVanillaType(EntityType<?> type) {
        return (type == EntityType.COW || type == EntityType.MOOSHROOM) && LegacyConfig.REPLACE_VANILLA_COWS.get()
                || type == EntityType.PIG && LegacyConfig.REPLACE_VANILLA_PIGS.get()
                || type == EntityType.CHICKEN && LegacyConfig.REPLACE_VANILLA_CHICKENS.get()
                || type == EntityType.SHEEP && LegacyConfig.REPLACE_VANILLA_SHEEP.get()
                || type == EntityType.HORSE && LegacyConfig.REPLACE_VANILLA_HORSES.get()
                || type == EntityType.RABBIT && LegacyConfig.REPLACE_VANILLA_RABBITS.get()
                || type == EntityType.WOLF && LegacyConfig.REPLACE_VANILLA_WOLVES.get()
                || type == EntityType.OCELOT && LegacyConfig.REPLACE_VANILLA_OCELOTS.get();
    }

    private static boolean allowedBiome(MobSpawnEvent.SpawnPlacementCheck event, EntityType<?> type) {
        String key = biomeConfigKey(type);
        var value = key == null ? null : LegacyConfig.BIOME_TYPES.get(key);
        return value == null || LegacyBiomeMatcher.matches(event.getLevel().getBiome(event.getPos()), value.get());
    }

    private static boolean overCap(MobSpawnEvent.SpawnPlacementCheck event, String group, EntityType<?> type) {
        String capGroup = capGroup(type);
        var cap = LegacyConfig.SPAWN_LIMIT.get(capGroup);
        if (cap == null) return false;
        if (cap.get() <= 0) return true;
        if (!(event.getLevel() instanceof ServerLevel server)) return false;
        int radius = LegacyConfig.ANIMAL_CAP_SEARCH_RANGE.get();
        AABB area = new AABB(event.getPos()).inflate(radius);
        int found = 0;
        for (Entity entity : server.getEntities((Entity) null, area,
                entity -> capGroup.equals(capGroup(entity.getType())))) {
            if (++found >= cap.get()) return true;
        }
        return false;
    }

    private static String capGroup(EntityType<?> type) {
        String group = spawnGroup(type);
        if (!"rodents".equals(group)) return group;
        String path = entityPath(type);
        return path.startsWith("hedgehog") ? "hedgehogs" : path.startsWith("ferret") ? "ferrets" : "hamsters";
    }

    private static String spawnGroup(EntityType<?> type) {
        if (contains(ModEntities.ALL_CHICKENS, type)) return "chickens";
        if (contains(ModEntities.ALL_COWS, type)) return "cows";
        if (contains(ModEntities.ALL_GOATS, type)) return "goats";
        if (contains(ModEntities.ALL_PIGS, type)) return "pigs";
        if (contains(ModEntities.ALL_SHEEP, type)) return "sheep";
        if (contains(ModEntities.ALL_HORSES, type)) return "horses";
        if (contains(ModEntities.ALL_AMPHIBIANS, type)) return "amphibians";
        if (contains(ModEntities.ALL_RODENTS, type)) return "rodents";
        if (contains(ModEntities.ALL_RABBITS, type)) return "rabbits";
        if (contains(ModEntities.ALL_PEAFOWL, type)) return "peacocks";
        if (contains(ModEntities.ALL_CATS, type)) return "cats";
        if (contains(ModEntities.ALL_DOGS, type)) return "dogs";
        return null;
    }

    private static String biomeConfigKey(EntityType<?> type) {
        String path = entityPath(type);
        String breed = path.substring(path.indexOf('_') + 1);
        if (path.startsWith("hen_")) return "chicken" + camel(breed);
        if (path.startsWith("cow_")) return "cow" + camel(breed);
        if (path.startsWith("mare_")) return "draftHorse";
        if (path.startsWith("sow_")) return "pig" + camel(breed);
        if (path.startsWith("doe_") && contains(ModEntities.ALL_GOATS, type)) return "goat" + camel(breed);
        if (path.startsWith("ewe_")) return "sheep" + camel(breed);
        if (path.equals("frog")) return "frog";
        if (path.equals("dartfrog")) return "dartFrog";
        if (path.equals("toad")) return "toad";
        if (path.equals("hamster")) return "hamster";
        if (path.equals("ferret_grey")) return "ferretGray";
        if (path.equals("ferret_white")) return "ferretWhite";
        if (path.equals("hedgehog")) return "hedgehog";
        if (path.equals("hedgehog_albino")) return "hedgehogAlbino";
        if (path.startsWith("doe_")) return "rabbit" + camel(breed);
        if (path.startsWith("peahen_") || path.startsWith("peacock_") || path.startsWith("peachick_")) {
            return "peafowl" + camel(breed);
        }
        if (path.equals("female_wolf")) return "wolf";
        if (path.equals("female_fox")) return "fox";
        if (path.equals("queen_ocelot")) return "ocelot";
        return null;
    }

    private static String entityPath(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
    }

    private static String camel(String value) {
        StringBuilder result = new StringBuilder();
        for (String part : value.split("_")) {
            if (!part.isEmpty()) result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private static boolean isFarm(EntityType<?> type) {
        return contains(ModEntities.ALL_CHICKENS, type) || contains(ModEntities.ALL_COWS, type)
                || contains(ModEntities.ALL_GOATS, type) || contains(ModEntities.ALL_PIGS, type)
                || contains(ModEntities.ALL_SHEEP, type) || contains(ModEntities.ALL_HORSES, type);
    }

    private static boolean isExtra(EntityType<?> type) {
        return contains(ModEntities.ALL_AMPHIBIANS, type) || contains(ModEntities.ALL_RODENTS, type)
                || contains(ModEntities.ALL_RABBITS, type) || contains(ModEntities.ALL_PEAFOWL, type);
    }

    private static boolean isPetWildlife(EntityType<?> type) {
        return contains(ModEntities.ALL_CATS, type) || contains(ModEntities.ALL_DOGS, type);
    }

    private static boolean contains(java.util.Map<String, ? extends java.util.function.Supplier<? extends EntityType<?>>> map,
                                    EntityType<?> type) {
        return map.values().stream().anyMatch(holder -> holder.get() == type);
    }
}
