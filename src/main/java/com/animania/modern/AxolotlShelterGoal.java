package com.animania.modern;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.entity.ai.FarmActivityGoal;
import com.animania.common.entity.ai.FarmHerdGoal;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** A short aquatic rest; never sets the land-animal sleeping flag. */
final class AxolotlShelterGoal extends Goal {
    private final ModernAxolotl animal;
    private Vec3 destination;
    private Path path;
    private int nextSearch, travel, remaining;
    private boolean resting;

    AxolotlShelterGoal(ModernAxolotl animal) {
        this.animal = animal;
        nextSearch = animal.tickCount + 200 + animal.getRandom().nextInt(400);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    private boolean available() {
        return animal.isAlive() && animal.isInWater() && !animal.isPlayingDead()
                && !animal.isInLove() && !animal.isLeashed() && !animal.isPassenger() && !animal.isVehicle()
                && animal.hurtTime == 0 && !animal.isOnFire() && animal.getTarget() == null
                && !animal.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && (animal.getLastHurtByMob() == null || animal.tickCount - animal.getLastHurtByMobTimestamp() > 100)
                && animal.level().isDay() && LegacyAnimalNeeds.isFed(animal)
                && LegacyAnimalNeeds.isWatered(animal) && !FarmHerdGoal.hasFoodLure(animal);
    }
    private boolean submerged(Vec3 pos) {
        var box = animal.getBoundingBox().move(pos.subtract(animal.position())).deflate(0.01);
        for (BlockPos part : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (!animal.level().hasChunkAt(part) || !animal.level().getFluidState(part).is(FluidTags.WATER)) return false;
        }
        return animal.level().noCollision(animal, box);
    }
    private boolean shelter(Vec3 pos) {
        BlockPos feet = BlockPos.containing(pos);
        if (!submerged(pos) || !animal.level().getBlockState(feet.below())
                .isFaceSturdy(animal.level(), feet.below(), Direction.UP)) return false;
        if (animal.level().getMaxLocalRawBrightness(feet) <= 7) return true;
        for (int i = 1; i <= 3; i++) {
            BlockPos above = feet.above(i);
            if (animal.level().hasChunkAt(above) && !animal.level().getBlockState(above)
                    .getCollisionShape(animal.level(), above).isEmpty()) return true;
        }
        return false;
    }
    @Override public boolean canUse() {
        if (animal.tickCount < nextSearch) return false;
        nextSearch = animal.tickCount + 200 + animal.getRandom().nextInt(200);
        if (!available()) return false;
        if (shelter(animal.position())) { destination = animal.position(); path = null; return true; }
        int paths = 0;
        for (int i = 0; i < 48; i++) {
            BlockPos feet = animal.blockPosition().offset(animal.getRandom().nextInt(13) - 6,
                    animal.getRandom().nextInt(5) - 2, animal.getRandom().nextInt(13) - 6);
            Vec3 pos = Vec3.atBottomCenterOf(feet).add(0, 0.05, 0);
            if (!animal.level().hasChunkAt(feet) || !shelter(pos)) continue;
            if (++paths > 4) break;
            Path route = animal.getNavigation().createPath(feet, 0);
            if (route == null || !route.canReach()) continue;
            boolean aquatic = true;
            for (int node = 0; node < route.getNodeCount(); node++) {
                var step = route.getNode(node);
                if (!submerged(new Vec3(step.x + 0.5, step.y + 0.05, step.z + 0.5))) { aquatic = false; break; }
            }
            if (!aquatic) continue;
            destination = pos; path = route; return true;
        }
        return false;
    }
    @Override public void start() {
        animal.pauseBrainForShelter();
        travel = 0; remaining = 200; resting = false;
        if (path == null) beginRest(); else animal.getNavigation().moveTo(path, 0.6);
    }
    private void beginRest() {
        resting = true;
        animal.getNavigation().stop();
        animal.setData(ModAttachments.FARM_ACTIVITY, FarmActivityGoal.AQUATIC_REST);
        animal.setData(ModAttachments.FARM_ACTIVITY_START, (int) animal.level().getGameTime());
    }
    @Override public boolean canContinueToUse() {
        return available() && destination != null && shelter(destination) && travel < 160 && remaining > 0
                && (!resting || animal.distanceToSqr(destination) < 2.25 && shelter(animal.position()));
    }
    @Override public void tick() {
        if (!resting) {
            travel++;
            if (animal.distanceToSqr(destination) < 0.5 && shelter(animal.position())) beginRest();
            else if (animal.getNavigation().isDone()) travel = 160;
            return;
        }
        remaining--;
        animal.getNavigation().stop();
    }
    @Override public void stop() {
        animal.getNavigation().stop();
        animal.setData(ModAttachments.FARM_ACTIVITY, 0);
        destination = null; path = null; resting = false;
        nextSearch = animal.tickCount + 1200 + animal.getRandom().nextInt(1200);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
