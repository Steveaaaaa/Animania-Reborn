package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.CowBreed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Port of the food-producing branch of Animania 1.12's GenericAIEatGrass. */
public final class LegacyGrazeGoal extends LegacySearchBlockGoal {
    private final Animal grazer;
    private int firingTimer;
    private int eatingTimer;
    private final boolean consumesGrass;

    public LegacyGrazeGoal(PathfinderMob animal) {
        this(animal, true);
    }

    public LegacyGrazeGoal(PathfinderMob animal, boolean consumesGrass) {
        super(animal, 1.0D, DestinationOffsets.UP, 8);
        this.consumesGrass = consumesGrass;
        this.grazer = (Animal) animal;
        setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public void start() {
        if (consumesGrass) super.start();
        else {
            eatingTimer = 160;
            grazer.setData(ModAttachments.EATING_TICKS, 80);
            animal.getNavigation().stop();
        }
    }

    @Override
    public boolean canUse() {
        if (++firingTimer <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (grazer.getData(ModAttachments.SLEEPING) || LegacyAnimalNeeds.isFed(grazer)
                || grazer instanceof AnimaniaHorse horse && (horse.isVehicle() || horse.isPassenger() || horse.isPullingVehicle())) {
            firingTimer = 0;
            return false;
        }
        if (grazer.getRandom().nextInt(120) != 0) return false;
        firingTimer = 0;
        return searchForDestination();
    }

    @Override
    public boolean canContinueToUse() {
        return eatingTimer > 0 || super.canContinueToUse();
    }

    @Override
    public void tick() {
        firingTimer = 0;
        if (eatingTimer <= 0) {
            super.tick();
            return;
        }
        animal.getNavigation().stop();
        eatingTimer--;
        if (consumesGrass && eatingTimer == 4 && seekingBlockPos != null && shouldMoveTo(seekingBlockPos)) {
            var oldState = level.getBlockState(seekingBlockPos);
            level.levelEvent(2001, seekingBlockPos, Block.getId(oldState));
            if (LegacyConfig.PLANTS_REMOVED_AFTER_EATING.get()) {
                level.setBlock(seekingBlockPos, Blocks.DIRT.defaultBlockState(), 2);
            }
            LegacyAnimalNeeds.setFed(grazer, true);
        }
    }

    @Override
    public void stop() {
        super.stop();
        eatingTimer = 0;
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) return true;
        return grazer instanceof AnimaniaCow cow && cow.breed() == CowBreed.MOOSHROOM
                && level.getBlockState(pos).is(Blocks.MYCELIUM);
    }

    @Override
    protected boolean targetStillValid() {
        return eatingTimer > 0 || seekingBlockPos != null && shouldMoveTo(seekingBlockPos);
    }

    @Override
    protected void onArriveAtDestination() {
        eatingTimer = 160;
        grazer.setData(ModAttachments.EATING_TICKS, 80);
        animal.getNavigation().stop();
    }
}
