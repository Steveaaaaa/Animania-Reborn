package com.animania.common.entity.ai;

import com.animania.common.entity.*;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.world.block.entity.NestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Females revisit their own clutch without making old or player-placed eggs un hatchable. */
public final class BroodNestGoal extends Goal {
    private final Animal bird;
    private BlockPos nest;
    private int delay, elapsed;
    public BroodNestGoal(Animal bird) { this.bird = bird; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
    private boolean ready() {
        return !bird.isBaby() && AnimalInformation.gender(bird) == AnimalInformation.Gender.FEMALE
                && !bird.getData(ModAttachments.SLEEPING) && bird.getTarget() == null && bird.hurtTime == 0
                && !bird.isOnFire() && !bird.isInWater() && !bird.isLeashed() && !bird.isPassenger()
                && LegacyAnimalNeeds.isFed(bird) && LegacyAnimalNeeds.isWatered(bird)
                && !(bird instanceof com.animania.farm.chicken.AnimaniaChicken hen && hen.isLookingForNest())
                && !(bird instanceof com.animania.extra.peafowl.AnimaniaPeafowl hen && hen.isLookingForNest());
    }
    private boolean owned(BlockPos pos) {
        return bird.level().hasChunkAt(pos) && bird.level().getBlockEntity(pos) instanceof NestBlockEntity eggs
                && eggs.belongsTo(bird);
    }
    @Override public boolean canUse() {
        if (--delay > 0 || !ready()) return false;
        delay = 400 + bird.getRandom().nextInt(400);
        int attempts = 0;
        for (BlockPos pos : BlockPos.betweenClosed(bird.blockPosition().offset(-6, -2, -6), bird.blockPosition().offset(6, 2, 6))) {
            if (!owned(pos)) continue;
            var route = bird.getNavigation().createPath(pos, 0);
            if (route != null && route.canReach()) { nest = pos.immutable(); return true; }
            if (++attempts >= 4) break;
        }
        return false;
    }
    @Override public boolean canContinueToUse() { return elapsed < 400 && nest != null && owned(nest) && ready(); }
    @Override public void start() { elapsed = 0; }
    @Override public void tick() {
        elapsed++;
        if (nest.distToCenterSqr(bird.position()) < 1.4) {
            FamilyAnimationState.set(bird, FamilyAnimationState.BROOD);
            bird.getNavigation().stop();
            bird.getLookControl().setLookAt(nest.getX() + .5, nest.getY() + .4, nest.getZ() + .5, 10, 20);
        } else {
            FamilyAnimationState.clear(bird, FamilyAnimationState.BROOD);
            if (elapsed > 200 || elapsed % 20 == 1 && !bird.getNavigation().moveTo(nest.getX() + .5, nest.getY(), nest.getZ() + .5, 1)) elapsed = 400;
        }
    }
    @Override public void stop() { FamilyAnimationState.clear(bird, FamilyAnimationState.BROOD); bird.getNavigation().stop(); nest = null; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
