package com.animania.common.entity.ai;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.entity.AnimalSocialMemory;
import com.animania.common.entity.HusbandryMood;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Voluntary contact with a familiar animal; never changes breeding partners or targets. */
public final class AnimalSocialGoal extends Goal {
    private final Animal animal;
    private Animal companion;
    private int nextAttempt, travel, remaining, activity;
    private boolean performing;

    public AnimalSocialGoal(Animal animal) {
        this.animal = animal;
        nextAttempt = animal.tickCount + 200 + animal.getRandom().nextInt(400);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean free(Animal a) {
        return HusbandryActivityGuard.free(a) && (a == animal && performing || a.getData(ModAttachments.FARM_ACTIVITY) == 0);
    }
    @Override public boolean canUse() {
        if (animal.tickCount < nextAttempt) return false;
        nextAttempt = animal.tickCount + 300 + animal.getRandom().nextInt(400);
        if (!AnimaniaConfig.ANIMAL_SOCIAL_BEHAVIOR.get() || !free(animal)) return false;
        var candidates = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(8),
                other -> AnimalSocialMemory.compatible(animal, other) && free(other)
                        && animal.hasLineOfSight(other) && AnimalSocialMemory.familiar(animal, other));
        candidates.sort(Comparator.<Animal>comparingInt(other -> -AnimalSocialMemory.familiarity(animal, other))
                .thenComparingDouble(animal::distanceToSqr));
        for (Animal other : candidates.subList(0, Math.min(4, candidates.size()))) {
            // Only one initiator in a pair; the recipient is free to leave.
            if (animal.getUUID().compareTo(other.getUUID()) < 0) continue;
            var path = animal.getNavigation().createPath(other, 1);
            if (path == null || !path.canReach()) continue;
            companion = other;
            return true;
        }
        return false;
    }
    @Override public void start() {
        travel = 0; remaining = 140; performing = false;
        activity = switch (HusbandryMood.kind(animal)) {
            case COW, HORSE, RABBIT, CAT -> FarmActivityGoal.SOCIAL_GROOM;
            case PIG, DOG, FERRET -> FarmActivityGoal.SOCIAL_SNIFF;
            default -> FarmActivityGoal.SOCIAL_REST;
        };
        approach();
    }
    private double gap() {
        return (animal.getBbWidth() + companion.getBbWidth()) * .5
                + (activity == FarmActivityGoal.SOCIAL_REST ? 1.1 : .3);
    }
    private void approach() {
        Vec3 side = new Vec3(Math.cos(Math.toRadians(companion.getYRot())), 0,
                Math.sin(Math.toRadians(companion.getYRot()))).scale(gap());
        Vec3 target = companion.position().add(side);
        if (!animal.getNavigation().moveTo(target.x, target.y, target.z, .8)) travel = 200;
    }
    @Override public boolean canContinueToUse() {
        return AnimaniaConfig.ANIMAL_SOCIAL_BEHAVIOR.get() && companion != null && free(animal) && free(companion)
                && travel < 200 && remaining > 0 && animal.distanceToSqr(companion) < 100
                && animal.hasLineOfSight(companion)
                && (!performing || companion.getDeltaMovement().horizontalDistanceSqr() < .006
                    && animal.distanceToSqr(companion) < (gap() + .65) * (gap() + .65));
    }
    @Override public void tick() {
        if (!performing) {
            if (++travel % 20 == 0) approach();
            double distance = animal.distanceToSqr(companion);
            if (distance <= (gap() + .25) * (gap() + .25) && Math.abs(animal.getY() - companion.getY()) < .6
                    && companion.getDeltaMovement().horizontalDistanceSqr() < .002) {
                performing = true;
                animal.getNavigation().stop();
                animal.setData(ModAttachments.FARM_ACTIVITY, activity);
                animal.setData(ModAttachments.FARM_ACTIVITY_START, (int) animal.level().getGameTime());
            }
            return;
        }
        remaining--;
        animal.getNavigation().stop();
        animal.getLookControl().setLookAt(companion.getX(), companion.getY() + companion.getBbHeight() * .75,
                companion.getZ(), 6, 12);
    }
    @Override public void stop() {
        if (performing && animal.getData(ModAttachments.FARM_ACTIVITY) == activity) animal.setData(ModAttachments.FARM_ACTIVITY, 0);
        animal.getNavigation().stop(); companion = null; performing = false;
        nextAttempt = animal.tickCount + 1200 + animal.getRandom().nextInt(1200);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
