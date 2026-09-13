package com.animania.common.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A thin, non-colliding layer used by the original straw and spilled-seed blocks. */
public final class GroundCoverBlock extends Block {
    public static final MapCodec<GroundCoverBlock> CODEC = simpleCodec(GroundCoverBlock::new);
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty SEED =
            net.minecraft.world.level.block.state.properties.IntegerProperty.create("seed", 0, 3);
    public static int seedType(net.minecraft.world.item.ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.PUMPKIN_SEEDS) ? 1
                : stack.is(net.minecraft.world.item.Items.MELON_SEEDS) ? 2
                : stack.is(net.minecraft.world.item.Items.BEETROOT_SEEDS) ? 3 : 0;
    }
    public static net.minecraft.world.item.Item seedItem(BlockState state) {
        return switch (state.getValue(SEED)) {
            case 1 -> net.minecraft.world.item.Items.PUMPKIN_SEEDS;
            case 2 -> net.minecraft.world.item.Items.MELON_SEEDS;
            case 3 -> net.minecraft.world.item.Items.BEETROOT_SEEDS;
            default -> net.minecraft.world.item.Items.WHEAT_SEEDS;
        };
    }
    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEED);
    }
    @Override
    public net.minecraft.world.item.ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target,
            net.minecraft.world.level.LevelReader level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        if (this == com.animania.common.registry.ModBlocks.SEEDS.get()) return new net.minecraft.world.item.ItemStack(seedItem(state));
        return super.getCloneItemStack(state, target, level, pos, player);
    }
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public GroundCoverBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SEED, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
