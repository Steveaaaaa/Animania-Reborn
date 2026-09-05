package com.animania.common.world;

import com.animania.common.registry.ModBlocks;
import com.animania.farm.world.block.HiveBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Places wild hives on tree trunks instead of leaving them on the ground. */
public final class WildHiveFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] SIDES = {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    public WildHiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        // The old tree-decoration hook searched the current chunk around canopy
        // height and refused to place a second hive close to the same trunk.
        for (int attempt = 0; attempt < 196; attempt++) {
            int dx = random.nextInt(14) - 7;
            int dz = random.nextInt(14) - 7;
            int dy = random.nextInt(10) - 5;
            BlockPos log = origin.offset(dx, dy, dz);
            if (!level.getBlockState(log).is(BlockTags.LOGS)) continue;
            if (nearExistingHive(level, log)) return false;

            int offset = random.nextInt(SIDES.length);
            for (int i = 0; i < SIDES.length; i++) {
                Direction side = SIDES[(offset + i) % SIDES.length];
                BlockPos target = log.relative(side);
                if (!level.getBlockState(target).canBeReplaced()) continue;
                BlockState hive = ModBlocks.WILD_HIVE.get().defaultBlockState()
                        .setValue(HiveBlock.FACING, side);
                return level.setBlock(target, hive, 3);
            }
        }
        return false;
    }

    private static boolean nearExistingHive(WorldGenLevel level, BlockPos center) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    cursor.setWithOffset(center, x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(ModBlocks.WILD_HIVE.get()) || state.is(ModBlocks.HIVE.get())) return true;
                }
            }
        }
        return false;
    }
}
