package com.animania.common.entity.ai;

import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.Comparator;
import java.util.EnumSet;

/** Sheep/goats follow the destination of a working German shepherd's path. */
public final class LegacyGetDogHerdedGoal extends Goal {
    private final PathfinderMob herdMover;
    private final Animal herdAnimal;
    private AnimaniaDog herder;

    public LegacyGetDogHerdedGoal(PathfinderMob herdMover, Animal herdAnimal) {
        this.herdMover = herdMover;
        this.herdAnimal = herdAnimal;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (herdAnimal.getData(ModAttachments.SLEEPING)) return false;
        herder = findHerder();
        return herder != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !herdAnimal.getData(ModAttachments.SLEEPING) && (herder = findHerder()) != null;
    }

    @Override
    public void tick() {
        if (herder == null || herder.getNavigation().getPath() == null) return;
        var destination = herder.getNavigation().getPath().getEndNode();
        if (destination == null) return;
        herdMover.getNavigation().moveTo(destination.x, destination.y,
                destination.z, 1.0D);
    }

    @Override
    public void stop() {
        herder = null;
    }

    private AnimaniaDog findHerder() {
        return herdAnimal.level().getEntitiesOfClass(AnimaniaDog.class, herdAnimal.getBoundingBox().inflate(10.0D),
                        dog -> dog.breed() == DogBreed.GERMAN_SHEPHERD && dog.role() != DogRole.PUPPY
                                && dog.isTame() && !dog.isInSittingPose()
                                && !dog.getData(ModAttachments.SLEEPING)
                                && dog.getNavigation().getPath() != null)
                .stream().min(Comparator.comparingDouble(herdAnimal::distanceToSqr)).orElse(null);
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
