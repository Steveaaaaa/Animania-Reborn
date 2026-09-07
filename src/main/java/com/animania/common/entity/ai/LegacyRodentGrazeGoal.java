package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallGrassBlock;

import java.util.EnumSet;

/** Eighty-tick local grazing animation used by rabbits, ferrets and hedgehogs. */
public final class LegacyRodentGrazeGoal extends Goal {
    private final PathfinderMob mover;
    private final Animal animal;
    private int eatingTimer;

    public LegacyRodentGrazeGoal(PathfinderMob mover, Animal animal) {
        this.mover = mover;
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (LegacyAnimalNeeds.isFed(animal) || ModAttachments.getData(animal, ModAttachments.SLEEPING)) return false;
        if (animal.getRandom().nextInt(animal.isBaby() ? 50 : 150) != 0) return false;
        BlockPos pos = animal.blockPosition();
        return animal.level().getBlockState(pos).is(Blocks.GRASS)
                || animal.level().getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
                || animal.level().getBlockState(pos.below()).is(Blocks.DIRT);
    }

    @Override
    public void start() {
        eatingTimer = 80;
        ModAttachments.setData(animal, ModAttachments.EATING_TICKS, 80);
        mover.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return eatingTimer > 0;
    }

    @Override
    public void tick() {
        eatingTimer = Math.max(0, eatingTimer - 1);
        if (eatingTimer != 4) return;
        BlockPos pos = animal.blockPosition();
        if (animal.level().getBlockState(pos).is(Blocks.GRASS)) {
            animal.level().destroyBlock(pos, false);
        }
        LegacyAnimalNeeds.setFed(animal, true);
    }

    @Override
    public void stop() {
        eatingTimer = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
