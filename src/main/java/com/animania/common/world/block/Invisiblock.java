package com.animania.common.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Internal collision helper retained for parity with the original large animated props. */
public final class Invisiblock extends Block {
    public static final MapCodec<Invisiblock> CODEC = simpleCodec(Invisiblock::new);

    public Invisiblock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
