package com.animania.extra.amphibian;

import com.animania.common.entity.ai.FarmActivityGoal;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** Short habitat visits, with species-specific destinations and no new needs meter. */
final class AmphibianHabitatGoal extends Goal {
    private final AnimaniaAmphibian animal;
    private Vec3 destination;
    private Path path;
    private int nextSearch, travel, remaining;
    private boolean resting;

    AmphibianHabitatGoal(AnimaniaAmphibian animal) {
        this.animal = animal;
        nextSearch = animal.tickCount + 100 + animal.getRandom().nextInt(300);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }
    private boolean appropriateTime() {
        return switch (animal.kind()) {
            case FROG -> animal.level().isDay();
            case TOAD -> animal.level().isDay() && !animal.level().isRaining();
            case DART_FROG -> animal.level().isNight();
        };
    }
    private boolean available() {
        return animal.isAlive() && animal.hurtTime == 0 && animal.getTarget() == null
                && !animal.isOnFire() && !animal.isInWaterOrBubble() && !animal.isLeashed()
                && !animal.isPassenger() && !animal.isVehicle() && appropriateTime()
                && (animal.getLastHurtByMob() == null || animal.tickCount - animal.getLastHurtByMobTimestamp() > 100)
                && !(animal.hasCustomName() && animal.getName().getString().equals("Pepe"));
    }
    private boolean suitable(Vec3 pos) {
        BlockPos feet = BlockPos.containing(pos);
        var level = animal.level();
        if (!level.hasChunkAt(feet) || !level.getFluidState(feet).isEmpty()
                || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                || !level.noCollision(animal, animal.getBoundingBox().move(pos.subtract(animal.position())))) return false;
        var floor = level.getBlockState(feet.below());
        if (floor.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
                || floor.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)
                || floor.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)) return false;
        if (animal.kind() == AnimaniaAmphibian.Kind.FROG) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos water = feet.relative(side);
                if (level.hasChunkAt(water) && (level.getFluidState(water).is(FluidTags.WATER)
                        || level.getFluidState(water.below()).is(FluidTags.WATER))) return true;
            }
            return false;
        }
        for (int height = 1; height <= 3; height++) {
            BlockPos roof = feet.above(height);
            if (!level.getBlockState(roof).getCollisionShape(level, roof).isEmpty()) return true;
        }
        return false;
    }
    @Override public boolean canUse() {
        if (animal.tickCount < nextSearch) return false;
        nextSearch = animal.tickCount + 200 + animal.getRandom().nextInt(200);
        if (!available() || !animal.onGround()) return false;
        if (suitable(animal.position())) { destination = animal.position(); path = null; return true; }
        int attempts = 0;
        for (int i = 0; i < 40; i++) {
            BlockPos feet = animal.blockPosition().offset(animal.getRandom().nextInt(13) - 6,
                    animal.getRandom().nextInt(3) - 1, animal.getRandom().nextInt(13) - 6);
            Vec3 pos = Vec3.atBottomCenterOf(feet);
            if (!suitable(pos)) continue;
            if (++attempts > 4) break;
            Path route = animal.getNavigation().createPath(feet, 0);
            if (route == null || !route.canReach()) continue;
            boolean safe = true;
            for (int n = 0; n < route.getNodeCount(); n++) {
                var node = route.getNode(n);
                BlockPos step = new BlockPos(node.x, node.y, node.z);
                if (!animal.level().hasChunkAt(step) || !animal.level().getFluidState(step).isEmpty()
                        || n > 0 && Math.abs(node.y - route.getNode(n - 1).y) > 1) { safe = false; break; }
            }
            if (!safe) continue;
            destination = pos; path = route; return true;
        }
        return false;
    }
    @Override public void start() {
        travel = 0; remaining = 200; resting = false;
        if (path == null) beginRest(); else animal.getNavigation().moveTo(path, 0.6);
    }
    private void beginRest() {
        resting = true; animal.getNavigation().stop();
        animal.setData(ModAttachments.FARM_ACTIVITY, animal.kind() == AnimaniaAmphibian.Kind.FROG
                ? FarmActivityGoal.SHORE_REST : FarmActivityGoal.SHELTER);
        animal.setData(ModAttachments.FARM_ACTIVITY_START, (int) animal.level().getGameTime());
    }
    @Override public boolean canContinueToUse() {
        return available() && destination != null && suitable(destination) && travel < 180 && remaining > 0
                && (!resting || animal.distanceToSqr(destination) < 2.25 && suitable(animal.position()));
    }
    @Override public void tick() {
        if (!resting) {
            travel++;
            if (animal.onGround() && animal.distanceToSqr(destination) < 0.5 && suitable(animal.position())) beginRest();
            else if (animal.getNavigation().isDone()) travel = 180;
            return;
        }
        remaining--; animal.getNavigation().stop();
        animal.getLookControl().setLookAt(animal.getX() + Math.sin(-animal.getYRot() * Math.PI / 180),
                animal.getEyeY(), animal.getZ() + Math.cos(animal.getYRot() * Math.PI / 180), 4, 10);
    }
    @Override public void stop() {
        animal.getNavigation().stop(); animal.setData(ModAttachments.FARM_ACTIVITY, 0);
        destination = null; path = null; resting = false;
        nextSearch = animal.tickCount + 600 + animal.getRandom().nextInt(600);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
