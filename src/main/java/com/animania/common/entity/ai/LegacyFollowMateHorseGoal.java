package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.FarmAnimalRole;
import net.minecraft.world.entity.ai.goal.Goal;

/** Stallions stay near their persistently paired mare during the day. */
public final class LegacyFollowMateHorseGoal extends Goal {
    private final AnimaniaHorse stallion;
    private AnimaniaHorse mate;
    private int delay;

    public LegacyFollowMateHorseGoal(AnimaniaHorse stallion) {
        this.stallion = stallion;
    }

    @Override
    public boolean canUse() {
        if (++delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (!stallion.level().isDay() || stallion.getData(ModAttachments.SLEEPING)
                || stallion.role() != FarmAnimalRole.MALE) {
            delay = 0;
            return false;
        }
        String mateId = stallion.getData(ModAttachments.LAST_MATE);
        if (mateId.isEmpty()) return false;
        mate = stallion.level().getEntitiesOfClass(AnimaniaHorse.class, stallion.getBoundingBox().inflate(40.0D),
                horse -> horse.role() == FarmAnimalRole.FEMALE && horse.getUUID().toString().equals(mateId))
                .stream().findFirst().orElse(null);
        if (mate == null) return false;
        double dx = Math.abs(mate.getX() - stallion.getX());
        double dy = Math.abs(mate.getY() - stallion.getY());
        double dz = Math.abs(mate.getZ() - stallion.getZ());
        return dx <= 20.0D && dy <= 8.0D && dz <= 20.0D && dx >= 3.0D && dz >= 3.0D;
    }

    @Override
    public void start() {
        delay = 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (mate == null || !mate.isAlive()) return false;
        double distance = stallion.distanceToSqr(mate);
        return distance >= 9.0D && distance <= 256.0D;
    }

    @Override
    public void tick() {
        if (--delay <= 0) {
            delay = 60;
            stallion.getNavigation().moveTo(mate, 1.1D);
        }
    }

    @Override
    public void stop() {
        mate = null;
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
