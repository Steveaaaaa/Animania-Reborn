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
    private static final Map<Animal, LegacyPlayGoal> GOALS = new WeakHashMap<>();
    private final PathfinderMob mover;
    private final Animal child;
    private Animal playmate;
    private boolean running;
    private boolean chaser;
    private Path playPath;

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
        if (running) return; // The playmate already assigned our flee/chase role.
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
            if (playPath == null || mover.getNavigation().isDone())
                playPath = mover.getNavigation().createPath(playmate, 0);
            if (playPath != null) mover.getNavigation().moveTo(playPath, 1.0D);
            if (child.distanceTo(playmate) <= 0.5F) {
                LegacyPlayGoal other = GOALS.get(playmate);
                if (other != null) other.chaser = true;
                chaser = false;
                playPath = null;
                if (other != null) other.playPath = null;
            }
        } else if (mover.getNavigation().isDone()) {
            Vec3 away = DefaultRandomPos.getPosAway(mover, 16, 7, playmate.position());
            if (away != null) {
                playPath = mover.getNavigation().createPath(away.x, away.y, away.z, 0);
                if (playPath != null) mover.getNavigation().moveTo(playPath, 1.0D);
            }
        }
    }

    @Override
    public void stop() {
        Animal oldMate = playmate;
        running = false;
        chaser = false;
        playmate = null;
        playPath = null;
        mover.getNavigation().stop();
        LegacyPlayGoal other = GOALS.get(oldMate);
        if (other != null && other.playmate == child) {
            other.running = false;
            other.chaser = false;
            other.playmate = null;
            other.mover.getNavigation().stop();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
