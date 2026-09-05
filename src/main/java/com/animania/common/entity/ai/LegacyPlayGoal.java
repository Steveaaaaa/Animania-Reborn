package com.animania.common.entity.ai;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

/** Paired chase-and-flee play behavior used by kittens and puppies. */
public final class LegacyPlayGoal extends Goal {
    private static final Map<Animal, LegacyPlayGoal> GOALS = new WeakHashMap<>();
    private final PathfinderMob mover;
    private final Animal child;
    private Animal playmate;
    private boolean running;
    private boolean chaser;

    public LegacyPlayGoal(PathfinderMob mover, Animal child) {
        this.mover = mover;
        this.child = child;
        GOALS.put(child, this);
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (running) return true;
        if (child.getData(ModAttachments.SLEEPING)) return false;
        playmate = child.level().getEntitiesOfClass(Animal.class, child.getBoundingBox().inflate(5.0D), other -> {
                    LegacyPlayGoal goal = GOALS.get(other);
                    return other != child && other.getClass() == child.getClass()
                            && AnimalInformation.gender(other) == AnimalInformation.Gender.YOUNG
                            && !other.getData(ModAttachments.SLEEPING) && goal != null && !goal.running;
                }).stream().min(Comparator.comparingDouble(child::distanceToSqr)).orElse(null);
        return playmate != null && child.getRandom().nextDouble() < 0.2D;
    }

    @Override
    public void start() {
        LegacyPlayGoal other = GOALS.get(playmate);
        if (other == null) return;
        running = true;
        chaser = true;
        other.playmate = child;
        other.running = true;
        other.chaser = false;
    }

    @Override
    public boolean canContinueToUse() {
        return running && playmate != null && playmate.isAlive()
                && !child.getData(ModAttachments.SLEEPING) && child.getRandom().nextDouble() >= 0.1D;
    }

    @Override
    public void tick() {
        if (!running || playmate == null) return;
        if (chaser) {
            mover.getNavigation().moveTo(playmate, 1.0D);
            if (child.distanceTo(playmate) <= 0.5F) {
                LegacyPlayGoal other = GOALS.get(playmate);
                if (other != null) other.chaser = true;
                chaser = false;
            }
        } else if (mover.getNavigation().isDone()) {
            Vec3 away = child.position().subtract(playmate.position());
            if (away.lengthSqr() < 0.01D) away = new Vec3(child.getRandom().nextDouble() - 0.5D, 0,
                    child.getRandom().nextDouble() - 0.5D);
            away = away.normalize().scale(8.0D);
            mover.getNavigation().moveTo(child.getX() + away.x, child.getY(), child.getZ() + away.z, 1.0D);
        }
    }

    @Override
    public void stop() {
        Animal oldMate = playmate;
        running = false;
        chaser = false;
        playmate = null;
        mover.getNavigation().stop();
        LegacyPlayGoal other = GOALS.get(oldMate);
        if (other != null && other.playmate == child) {
            other.running = false;
            other.chaser = false;
            other.playmate = null;
            other.mover.getNavigation().stop();
        }
    }
}
