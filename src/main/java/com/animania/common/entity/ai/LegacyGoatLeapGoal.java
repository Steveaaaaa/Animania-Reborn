package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaGoat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** The buck leap has a 0..4 squared-distance window, unlike vanilla LeapAtTargetGoal. */
public final class LegacyGoatLeapGoal extends Goal {
    private final AnimaniaGoat goat;
    private LivingEntity target;
    public LegacyGoatLeapGoal(AnimaniaGoat goat) { this.goat = goat; setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP)); }
    @Override public boolean canUse() {
        target = goat.getTarget();
        return !goat.getData(ModAttachments.SLEEPING) && goat.getData(ModAttachments.FIGHTING)
                && target != null && goat.distanceToSqr(target) <= 4.0D
                && goat.onGround() && goat.getRandom().nextInt(20) == 0;
    }
    @Override public boolean canContinueToUse() { return !goat.onGround(); }
    @Override public void start() {
        double x = target.getX() - goat.getX(), z = target.getZ() - goat.getZ();
        double length = Math.sqrt(x * x + z * z);
        var old = goat.getDeltaMovement();
        if (length >= 1.0E-4) goat.setDeltaMovement(old.x + x / length * 0.5D * 0.800000011920929D
                        + old.x * 0.20000000298023224D, 0.25D,
                old.z + z / length * 0.5D * 0.800000011920929D + old.z * 0.20000000298023224D);
        else goat.setDeltaMovement(old.x, 0.25D, old.z);
    }
}
