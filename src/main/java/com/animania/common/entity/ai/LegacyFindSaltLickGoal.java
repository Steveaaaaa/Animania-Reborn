package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.common.world.block.entity.SaltLickBlockEntity;
import com.animania.farm.livestock.AnimaniaPig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;

/** Makes an injured animal reach a salt lick before consuming a use. */
public final class LegacyFindSaltLickGoal extends LegacySearchBlockGoal {
    private final Animal consumer;
    private int delay;

    public LegacyFindSaltLickGoal(PathfinderMob mob, Animal consumer) {
        super(mob, 1.0D, DestinationOffsets.HORIZONTAL);
        this.consumer = consumer;
    }

    @Override
    public boolean canUse() {
        if (++delay <= LegacyConfig.SALT_LICK_TICK.get()) return false;
        if (consumer.getHealth() >= consumer.getMaxHealth() || consumer.isVehicle()
                || consumer.getData(ModAttachments.SLEEPING)
                || consumer instanceof AnimaniaPig pig && pig.isMuddy()) {
            delay = 0;
            return false;
        }
        if (consumer.getRandom().nextInt(3) != 0) return false;
        delay = 0;
        return searchForDestination();
    }

    @Override
    public boolean canContinueToUse() {
        return consumer.getHealth() < consumer.getMaxHealth() && super.canContinueToUse();
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.SALT_LICK.get())
                && level.getBlockEntity(pos) instanceof SaltLickBlockEntity lick && lick.usesLeft() > 0;
    }

    @Override
    protected boolean targetStillValid() {
        return seekingBlockPos != null && shouldMoveTo(seekingBlockPos);
    }

    @Override
    protected void onArriveAtDestination() {
        if (seekingBlockPos != null && level.getBlockEntity(seekingBlockPos) instanceof SaltLickBlockEntity lick) {
            consumer.setData(ModAttachments.EATING_TICKS, 40);
            lick.use(consumer);
            delay = 0;
        }
    }
}
