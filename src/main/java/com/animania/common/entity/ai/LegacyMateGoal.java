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

/** 200-tick courtship and persistent pairing behavior from Animania 1.12's GenericAIMate. */
public final class LegacyMateGoal extends Goal {
    private final PathfinderMob mover;
    private final Animal male;
    private final double speed;
    private Animal targetMate;
    private int courtshipTimer = 20;
    private int delayCounter;

    public LegacyMateGoal(PathfinderMob mover, Animal male, double speed) {
        this.mover = mover;
        this.male = male;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (++delayCounter <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (AnimalInformation.isSterilized(male) || male.getData(ModAttachments.SLEEPING)
                || male.isInWater() || AnimalInformation.gender(male) != AnimalInformation.Gender.MALE
                || LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get() && !LegacyAnimalNeeds.isInteracted(male)
                || LegacyConfig.FEED_TO_BREED.get() && !male.getData(ModAttachments.HAND_FED)
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
        return targetMate != null && targetMate.isAlive();
    }

    @Override
    public void start() {
        courtshipTimer = 200;
    }

    @Override
    public void stop() {
        if (targetMate != null) targetMate.getNavigation().stop();
        mover.getNavigation().stop();
        targetMate = null;
    }

    @Override
    public void tick() {
        if (targetMate == null) return;
        String assigned = targetMate.getData(ModAttachments.LAST_MATE);
        if ((!assigned.isEmpty() && !assigned.equals(male.getUUID().toString()))
                || !targetMate.getData(ModAttachments.FERTILE)) {
            stop();
            courtshipTimer = 200;
            return;
        }
        if (--courtshipTimer >= 0) {
            if (courtshipTimer % 20 == 0) {
                mover.getLookControl().setLookAt(targetMate, 10.0F, mover.getMaxHeadXRot());
                mover.getNavigation().moveTo(targetMate, speed);
                targetMate.getLookControl().setLookAt(male, 10.0F, targetMate.getMaxHeadXRot());
                targetMate.getNavigation().moveTo(male, speed);
            }
            if (male.distanceTo(targetMate) <= 1.8F && male.level() instanceof ServerLevel server) {
                targetMate.spawnChildFromBreeding(server, male);
                LegacyReproduction.conceived(targetMate);
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
        String mateId = male.getData(ModAttachments.LAST_MATE);
        if (LegacyConfig.MALES_MATE_MULTIPLE_FEMALES.get()) mateId = "";
        final String requiredMate = mateId;
        double radius = requiredMate.isEmpty() ? 8.0D : 5.0D;
        return male.level().getEntitiesOfClass(Animal.class, male.getBoundingBox().inflate(radius), female ->
                        female != male && female.getClass() == male.getClass()
                                && AnimalInformation.gender(female) == AnimalInformation.Gender.FEMALE
                                && (requiredMate.isEmpty() || female.getUUID().toString().equals(requiredMate))
                                && female.getData(ModAttachments.FERTILE)
                                && !female.getData(ModAttachments.SLEEPING)
                                && male.hasLineOfSight(female)
                                && LegacyBreedingRules.canMate(male, female)
                                && male.canMate(female))
                .stream().min(Comparator.comparingDouble(male::distanceToSqr)).orElse(null);
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
