package com.animania.common.entity.ai;

import com.animania.common.entity.FamilyAnimationState;
import com.animania.common.entity.FamilyLifecycle;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Keep contact while nursing, but yield to panic, swimming and player handling. */
public final class NursingPauseGoal extends Goal {
    private final Animal child;
    public NursingPauseGoal(Animal child) { this.child = child; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
    @Override public boolean canUse() {
        return child.getData(ModAttachments.FAMILY_POSE) == FamilyAnimationState.SUCKLE
                && child.isBaby() && !child.getData(ModAttachments.SLEEPING) && child.hurtTime == 0
                && child.getTarget() == null && !child.isOnFire() && !child.isInWater()
                && !child.isLeashed() && !child.isPassenger() && !child.isVehicle()
                && !(child instanceof net.minecraft.world.entity.TamableAnimal pet && pet.isOrderedToSit());
    }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public void start() { child.getNavigation().stop(); }
    @Override public void tick() {
        Animal mother = FamilyLifecycle.mother(child);
        if (mother != null) child.getLookControl().setLookAt(mother.getX(), mother.getY() + mother.getBbHeight() * .45, mother.getZ(), 10, 20);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
