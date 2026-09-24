package com.animania.common.entity.ai;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

/** Paired chase-and-flee play behavior used by kittens and puppies. */
public final class LegacyPlayGoal extends Goal {
    private static final Map<Animal, java.lang.ref.WeakReference<LegacyPlayGoal>> GOALS = new WeakHashMap<>();
    private final PathfinderMob mover;
    private final Animal child;
    private Animal playmate;
    private boolean running;
    private boolean chaser;
    private Path playPath;
    private int remaining, nextAttempt, nextPlay;

    public LegacyPlayGoal(PathfinderMob mover, Animal child) {
        this.mover = mover;
        this.child = child;
        GOALS.put(child, new java.lang.ref.WeakReference<>(this));
        nextAttempt = child.tickCount + 100 + child.getRandom().nextInt(200);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (running) return available(child) && playmate != null && available(playmate);
        if (child.tickCount < nextAttempt || child.tickCount < nextPlay || !available(child)) return false;
        nextAttempt = child.tickCount + 100 + child.getRandom().nextInt(100);
        playmate = child.level().getEntitiesOfClass(Animal.class, child.getBoundingBox().inflate(5.0D), other -> {
                    LegacyPlayGoal goal = goalFor(other);
                    return other != child && samePlayGroup(child, other) && available(other)
                            && AnimalInformation.gender(other) == AnimalInformation.Gender.YOUNG
                            && !other.getData(ModAttachments.SLEEPING) && goal != null && !goal.running && other.tickCount >= goal.nextPlay;
                }).stream().min(Comparator.comparingDouble(child::distanceToSqr)).orElse(null);
        return playmate != null && child.getRandom().nextDouble() < 0.2D;
    }

    @Override
    public void start() {
        if (running) return; // The playmate already assigned our flee/chase role.
        LegacyPlayGoal other = goalFor(playmate);
        if (other == null) return;
        running = true;
        remaining = 200;
        other.remaining = 200;
        chaser = true;
        other.playmate = child;
        other.running = true;
        other.chaser = false;
    }

    @Override
    public boolean canContinueToUse() {
        return running && playmate != null && playmate.isAlive()
                && remaining > 0 && available(child) && available(playmate)
                && child.distanceToSqr(playmate) < 144;
    }

    @Override
    public void tick() {
        if (!running || playmate == null) return;
        remaining--;
        if (chaser) {
            if (playPath == null || mover.getNavigation().isDone())
                playPath = mover.getNavigation().createPath(playmate, 0);
            if (playPath != null) mover.getNavigation().moveTo(playPath, 1.0D);
            if (child.distanceTo(playmate) <= 0.5F) {
                LegacyPlayGoal other = goalFor(playmate);
                if (other != null) other.chaser = true;
                chaser = false;
                playPath = null;
                if (other != null) other.playPath = null;
            }
        } else if (mover.getNavigation().isDone()) {
            Vec3 away = DefaultRandomPos.getPosAway(mover, 5, 1, playmate.position());
            if (away != null) {
                playPath = mover.getNavigation().createPath(away.x, away.y, away.z, 0);
                if (playPath != null) mover.getNavigation().moveTo(playPath, 1.0D);
            }
        }
    }

    @Override
    public void stop() {
        Animal oldMate = playmate;
        nextPlay = nextAttempt = child.tickCount + 600 + child.getRandom().nextInt(600);
        running = false;
        chaser = false;
        playmate = null;
        playPath = null;
        mover.getNavigation().stop();
        LegacyPlayGoal other = goalFor(oldMate);
        if (other != null && other.playmate == child) {
            other.nextPlay = other.nextAttempt = oldMate.tickCount + 600 + oldMate.getRandom().nextInt(600);
            other.running = false;
            other.chaser = false;
            other.playmate = null;
            other.mover.getNavigation().stop();
        }
    }

    private static LegacyPlayGoal goalFor(Animal animal) {
        var reference = GOALS.get(animal);
        return reference == null ? null : reference.get();
    }

    private static boolean available(Animal animal) {
        if (!animal.isAlive() || animal.hurtTime > 0 || animal.getTarget() != null || animal.isOnFire()
                || animal.isInWaterOrBubble() || animal.isPassenger() || animal.isVehicle() || animal.isLeashed()
                || animal.getData(ModAttachments.SLEEPING) || LegacySleepGoal.shouldSleepNow(animal)
                || !com.animania.common.entity.LegacyAnimalNeeds.isFed(animal)
                || !com.animania.common.entity.LegacyAnimalNeeds.isWatered(animal)
                || FarmHerdGoal.hasFoodLure(animal)) return false;
        if (animal instanceof net.minecraft.world.entity.TamableAnimal tame) {
            if (tame.isOrderedToSit() || tame.isInSittingPose()) return false;
            var owner = tame.getOwner();
            if (owner != null && owner.distanceToSqr(animal) >= 25) return false;
        }
        return true;
    }

    private static boolean samePlayGroup(Animal first, Animal second) {
        if (first.getClass() != second.getClass()) return false;
        if (first instanceof com.animania.catsdogs.dog.AnimaniaDog a && second instanceof com.animania.catsdogs.dog.AnimaniaDog b)
            return a.breed().isWolf() == b.breed().isWolf()
                    && (a.breed() == com.animania.catsdogs.dog.DogBreed.FOX) == (b.breed() == com.animania.catsdogs.dog.DogBreed.FOX);
        if (first instanceof com.animania.catsdogs.cat.AnimaniaCat a && second instanceof com.animania.catsdogs.cat.AnimaniaCat b)
            return (a.breed() == com.animania.catsdogs.cat.CatBreed.OCELOT) == (b.breed() == com.animania.catsdogs.cat.CatBreed.OCELOT);
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
