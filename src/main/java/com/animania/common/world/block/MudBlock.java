package com.animania.common.world.block;

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
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public MudBlock(Properties properties) {
        super(properties);
    }

    /** FarmAddonInjectionHandler.mudParticleDisplay: eight mud fragments at entry. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        for (var pig : level.getEntitiesOfClass(com.animania.farm.livestock.AnimaniaPig.class,
                new net.minecraft.world.phys.AABB(pos).inflate(10))) {
            if (!pig.isMuddy() || pig.splashTimer() <= 0) continue;
            if (Math.abs((int) (pig.getX() - pos.getX())) >= 1
                    || Math.abs((int) (pig.getY() - pos.getY())) >= 1
                    || Math.abs((int) (pig.getZ() - pos.getZ())) >= 1) continue;
            for (int i = 0; i < 8; i++) {
                level.addParticle(new net.minecraft.core.particles.BlockParticleOption(
                                net.minecraft.core.particles.ParticleTypes.BLOCK, state),
                        pig.getX() + (random.nextFloat() - 0.5D) * pig.getBbWidth(),
                        pig.getBoundingBox().minY + 0.5D,
                        pig.getZ() + (random.nextFloat() - 0.5D) * pig.getBbWidth(),
                        4 * (random.nextFloat() - 0.5D), 0.5D, (random.nextFloat() - 0.5D) * 4);
            }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
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
