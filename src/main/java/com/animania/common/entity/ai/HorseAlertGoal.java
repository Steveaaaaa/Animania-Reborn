package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaHorse;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import java.util.Comparator;
import java.util.EnumSet;

/** Observe an unfamiliar rushing player before making a short evasive movement. */
public final class HorseAlertGoal extends Goal {
    private final AnimaniaHorse horse;
    private Player approaching;
    private int nextCheck, ticks;
    public HorseAlertGoal(AnimaniaHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean free() {
        return !horse.isBaby() && !horse.isVehicle() && !horse.isPassenger() && !horse.isLeashed()
                && !horse.isPullingVehicle() && horse.hurtTime == 0 && horse.getTarget() == null
                && !ModAttachments.getData(horse, ModAttachments.SLEEPING) && !horse.isInWaterOrBubble()
                && !FarmHerdGoal.hasFoodLure(horse);
    }
    private boolean unfamiliar(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative()
                && !player.getUUID().equals(horse.getOwnerUUID()) && horse.hasLineOfSight(player);
    }
    private boolean rushingCloser(Player player) {
        var toward = horse.position().subtract(player.position());
        var movement = new net.minecraft.world.phys.Vec3(player.getX() - player.xo, 0, player.getZ() - player.zo);
        return player.isSprinting() && Math.max(movement.dot(toward), player.getDeltaMovement().dot(toward)) > 0.02;
    }
    @Override public boolean canUse() {
        if (horse.tickCount < nextCheck) return false;
        nextCheck = horse.tickCount + 20;
        if (!free()) return false;
        approaching = horse.level().players().stream().filter(p -> unfamiliar(p) && rushingCloser(p)
                && p.distanceToSqr(horse) < 64).min(Comparator.comparingDouble(horse::distanceToSqr)).orElse(null);
        return approaching != null;
    }
    @Override public void start() {
        ticks = 0; horse.getNavigation().stop();
        ModAttachments.setData(horse, ModAttachments.FARM_ACTIVITY, FarmActivityGoal.ALERT);
        ModAttachments.setData(horse, ModAttachments.FARM_ACTIVITY_START, (int) horse.level().getGameTime());
    }
    @Override public boolean canContinueToUse() {
        return free() && approaching != null && unfamiliar(approaching) && ticks < 100
                && horse.distanceToSqr(approaching) < 144;
    }
    @Override public void tick() {
        ticks++;
        horse.getLookControl().setLookAt(approaching, 8, 20);
        if (ticks >= 20 && ticks % 20 == 0 && horse.distanceToSqr(approaching) < 16 && rushingCloser(approaching)) {
            var away = DefaultRandomPos.getPosAway(horse, 6, 2, approaching.position());
            if (away != null) horse.getNavigation().moveTo(away.x, away.y, away.z, 1.2);
        }
    }
    @Override public void stop() {
        horse.getNavigation().stop(); ModAttachments.setData(horse, ModAttachments.FARM_ACTIVITY, 0);
        approaching = null; nextCheck = horse.tickCount + 400 + horse.getRandom().nextInt(200);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
