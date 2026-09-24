package com.animania.common.world;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModWorldgen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.function.Function;

/**
 * Adds spawn entries while applying the old configurable weight and family-pack
 * limits when the biome registry is rebuilt.
 */
public record LegacySpawnBiomeModifier(HolderSet<Biome> biomes, List<SpawnerData> spawners,
                                       String probabilityGroup, int defaultProbability,
                                       String familyGroup) implements BiomeModifier {
    public static final MapCodec<LegacySpawnBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(LegacySpawnBiomeModifier::biomes),
            Codec.either(SpawnerData.CODEC.listOf(), SpawnerData.CODEC).xmap(
                    either -> either.map(Function.identity(), List::of),
                    list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list))
                    .fieldOf("spawners").forGetter(LegacySpawnBiomeModifier::spawners),
            Codec.STRING.fieldOf("probability_group").forGetter(LegacySpawnBiomeModifier::probabilityGroup),
            Codec.INT.fieldOf("default_probability").forGetter(LegacySpawnBiomeModifier::defaultProbability),
            Codec.STRING.optionalFieldOf("family_group", "").forGetter(LegacySpawnBiomeModifier::familyGroup)
    ).apply(builder, LegacySpawnBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        var probabilityValue = LegacyConfig.SPAWN_PROBABILITY.get(probabilityGroup);
        int configured = probabilityValue == null ? defaultProbability : probabilityValue.get();
        if (configured <= 0) return;

        long breedWeight = 0;
        for (SpawnerData source : spawners) {
            if (source.type == com.animania.common.registry.ModEntities.dog(
                    com.animania.catsdogs.dog.DogRole.FEMALE, com.animania.catsdogs.dog.DogBreed.WOLF)
                    && com.animania.catsdogs.dog.DogBreed.hasNewWolfHabitat(biome)) continue;
            int weight;
            if ("chickens".equals(probabilityGroup) || "pigs".equals(probabilityGroup)
                    || "rabbits".equals(probabilityGroup) || "cows".equals(probabilityGroup)
                    || "sheep".equals(probabilityGroup) || "ferrets".equals(probabilityGroup)) {
                // Divide the existing spawn budget between breeds without rounding every entry up.
                long before = breedWeight * configured / Math.max(1, defaultProbability);
                breedWeight += source.getWeight().asInt();
                weight = (int) Math.min(Integer.MAX_VALUE,
                        breedWeight * configured / Math.max(1, defaultProbability) - before);
                if (weight == 0) continue;
            } else if ("amphibians".equals(probabilityGroup)) {
                weight = configured + 10;
            } else {
                weight = Math.max(1, source.getWeight().asInt() * configured / Math.max(1, defaultProbability));
            }
            int maximum = source.maxCount;
            var familyValue = familyGroup.isEmpty() ? null : LegacyConfig.FAMILY_COUNT.get(familyGroup);
            if (familyValue != null) maximum = Math.max(2, Math.max(source.minCount, familyValue.get()));
            SpawnerData configuredSpawn = new SpawnerData(source.type, weight, source.minCount, maximum);
            builder.getMobSpawnSettings().addSpawn(source.type.getCategory(), configuredSpawn);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModWorldgen.LEGACY_SPAWNS.get();
    }
}
