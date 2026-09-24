package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaGoat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import java.util.EnumSet;

/** Uses ordinary navigation to reach raised terrain; no teleporting or cliff jumps. */
public final class GoatExploreGoal extends Goal {
    private final net.minecraft.world.entity.animal.Animal goat;
    private BlockPos destination;
    private Path path;
    private int nextSearch, ticks;
    public GoatExploreGoal(net.minecraft.world.entity.animal.Animal goat) {
        this.goat = goat;
        nextSearch = goat.tickCount + 200 + goat.getRandom().nextInt(400);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean available() {
        return goat.isAlive() && !(goat instanceof AnimaniaGoat domestic && domestic.isSpooked())
                && (!(goat instanceof com.animania.modern.MountainGoat) || !goat.isBaby())
                && !goat.isOnFire() && goat.hurtTime == 0 && goat.getTarget() == null
                && !goat.isLeashed() && !goat.isPassenger() && !goat.isVehicle() && !goat.isInWaterOrBubble()
                && !ModAttachments.getData(goat, ModAttachments.SLEEPING) && !ModAttachments.getData(goat, ModAttachments.FIGHTING)
                && !LegacySleepGoal.shouldSleepNow(goat) && LegacyAnimalNeeds.isFed(goat)
                && LegacyAnimalNeeds.isWatered(goat) && !FarmHerdGoal.hasFoodLure(goat);
    }
    @Override public boolean canUse() {
        if (goat.tickCount < nextSearch) return false;
        nextSearch = goat.tickCount + 400 + goat.getRandom().nextInt(400);
        if (!available()) return false;
        int paths = 0;
        for (int i = 0; i < 32; i++) {
            BlockPos pos = goat.blockPosition().offset(goat.getRandom().nextInt(13) - 6,
                    1 + goat.getRandom().nextInt(3), goat.getRandom().nextInt(13) - 6);
            if (!goat.level().hasChunkAt(pos) || !goat.level().getFluidState(pos).isEmpty()
                    || !goat.level().getBlockState(pos.below()).isFaceSturdy(goat.level(), pos.below(), Direction.UP)
                    || !goat.level().noCollision(goat, goat.getBoundingBox().move(pos.getX() + 0.5 - goat.getX(),
                        pos.getY() - goat.getY(), pos.getZ() + 0.5 - goat.getZ()))) continue;
            if (++paths > 4) break;
            Path route = goat.getNavigation().createPath(pos, 0);
            if (route == null || !route.canReach()) continue;
            boolean safe = true;
            for (int node = 1; node < route.getNodeCount(); node++)
                if (Math.abs(route.getNode(node).y - route.getNode(node - 1).y) > 1) { safe = false; break; }
            if (safe) { destination = pos; path = route; return true; }
        }
        return false;
    }
    @Override public void start() {
        ticks = 0;
        goat.getNavigation().moveTo(path, 0.9);
        ModAttachments.setData(goat, ModAttachments.FARM_ACTIVITY, FarmActivityGoal.EXPLORE);
        ModAttachments.setData(goat, ModAttachments.FARM_ACTIVITY_START, (int) goat.level().getGameTime());
    }
    @Override public boolean canContinueToUse() { return available() && ticks < 180 && !goat.getNavigation().isDone(); }
    @Override public void tick() { ticks++; }
    @Override public void stop() {
        goat.getNavigation().stop(); ModAttachments.setData(goat, ModAttachments.FARM_ACTIVITY, 0);
        path = null; destination = null;
        nextSearch = goat.tickCount + 600 + goat.getRandom().nextInt(600);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
