package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.InteractionHand;
import java.util.EnumSet;

/** EntityAIAttackMeleeBulls with the original reach and repathing/attack intervals. */
public final class LegacyBullAttackGoal extends Goal {
    private final PathfinderMob bull;
    private Path path;
    private int delay, attackTick;
    private double targetX, targetY, targetZ;
    public LegacyBullAttackGoal(PathfinderMob bull) { this.bull = bull; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
    @Override public boolean canUse() {
        var target = bull.getTarget();
        if (target == null || !target.isAlive() || target instanceof Skeleton) return false;
        if (bull.getData(ModAttachments.SLEEPING)) bull.setData(ModAttachments.SLEEPING, false);
        else { bull.setData(ModAttachments.FIGHTING, true); bull.setData(ModAttachments.EATING_TICKS, 0); }
        path = bull.getNavigation().createPath(target, 0);
        return path != null;
    }
    @Override public boolean canContinueToUse() {
        return bull.getTarget() != null && bull.getTarget().isAlive() && !bull.getNavigation().isDone();
    }
    @Override public void start() { bull.getNavigation().moveTo(path, 1.8D); delay = 0; }
    @Override public void stop() {
        if (bull.getTarget() instanceof Player p && (p.isSpectator() || p.isCreative())) bull.setTarget(null);
        bull.getNavigation().stop(); bull.setData(ModAttachments.FIGHTING, false);
    }
    @Override public void tick() {
        var target = bull.getTarget();
        if (target == null) return;
        bull.getLookControl().setLookAt(target, 20, 20);
        double distance = bull.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        --delay;
        if (bull.getSensing().hasLineOfSight(target) && delay <= 0
                && (targetX == 0 && targetY == 0 && targetZ == 0
                || target.distanceToSqr(targetX, targetY, targetZ) >= 1 || bull.getRandom().nextFloat() < 0.05F)) {
            targetX = target.getX(); targetY = target.getBoundingBox().minY; targetZ = target.getZ();
            delay = 4 + bull.getRandom().nextInt(7);
            if (distance > 1024) delay += 10;
            else if (distance > 256) delay += 5;
            if (!bull.getNavigation().moveTo(target, 1.8D)) delay += 15;
        }
        attackTick = Math.max(attackTick - 1, 0);
        double reach = bull.getBbWidth() * 2.0F * bull.getBbWidth() * 2.0F + target.getBbWidth();
        if (distance <= reach && attackTick <= 0) {
            attackTick = 20; bull.swing(InteractionHand.MAIN_HAND); bull.doHurtTarget(target);
        }
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
