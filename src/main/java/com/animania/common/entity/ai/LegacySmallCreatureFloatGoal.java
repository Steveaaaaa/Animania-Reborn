package com.animania.common.entity.ai;

import com.animania.farm.livestock.AnimaniaPig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;

/** GenericAISwimmingSmallCreatures also swims out of mud. */
public final class LegacySmallCreatureFloatGoal extends FloatGoal {
    private final Mob mob;
    public LegacySmallCreatureFloatGoal(Mob mob) { super(mob); this.mob = mob; }
    @Override public boolean canUse() {
        var motion = mob.getDeltaMovement();
        return AnimaniaPig.isMud(mob.level(), BlockPos.containing(
                mob.getX() + motion.x / 1.5D, mob.getY() + 0.1D, mob.getZ() + motion.z / 1.5D)) || super.canUse();
    }
}
