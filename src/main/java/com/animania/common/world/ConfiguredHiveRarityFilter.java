package com.animania.common.world;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModWorldgen;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/** Replays the 1.12 decorator's {@code random.nextInt(200) < frequency} rule. */
public final class ConfiguredHiveRarityFilter extends PlacementFilter {
    public static final MapCodec<ConfiguredHiveRarityFilter> CODEC =
            MapCodec.unit(ConfiguredHiveRarityFilter::new);

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return LegacyConfig.HIVE_SPAWNING.get()
                && random.nextInt(200) < LegacyConfig.HIVE_SPAWNING_FREQUENCY.get();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldgen.CONFIGURED_HIVE_RARITY.get();
    }
}
