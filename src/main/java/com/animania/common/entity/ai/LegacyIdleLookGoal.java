package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** Generic, horse and carried-rodent look-idle variants from 1.12. */
public final class LegacyIdleLookGoal extends Goal {
    private final Mob mob;
    private int remaining;
    private double x, z;
    public LegacyIdleLookGoal(Mob mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
    @Override public boolean canUse() {
        return !mob.getData(ModAttachments.SLEEPING)
                && (!(mob instanceof AnimaniaHorse) || mob.level().isDay())
                && (!(mob instanceof AnimaniaRodent) || !mob.isPassenger()) && mob.getRandom().nextFloat() < 0.02F;
    }
    @Override public void start() {
        double angle = Math.PI * 2 * mob.getRandom().nextDouble();
        x = Math.cos(angle); z = Math.sin(angle); remaining = 20 + mob.getRandom().nextInt(20);
    }
    @Override public boolean canContinueToUse() { return remaining >= 0 && !mob.getData(ModAttachments.SLEEPING); }
    @Override public void tick() {
        --remaining;
        mob.getLookControl().setLookAt(mob.getX() + x, mob.getEyeY(), mob.getZ() + z,
                mob.getMaxHeadYRot(), mob.getMaxHeadXRot());
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
