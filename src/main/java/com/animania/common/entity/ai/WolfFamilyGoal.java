package com.animania.common.entity.ai;

import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import java.util.Comparator;
import java.util.EnumSet;

/** Rejoins a known family member without assembling unrelated wolves into a pack. */
public final class WolfFamilyGoal extends Goal {
    private final AnimaniaDog wolf;
    private AnimaniaDog relative;
    private Path path;
    private int nextSearch, ticks;

    public WolfFamilyGoal(AnimaniaDog wolf) {
        this.wolf = wolf;
        nextSearch = wolf.tickCount + 100 + wolf.getRandom().nextInt(100);
        setFlags(EnumSet.of(Flag.MOVE));
    }

    private static boolean available(AnimaniaDog animal) {
        return animal.breed().isWolf() && animal.isAlive() && !animal.isBaby() && !animal.isTame()
                && !animal.isLeashed() && !animal.isPassenger() && !animal.isVehicle()
                && !animal.isOrderedToSit() && !animal.isInSittingPose()
                && animal.hurtTime == 0 && animal.getTarget() == null && !animal.isOnFire()
                && !animal.isInWaterOrBubble() && !animal.getData(ModAttachments.SLEEPING)
                && animal.getData(ModAttachments.EATING_TICKS) == 0
                && LegacyAnimalNeeds.isFed(animal) && LegacyAnimalNeeds.isWatered(animal)
                && !LegacySleepGoal.shouldSleepNow(animal) && !FarmHerdGoal.hasFoodLure(animal);
    }

    private boolean follows(AnimaniaDog other) {
        String parent = wolf.getData(ModAttachments.PARENT);
        String otherParent = other.getData(ModAttachments.PARENT);
        if (parent.equals(other.getUUID().toString())) return true;
        // Parents do not turn back to follow an adult child that is already following them.
        if (otherParent.equals(wolf.getUUID().toString())) return false;
        boolean siblings = !parent.isBlank() && parent.equals(otherParent);
        boolean mates = wolf.getData(ModAttachments.LAST_MATE).equals(other.getUUID().toString())
                && other.getData(ModAttachments.LAST_MATE).equals(wolf.getUUID().toString());
        // A stable tie-break prevents paired adults or siblings from chasing one another.
        return (siblings || mates) && wolf.getUUID().compareTo(other.getUUID()) > 0;
    }

    @Override public boolean canUse() {
        if (wolf.tickCount < nextSearch) return false;
        nextSearch = wolf.tickCount + 100 + wolf.getRandom().nextInt(100);
        if (!available(wolf)) return false;
        var relatives = wolf.level().getEntitiesOfClass(AnimaniaDog.class, wolf.getBoundingBox().inflate(16),
                other -> other != wolf && available(other) && follows(other) && wolf.hasLineOfSight(other)
                        && wolf.distanceToSqr(other) > 64 && wolf.distanceToSqr(other) < 256);
        relatives.sort(Comparator.comparingDouble(wolf::distanceToSqr));
        for (int i = 0; i < Math.min(4, relatives.size()); i++) {
            AnimaniaDog candidate = relatives.get(i);
            Path route = wolf.getNavigation().createPath(candidate, 1);
            if (route == null || !route.canReach()) continue;
            relative = candidate; path = route;
            return true;
        }
        return false;
    }

    @Override public void start() {
        ticks = 0;
        wolf.getNavigation().moveTo(path, 1.0);
        wolf.setData(ModAttachments.FARM_ACTIVITY, FarmActivityGoal.FAMILY_REGROUP);
        wolf.setData(ModAttachments.FARM_ACTIVITY_START, (int) wolf.level().getGameTime());
    }

    @Override public boolean canContinueToUse() {
        return ticks < 200 && available(wolf) && relative != null && available(relative) && follows(relative)
                && wolf.distanceToSqr(relative) > 16 && wolf.distanceToSqr(relative) < 400;
    }

    @Override public void tick() {
        if (++ticks % 20 == 0 && !wolf.getNavigation().moveTo(relative, 1.0)) ticks = 200;
    }

    @Override public void stop() {
        wolf.getNavigation().stop();
        wolf.setData(ModAttachments.FARM_ACTIVITY, 0);
        relative = null; path = null;
        nextSearch = wolf.tickCount + 200 + wolf.getRandom().nextInt(200);
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }
}
