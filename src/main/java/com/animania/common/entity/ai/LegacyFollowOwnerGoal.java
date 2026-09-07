package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/** Follow-owner goal whose long-distance teleport obeys tamedAnimalsTeleport. */
public final class LegacyFollowOwnerGoal extends Goal {
    private final TamableAnimal pet;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int recalc;
    private float oldWaterCost;

    public LegacyFollowOwnerGoal(TamableAnimal pet, double speed, float startDistance, float stopDistance) {
        this.pet = pet;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        owner = pet.getOwner();
        return owner != null && !owner.isSpectator() && !pet.isInSittingPose() && !pet.isPassenger()
                && !ModAttachments.getData(pet, ModAttachments.SLEEPING)
                && pet.distanceToSqr(owner) >= startDistance * startDistance;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && owner.isAlive() && !pet.getNavigation().isDone() && !pet.isInSittingPose()
                && !ModAttachments.getData(pet, ModAttachments.SLEEPING)
                && pet.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        recalc = 0;
        oldWaterCost = pet.getPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER);
        pet.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, 0);
    }

    @Override
    public void tick() {
        if (owner == null) return;
        pet.getLookControl().setLookAt(owner, 10.0F, pet.getMaxHeadXRot());
        if (--recalc > 0) return;
        recalc = 10;
        if (!pet.getNavigation().moveTo(owner, speed) && !pet.isLeashed() && !pet.isPassenger()
                && LegacyConfig.TAMED_ANIMALS_TELEPORT.get() && pet.distanceToSqr(owner) >= 144.0D)
            tryTeleportNearOwner();
    }

    @Override
    public void stop() {
        pet.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, oldWaterCost);
        owner = null;
        pet.getNavigation().stop();
    }

    private boolean tryTeleportNearOwner() {
        BlockPos center = owner.blockPosition();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos pos = center.offset(x, 0, z);
            if (Math.abs(pos.getX() - center.getX()) < 2 && Math.abs(pos.getZ() - center.getZ()) < 2
                    || !pet.level().hasChunkAt(pos)) continue;
            BlockState floor = pet.level().getBlockState(pos.below());
            if (!floor.isFaceSturdy(pet.level(), pos.below(), net.minecraft.core.Direction.UP)
                    || !pet.level().getBlockState(pos).getCollisionShape(pet.level(), pos).isEmpty()
                    || !pet.level().getBlockState(pos.above()).getCollisionShape(pet.level(), pos.above()).isEmpty()) {
                continue;
            }
            pet.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            pet.getNavigation().stop();
            return true;
        }
        return false;
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
