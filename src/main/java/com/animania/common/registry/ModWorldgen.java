package com.animania.common.registry;

import com.animania.Animania;
import com.animania.common.world.ConfiguredHiveRarityFilter;
import com.animania.common.world.LegacySpawnBiomeModifier;
import com.animania.common.world.WildHiveFeature;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Runtime-configurable world generation and spawn-list codecs. */
public final class ModWorldgen {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Animania.MOD_ID);
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Animania.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Animania.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> WILD_HIVE =
            FEATURES.register("wild_hive", () -> new WildHiveFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfiguredHiveRarityFilter>>
            CONFIGURED_HIVE_RARITY = PLACEMENT_MODIFIERS.register("configured_hive_rarity",
            () -> () -> ConfiguredHiveRarityFilter.CODEC);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<LegacySpawnBiomeModifier>>
            LEGACY_SPAWNS = BIOME_MODIFIERS.register("legacy_add_spawns", () -> LegacySpawnBiomeModifier.CODEC);

    private ModWorldgen() {
    }

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
        PLACEMENT_MODIFIERS.register(bus);
        BIOME_MODIFIERS.register(bus);
    }
}
