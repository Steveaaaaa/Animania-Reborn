package com.animania.catsdogs.block.entity;

import com.animania.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render anchor for the six original CraftStudio pet props. */
public final class PetPropBlockEntity extends BlockEntity {
    public PetPropBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PET_PROP.get(), pos, state);
    }
}
