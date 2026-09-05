package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.farm.world.block.NestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

/** Ferret/hedgehog nest raiding and hedgehog crop foraging from the Extra addon. */
public final class LegacyRaidNestGoal extends LegacySearchBlockGoal {
    private final AnimaniaRodent rodent;
    private int delay;

    public LegacyRaidNestGoal(AnimaniaRodent rodent) {
        super(rodent, 1.0D, DestinationOffsets.UP, 16);
        this.rodent = rodent;
    }

    @Override
    public boolean canUse() {
        int firingDelay = rodent.kind().isHedgehog() ? 60 : LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get();
        if (++delay < firingDelay) return false;
        delay = 0;
        if (LegacyAnimalNeeds.isFed(rodent) || rodent.getData(ModAttachments.SLEEPING)) return false;
        if (rodent.kind().isFerret() && rodent.getRandom().nextInt(100) == 0) return false;
        return searchForDestination();
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        if (isChickenNest(pos)) return true;
        return rodent.kind().isHedgehog() && isCrop(pos);
    }

    @Override
    protected boolean targetStillValid() {
        return seekingBlockPos != null && shouldMoveTo(seekingBlockPos)
                && !LegacyAnimalNeeds.isFed(rodent);
    }

    @Override
    protected void onArriveAtDestination() {
        if (seekingBlockPos == null) return;
        if (NestBlock.takeChickenEggForPredator(level, seekingBlockPos)) {
            LegacyAnimalNeeds.feed(rodent, false, false);
            LegacyAnimalNeeds.setWatered(rodent, true);
            rodent.setData(ModAttachments.EATING_TICKS, 80);
        } else if (rodent.kind().isHedgehog() && isCrop(seekingBlockPos)) {
            if (LegacyConfig.PLANTS_REMOVED_AFTER_EATING.get()) level.destroyBlock(seekingBlockPos, false);
            LegacyAnimalNeeds.feed(rodent, false, false);
            rodent.setData(ModAttachments.EATING_TICKS, 80);
        }
        delay = 0;
    }

    private boolean isChickenNest(BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(ModBlocks.NEST.get()) && state.getValue(NestBlock.EGGS) > 0
                && state.getValue(NestBlock.BREED).chickenBreed() != null;
    }

    private boolean isCrop(BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        return block == Blocks.CARROTS || block == Blocks.BEETROOTS || block == Blocks.POTATOES;
    }
}
