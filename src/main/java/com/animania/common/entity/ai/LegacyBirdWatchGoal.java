package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

/** Both 1.12 EntityAIWatchClosestFromSide implementations aim five blocks above the eyes. */
public final class LegacyBirdWatchGoal extends LookAtPlayerGoal {
    private final Mob bird;
    public LegacyBirdWatchGoal(Mob bird, float distance) { super(bird, Player.class, distance); this.bird = bird; }
    @Override public boolean canUse() { return !ModAttachments.getData(bird, ModAttachments.SLEEPING) && super.canUse(); }
    @Override public void tick() {
        super.tick();
        if (lookAt != null) bird.getLookControl().setLookAt(lookAt.getX(), lookAt.getEyeY() + 5.0D,
                lookAt.getZ(), bird.getMaxHeadYRot(), bird.getMaxHeadXRot());
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
