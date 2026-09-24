package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import java.util.Comparator;
import java.util.EnumSet;

/** Adults rejoin a nearby herd without competing with a calf's maternal following. */
public final class FarmHerdGoal extends Goal {
    private final Animal animal;
    private Animal neighbour;
    private int nextSearch, ticks;
    public FarmHerdGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.of(Flag.MOVE));
    }
    private boolean free() {
        return !animal.isBaby() && animal.isAlive() && !animal.isOnFire() && !animal.isInWaterOrBubble()
                && (!(animal instanceof com.animania.modern.MountainGoat goat) || goat.isFemale()
                    && com.animania.common.entity.LegacyAnimalNeeds.isFed(goat)
                    && com.animania.common.entity.LegacyAnimalNeeds.isWatered(goat))
                && animal.hurtTime == 0 && animal.getTarget() == null
                && !animal.isLeashed() && !animal.isVehicle() && !animal.isPassenger()
                && !animal.getData(ModAttachments.SLEEPING) && animal.getData(ModAttachments.EATING_TICKS) == 0
                && animal.getData(ModAttachments.FARM_ACTIVITY) == 0
                && !LegacySleepGoal.shouldSleepNow(animal)
                && (!(animal instanceof com.animania.farm.livestock.AnimaniaHorse horse) || !horse.isPullingVehicle())
                && !hasFoodLure(animal);
    }
    public static boolean hasFoodLure(Animal animal) {
        return animal.level().players().stream().anyMatch(p -> !p.isSpectator()
                && p.distanceToSqr(animal) < 100 && (animal.isFood(p.getMainHandItem()) || animal.isFood(p.getOffhandItem())));
    }

    @Override public boolean canUse() {
        if (animal.tickCount < nextSearch) return false;
        nextSearch = animal.tickCount + 100 + animal.getRandom().nextInt(100);
        if (!free()) return false;
        neighbour = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(16),
                other -> other != animal && other.getClass() == animal.getClass() && !other.isBaby()
                        && other.isAlive() && (!(other instanceof com.animania.modern.MountainGoat goat) || goat.isFemale())
                        && animal.hasLineOfSight(other))
                .stream().min(Comparator.comparingDouble(animal::distanceToSqr)).orElse(null);
        return neighbour != null && animal.distanceToSqr(neighbour) > 64;
    }
    @Override public boolean canContinueToUse() {
        return free() && neighbour != null && neighbour.isAlive() && ticks < 200
                && animal.distanceToSqr(neighbour) > 16 && animal.distanceToSqr(neighbour) < 400;
    }
    private boolean moveToNeighbour() {
        if (animal instanceof com.animania.modern.MountainGoat) {
            var route = animal.getNavigation().createPath(neighbour, 1);
            if (route == null || !route.canReach()) return false;
            for (int i = 1; i < route.getNodeCount(); i++)
                if (Math.abs(route.getNode(i).y - route.getNode(i - 1).y) > 1) return false;
            return animal.getNavigation().moveTo(route, 0.9);
        }
        return animal.getNavigation().moveTo(neighbour, 0.9);
    }
    @Override public void start() { ticks = moveToNeighbour() ? 0 : 200; }
    @Override public void tick() {
        if (++ticks % 20 == 0 && !moveToNeighbour()) ticks = 200;
    }
    @Override public void stop() { neighbour = null; animal.getNavigation().stop(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
