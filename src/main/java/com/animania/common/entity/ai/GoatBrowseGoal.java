package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaGoat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import java.util.EnumSet;

/** Browses reachable low branches without treating every decorative plant as food. */
public final class GoatBrowseGoal extends Goal {
    private final AnimaniaGoat goat;
    private BlockPos leaves, standing;
    private Path path;
    private int nextSearch, travel, remaining;
    private boolean eating;
    public GoatBrowseGoal(AnimaniaGoat goat) {
        this.goat = goat;
        nextSearch = goat.tickCount + 60 + goat.getRandom().nextInt(160);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean available() {
        return !goat.isBaby() && !goat.isSpooked() && goat.hurtTime == 0 && goat.getTarget() == null
                && !goat.isLeashed() && !goat.isPassenger() && !goat.isVehicle() && !goat.isInWaterOrBubble()
                && !goat.getData(ModAttachments.SLEEPING) && !goat.getData(ModAttachments.FIGHTING)
                && !LegacySleepGoal.shouldSleepNow(goat) && !LegacyAnimalNeeds.isFed(goat)
                && !FarmHerdGoal.hasFoodLure(goat);
    }
    private boolean edible(BlockPos pos) {
        var state = goat.level().getBlockState(pos);
        return state.is(Blocks.OAK_LEAVES) || state.is(Blocks.BIRCH_LEAVES) || state.is(Blocks.ACACIA_LEAVES);
    }
    @Override public boolean canUse() {
        if (goat.tickCount < nextSearch) return false;
        nextSearch = goat.tickCount + 160 + goat.getRandom().nextInt(160);
        if (!available()) return false;
        BlockPos origin = goat.blockPosition();
        int attempts = 0;
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-5, 0, -5), origin.offset(5, 2, 5))) {
            if (!edible(candidate) || candidate.getY() + 0.5 - goat.getEyeY() > 1.0) continue;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos foot = candidate.relative(direction).atY(origin.getY());
                if (!goat.level().getBlockState(foot.below()).isFaceSturdy(goat.level(), foot.below(), Direction.UP)
                        || !goat.level().getFluidState(foot).isEmpty()
                        || !goat.level().noCollision(goat, goat.getBoundingBox().move(
                            foot.getX() + 0.5 - goat.getX(), foot.getY() - goat.getY(), foot.getZ() + 0.5 - goat.getZ()))) continue;
                if (++attempts > 8) return false;
                Path route = goat.getNavigation().createPath(foot, 0);
                if (route == null || !route.canReach()) continue;
                leaves = candidate.immutable(); standing = foot; path = route;
                return true;
            }
        }
        return false;
    }
    @Override public void start() { travel = 0; remaining = 100; eating = false; goat.getNavigation().moveTo(path, 1); }
    @Override public boolean canContinueToUse() {
        return available() && leaves != null && edible(leaves) && remaining > 0 && travel < 200
                && goat.distanceToSqr(leaves.getX() + 0.5, leaves.getY(), leaves.getZ() + 0.5) < 100;
    }
    @Override public void tick() {
        goat.getLookControl().setLookAt(leaves.getX() + 0.5, leaves.getY() + 0.5, leaves.getZ() + 0.5, 8, 25);
        if (!eating) {
            travel++;
            if (goat.distanceToSqr(standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5) > 0.7) return;
            eating = true;
            goat.getNavigation().stop();
            goat.setData(ModAttachments.FARM_ACTIVITY, FarmActivityGoal.BROWSE);
            goat.setData(ModAttachments.FARM_ACTIVITY_START, (int) goat.level().getGameTime());
        }
        float facing = (float) (Math.atan2(leaves.getZ() + 0.5 - goat.getZ(), leaves.getX() + 0.5 - goat.getX()) * 180 / Math.PI) - 90;
        goat.setYRot(net.minecraft.util.Mth.approachDegrees(goat.getYRot(), facing, 6));
        goat.yBodyRot = goat.getYRot();
        if (--remaining == 0) {
            int blockId = Block.getId(goat.level().getBlockState(leaves));
            if (LegacyConfig.PLANTS_REMOVED_AFTER_EATING.get() && goat.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
                goat.level().destroyBlock(leaves, false, goat);
            goat.level().levelEvent(2001, leaves, blockId);
            LegacyAnimalNeeds.setFed(goat, true);
        }
    }
    @Override public void stop() {
        goat.setData(ModAttachments.FARM_ACTIVITY, 0);
        goat.getNavigation().stop(); leaves = standing = null; path = null; eating = false;
        nextSearch = goat.tickCount + 200 + goat.getRandom().nextInt(200);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
