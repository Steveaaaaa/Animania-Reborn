package com.animania.common.entity.ai;

import com.animania.common.entity.*;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;
import java.util.Comparator;

/** Short maternal visits; the offspring's recorded identity prevents adoption by proximity. */
public final class FamilyCareGoal extends Goal {
    private final Animal parent;
    private Animal child;
    private int searchDelay, elapsed, contact;
    public FamilyCareGoal(Animal parent) {
        this.parent = parent;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean available(Animal animal) {
        return animal.isAlive() && !animal.getData(ModAttachments.SLEEPING) && animal.hurtTime == 0
                && animal.getTarget() == null && !animal.isOnFire() && !animal.isInWater()
                && !animal.isLeashed() && !animal.isPassenger() && !animal.isVehicle()
                && !(animal instanceof TamableAnimal pet && pet.isOrderedToSit());
    }
    private boolean ownYoung(Animal young) {
        boolean mother = young.getData(ModAttachments.PARENT).equals(parent.getUUID().toString());
        boolean father = AnimalInformation.formsPairBond(parent)
                && young.getData(ModAttachments.FATHER).equals(parent.getUUID().toString());
        return young.isBaby() && young.getClass() == parent.getClass() && (mother || father)
                && available(young) && (FamilyLifecycle.bird(young) || FamilyLifecycle.milkDependent(young));
    }
    @Override public boolean canUse() {
        if (--searchDelay > 0) return false;
        searchDelay = 80 + parent.getRandom().nextInt(40);
        if (parent.isBaby() || !available(parent) || parent.getData(ModAttachments.CARE_COOLDOWN) > 0
                || !LegacyAnimalNeeds.isFed(parent) || !LegacyAnimalNeeds.isWatered(parent)) return false;
        if (AnimalInformation.gender(parent) != AnimalInformation.Gender.FEMALE
                && !AnimalInformation.formsPairBond(parent)) return false;
        child = parent.level().getEntitiesOfClass(Animal.class, parent.getBoundingBox().inflate(16),
                young -> ownYoung(young) && (!LegacyAnimalNeeds.isFed(young)
                        || !LegacyAnimalNeeds.isWatered(young) || parent.distanceToSqr(young) > 36))
                .stream().min(Comparator.comparingDouble(parent::distanceToSqr)).orElse(null);
        return child != null;
    }
    @Override public boolean canContinueToUse() {
        return child != null && elapsed < 240 && available(parent) && ownYoung(child)
                && parent.distanceToSqr(child) < 400 && LegacyAnimalNeeds.isFed(parent)
                && LegacyAnimalNeeds.isWatered(parent);
    }
    @Override public void start() { elapsed = contact = 0; }
    @Override public void tick() {
        elapsed++;
        parent.getLookControl().setLookAt(child, 10, parent.getMaxHeadXRot());
        double reach = Math.max(1.5, (parent.getBbWidth() + child.getBbWidth()) * .5 + .7);
        if (parent.distanceToSqr(child) > reach * reach || !parent.hasLineOfSight(child)) {
            contact = 0;
            FamilyAnimationState.clear(parent, FamilyAnimationState.NURSE);
            FamilyAnimationState.clear(parent, FamilyAnimationState.NUZZLE);
            FamilyAnimationState.clear(child, FamilyAnimationState.SUCKLE);
            if (elapsed % 20 == 1 && !parent.getNavigation().moveTo(child, 1.0)) elapsed = 240;
            return;
        }
        parent.getNavigation().stop();
        boolean nursing = FamilyLifecycle.milkDependent(child)
                && child.getData(ModAttachments.PARENT).equals(parent.getUUID().toString());
        FamilyAnimationState.set(parent, nursing ? FamilyAnimationState.NURSE : FamilyAnimationState.NUZZLE);
        if (nursing) FamilyAnimationState.set(child, FamilyAnimationState.SUCKLE);
        if (++contact < 60) return;
        // Only mothers nurse. Birds and fathers reunite with offspring without creating food.
        if (FamilyLifecycle.milkDependent(child)
                && child.getData(ModAttachments.PARENT).equals(parent.getUUID().toString())) {
            LegacyAnimalNeeds.feed(child, false, false);
            LegacyAnimalNeeds.water(child);
        }
        elapsed = 240;
    }
    @Override public void stop() {
        FamilyAnimationState.clear(parent, FamilyAnimationState.NURSE);
        FamilyAnimationState.clear(parent, FamilyAnimationState.NUZZLE);
        FamilyAnimationState.clear(child, FamilyAnimationState.SUCKLE);
        parent.getNavigation().stop(); child = null;
        parent.setData(ModAttachments.CARE_COOLDOWN, FamilyLifecycle.careInterval(parent));
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
