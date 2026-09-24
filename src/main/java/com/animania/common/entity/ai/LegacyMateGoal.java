package com.animania.common.entity.ai;

import com.animania.common.config.LegacyBreedingRules;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.entity.LegacyReproduction;
import com.animania.common.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.Comparator;
import java.util.EnumSet;

/** Courtship with species-specific pair bonds and rechecked breeding readiness. */
public final class LegacyMateGoal extends Goal {
    private final PathfinderMob mover;
    private final Animal male;
    private final double speed;
    private Animal targetMate;
    private int courtshipTimer = 20;
    private int delayCounter;
    private int closeTicks;

    public LegacyMateGoal(PathfinderMob mover, Animal male, double speed) {
        this.mover = mover;
        this.male = male;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (++delayCounter <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (LegacySleepGoal.shouldSleepNow(male) || AnimalInformation.isSterilized(male) || ModAttachments.getData(male, ModAttachments.SLEEPING)
                || male.isInWater() || AnimalInformation.gender(male) != AnimalInformation.Gender.MALE
                || LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get() && !LegacyAnimalNeeds.isInteracted(male)
                || LegacyConfig.FEED_TO_BREED.get() && !ModAttachments.getData(male, ModAttachments.HAND_FED)
                || !LegacyAnimalNeeds.isFed(male) || !LegacyAnimalNeeds.isWatered(male)) {
            delayCounter = 0;
            return false;
        }
        targetMate = findNearbyMate();
        if (targetMate != null && male.getRandom().nextInt(20) == 0) {
            delayCounter = 0;
            targetMate = null;
            return false;
        }
        return targetMate != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetMate != null && targetMate.isAlive() && male.isAlive()
                && !ModAttachments.getData(male, ModAttachments.SLEEPING) && !ModAttachments.getData(targetMate, ModAttachments.SLEEPING)
                && male.getTarget() == null && targetMate.getTarget() == null
                && male.hurtTime == 0 && targetMate.hurtTime == 0 && !LegacySleepGoal.shouldSleepNow(male)
                && !male.isLeashed() && !targetMate.isLeashed() && !male.isVehicle() && !targetMate.isVehicle()
                && !male.isOnFire() && !targetMate.isOnFire() && courtshipTimer >= 0
                && (com.animania.common.entity.FamilyLifecycle.bird(targetMate) || ModAttachments.getData(targetMate, ModAttachments.FERTILE)) && LegacyBreedingRules.canMate(male, targetMate)
                && (com.animania.common.entity.FamilyLifecycle.bird(male) || male.canMate(targetMate));
    }

    @Override
    public void start() {
        courtshipTimer = 400;
        closeTicks = 0;
    }

    @Override
    public void stop() {
        com.animania.common.entity.FamilyAnimationState.clear(male, com.animania.common.entity.FamilyAnimationState.COURT);
        mover.getNavigation().stop();
        targetMate = null;
    }

    @Override
    public void tick() {
        if (targetMate == null) return;
        String assigned = ModAttachments.getData(targetMate, ModAttachments.LAST_MATE);
        if ((AnimalInformation.formsPairBond(targetMate) && !assigned.isEmpty() && !assigned.equals(male.getUUID().toString()))
                || !(com.animania.common.entity.FamilyLifecycle.bird(targetMate) || ModAttachments.getData(targetMate, ModAttachments.FERTILE))) {
            stop();
            courtshipTimer = 200;
            return;
        }
        if (--courtshipTimer >= 0) {
            if (courtshipTimer % 20 == 0) {
                mover.getLookControl().setLookAt(targetMate, 10.0F, mover.getMaxHeadXRot());
                mover.getNavigation().moveTo(targetMate, speed);
            }
            if (male.distanceTo(targetMate) <= 1.8F && male.hasLineOfSight(targetMate)) closeTicks++;
            else closeTicks = 0;
            com.animania.common.entity.FamilyAnimationState.set(male, closeTicks > 0
                    ? com.animania.common.entity.FamilyAnimationState.COURT : 0);
            if (closeTicks >= com.animania.common.entity.FamilyLifecycle.courtshipTicks(male)
                    && male.level() instanceof ServerLevel server) {
                if (com.animania.common.entity.FamilyLifecycle.bird(male)) {
                    AnimalInformation.recordMating(targetMate, male);
                    targetMate.setAge(6000); male.setAge(6000);
                    server.broadcastEntityEvent(targetMate, (byte) 18);
                } else {
                    targetMate.spawnChildFromBreeding(server, male);
                    LegacyReproduction.conceived(targetMate);
                }
                courtshipTimer = 200;
                stop();
            }
        } else {
            courtshipTimer = 200;
            stop();
            delayCounter = -2000;
        }
    }

    private Animal findNearbyMate() {
        String mateId = ModAttachments.getData(male, ModAttachments.LAST_MATE);
        if (!AnimalInformation.formsPairBond(male)) mateId = "";
        final String requiredMate = mateId;
        double radius = requiredMate.isEmpty() ? 8.0D : 5.0D;
        return male.level().getEntitiesOfClass(Animal.class, male.getBoundingBox().inflate(radius), female ->
                        female != male && female.getClass() == male.getClass()
                                && AnimalInformation.gender(female) == AnimalInformation.Gender.FEMALE
                                && (requiredMate.isEmpty() || female.getUUID().toString().equals(requiredMate))
                                && (com.animania.common.entity.FamilyLifecycle.bird(female) || ModAttachments.getData(female, ModAttachments.FERTILE))
                                && !ModAttachments.getData(female, ModAttachments.SLEEPING)
                                && male.hasLineOfSight(female)
                                && LegacyBreedingRules.canMate(male, female)
                                && (com.animania.common.entity.FamilyLifecycle.bird(male) || male.canMate(female)))
                .stream().min(Comparator.comparingDouble(male::distanceToSqr)).orElse(null);
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
