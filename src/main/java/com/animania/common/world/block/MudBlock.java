package com.animania.common.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MudBlock extends Block {
    public static final MapCodec<MudBlock> CODEC = simpleCodec(MudBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public MudBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * 0.2, motion.y, motion.z * 0.2);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * 0.2, motion.y, motion.z * 0.2);
        super.stepOn(level, pos, state, entity);
    }
}
