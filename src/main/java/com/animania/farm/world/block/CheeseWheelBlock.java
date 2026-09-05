package com.animania.farm.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CheeseWheelBlock extends Block {
    public static final MapCodec<CheeseWheelBlock> CODEC = simpleCodec(CheeseWheelBlock::new);
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 8, 15);

    public CheeseWheelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!player.canEat(false)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            player.getFoodData().eat(2, 1.2F);
            int bites = state.getValue(BITES);
            if (bites < 3) level.setBlock(pos, state.setValue(BITES, bites + 1), UPDATE_ALL);
            else level.removeBlock(pos, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
