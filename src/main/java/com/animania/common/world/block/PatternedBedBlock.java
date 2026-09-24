package com.animania.common.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A vanilla-behaving bed whose quilt is rendered by ordinary block models. */
public final class PatternedBedBlock extends BedBlock {
    public PatternedBedBlock(DyeColor color, Properties properties) {
        super(color, properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return null; }
}
