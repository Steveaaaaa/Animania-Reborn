package com.animania.common.entity.ai;

import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.AnimaniaSheep;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/** Follow the recorded mother only; vanilla's class-only search mixes Animania breeds. */
public final class LegacyFollowParentGoal extends Goal {
    private final PathfinderMob pathfinder;
    private final Animal child;
    private final double speed;
    @Nullable private Animal parent;
    private int firingDelay;
    private int recalcPath;

    public LegacyFollowParentGoal(PathfinderMob pathfinder, Animal child, double speed) {
        this.pathfinder = pathfinder;
        this.child = child;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    public static boolean supports(Animal animal) {
        return animal instanceof AnimaniaCow || animal instanceof AnimaniaGoat
                || animal instanceof AnimaniaHorse || animal instanceof AnimaniaPig
                || animal instanceof AnimaniaSheep || animal instanceof AnimaniaRabbit
                || animal instanceof AnimaniaCat || animal instanceof AnimaniaDog;
    }

    @Override
    public boolean canUse() {
        if (++firingDelay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        firingDelay = 0;
        if (!child.isBaby() || !(child.level() instanceof ServerLevel server)) return false;
        if (!child.level().isDay() || ModAttachments.getData(child, ModAttachments.SLEEPING)) return false;
        String parentId = ModAttachments.getData(child, ModAttachments.PARENT);
        if (parentId.isBlank()) return false;
        Entity found;
        try {
            found = server.getEntity(UUID.fromString(parentId));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        if (!(found instanceof Animal animal) || !animal.isAlive() || animal.isBaby()
                || AnimalInformation.gender(animal) != AnimalInformation.Gender.FEMALE
                || !sameBreed(child, animal)) return false;
        double distance = child.distanceToSqr(animal);
        if (distance < 9.0D || distance > 256.0D) return false;
        parent = animal;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!child.isBaby() || parent == null || !parent.isAlive() || !sameBreed(child, parent)) return false;
        double distance = child.distanceToSqr(parent);
        return distance >= 9.0D && distance <= 256.0D;
    }

    @Override
    public void start() {
        recalcPath = 0;
    }

    @Override
    public void stop() {
        parent = null;
        pathfinder.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (parent != null && --recalcPath <= 0) {
            recalcPath = adjustedTickDelay(40);
            pathfinder.getNavigation().moveTo(parent, speed);
        }
    }

    private static boolean sameBreed(Animal child, Animal parent) {
        if (child instanceof AnimaniaCow a && parent instanceof AnimaniaCow b) return a.breed() == b.breed();
        if (child instanceof AnimaniaGoat a && parent instanceof AnimaniaGoat b) return a.breed() == b.breed();
        if (child instanceof AnimaniaPig a && parent instanceof AnimaniaPig b) return a.breed() == b.breed();
        if (child instanceof AnimaniaSheep a && parent instanceof AnimaniaSheep b) return a.breed() == b.breed();
        if (child instanceof AnimaniaRabbit a && parent instanceof AnimaniaRabbit b) return a.breed() == b.breed();
        if (child instanceof AnimaniaCat a && parent instanceof AnimaniaCat b) return a.breed() == b.breed();
        if (child instanceof AnimaniaDog a && parent instanceof AnimaniaDog b) return a.breed() == b.breed();
        return child instanceof AnimaniaHorse && parent instanceof AnimaniaHorse;
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
