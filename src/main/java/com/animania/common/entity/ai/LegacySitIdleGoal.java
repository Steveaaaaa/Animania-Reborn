package com.animania.common.entity.ai;

import com.animania.catsdogs.cat.AnimaniaCat;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/** GenericAISitIdle, retained but intentionally unregistered as in the 1.12 cat base. */
public final class LegacySitIdleGoal extends Goal {
    private final AnimaniaCat cat;
    private double lookX, lookZ;
    private int idleTime;
    public LegacySitIdleGoal(AnimaniaCat cat) { this.cat = cat; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
    @Override public boolean canUse() { return cat.getRandom().nextFloat() < 0.002F; }
    @Override public boolean canContinueToUse() { return idleTime >= 0; }
    @Override public void start() {
        double angle = Math.PI * 2 * cat.getRandom().nextDouble();
        lookX = Math.cos(angle); lookZ = Math.sin(angle); idleTime = 50 + cat.getRandom().nextInt(20);
    }
    @Override public void tick() {
        --idleTime;
        if (!cat.isInSittingPose() && idleTime > 0) cat.setInSittingPose(true);
        else if (cat.isInSittingPose() && idleTime == 0) cat.setInSittingPose(false);
        cat.getLookControl().setLookAt(cat.getX() + lookX, cat.getEyeY(), cat.getZ() + lookZ,
                cat.getMaxHeadYRot(), cat.getMaxHeadXRot());
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
