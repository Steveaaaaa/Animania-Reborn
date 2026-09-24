package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** Quiet pet activities yield to commands, needs, combat and following an owner. */
public final class PetActivityGoal extends Goal {
    private final TamableAnimal pet;
    private final int activity;
    private int nextAttempt, travel, remaining;
    private boolean performing;
    private Vec3 destination;
    private Path path;
    private LivingEntity owner;

    public PetActivityGoal(TamableAnimal pet, int activity) {
        this.pet = pet;
        this.activity = activity;
        nextAttempt = pet.tickCount + 100 + pet.getRandom().nextInt(500);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }
    private boolean available() {
        LivingEntity currentOwner = pet.getOwner();
        return pet.isAlive() && pet.hurtTime == 0 && pet.getTarget() == null && !pet.isOnFire()
                && !pet.isInWaterOrBubble() && !pet.isInSittingPose() && !pet.isOrderedToSit()
                && !pet.isPassenger() && !pet.isVehicle() && !pet.isLeashed()
                && !ModAttachments.getData(pet, ModAttachments.SLEEPING) && !LegacySleepGoal.shouldSleepNow(pet)
                && ModAttachments.getData(pet, ModAttachments.EATING_TICKS) == 0
                && LegacyAnimalNeeds.isFed(pet) && LegacyAnimalNeeds.isWatered(pet)
                && !pet.level().isRainingAt(pet.blockPosition()) && !FarmHerdGoal.hasFoodLure(pet)
                && (currentOwner == null || pet.distanceToSqr(currentOwner) < 25)
                && (activity != FarmActivityGoal.GREET || owner != null && owner.isAlive()
                    && owner == currentOwner && !owner.isSpectator() && owner.hurtTime == 0
                    && !owner.isSprinting() && owner.getDeltaMovement().horizontalDistanceSqr() < 0.02);
    }
    private boolean safe(Vec3 pos) {
        BlockPos foot = BlockPos.containing(pos);
        if (!pet.level().hasChunkAt(foot) || !pet.level().getFluidState(foot).isEmpty()
                || !pet.level().getBlockState(foot.below()).isFaceSturdy(pet.level(), foot.below(), Direction.UP)
                || !pet.level().noCollision(pet, pet.getBoundingBox().move(pos.subtract(pet.position())))
                || pet.level().isRainingAt(foot)) return false;
        LivingEntity currentOwner = pet.getOwner();
        return currentOwner == null || pos.distanceToSqr(currentOwner.position()) < 16;
    }
    @Override public boolean canUse() {
        if (pet.tickCount < nextAttempt) return false;
        nextAttempt = pet.tickCount + 160 + pet.getRandom().nextInt(240);
        owner = pet.getOwner();
        if (!available() || !pet.onGround()) return false;
        if (activity == FarmActivityGoal.GROOM) {
            destination = pet.position();
            return safe(destination);
        }
        if (activity == FarmActivityGoal.GREET) {
            if (owner == null || pet.distanceToSqr(owner) < 2.25 || !pet.hasLineOfSight(owner)) return false;
            Vec3 look = owner.getViewVector(1);
            destination = owner.position().add(look.z * 1.8, 0, -look.x * 1.8);
            return preparePath(destination);
        }
        for (int attempt = 0; attempt < 4; attempt++) {
            Vec3 candidate = DefaultRandomPos.getPos(pet, 4, 1);
            if (candidate == null) continue;
            if (activity == FarmActivityGoal.PET_REST && pet.level().getMaxLocalRawBrightness(BlockPos.containing(candidate))
                    > pet.level().getMaxLocalRawBrightness(pet.blockPosition())) continue;
            if (preparePath(candidate)) return true;
        }
        return false;
    }
    private boolean preparePath(Vec3 pos) {
        if (!safe(pos)) return false;
        Path candidate = pet.getNavigation().createPath(BlockPos.containing(pos), 0);
        if (candidate == null || !candidate.canReach()) return false;
        destination = pos;
        path = candidate;
        return true;
    }
    @Override public void start() {
        travel = 0;
        remaining = FarmActivityGoal.duration(activity);
        performing = false;
        if (activity == FarmActivityGoal.GROOM) beginActivity();
        else pet.getNavigation().moveTo(path, 0.8);
    }
    private void beginActivity() {
        performing = true;
        pet.getNavigation().stop();
        ModAttachments.setData(pet, ModAttachments.FARM_ACTIVITY, activity);
        ModAttachments.setData(pet, ModAttachments.FARM_ACTIVITY_START, (int) pet.level().getGameTime());
    }
    @Override public boolean canContinueToUse() {
        return available() && travel < 160 && remaining > 0 && destination != null
                && (!performing || pet.distanceToSqr(destination) < 2.25)
                && (activity != FarmActivityGoal.GREET || owner.position().distanceToSqr(destination) < 9);
    }
    @Override public void tick() {
        if (!performing) {
            travel++;
            if (pet.distanceToSqr(destination) < 0.65) beginActivity();
            else if (pet.getNavigation().isDone()) travel = 160;
            return;
        }
        remaining--;
        pet.getNavigation().stop();
        if (activity == FarmActivityGoal.GREET && owner != null)
            pet.getLookControl().setLookAt(owner, 6, 20);
        else if (activity == FarmActivityGoal.SNIFF)
            pet.getLookControl().setLookAt(destination.x, destination.y, destination.z, 4, 15);
        else if (activity == FarmActivityGoal.PET_EXPLORE && remaining % 30 == 0) {
            double angle = pet.getRandom().nextDouble() * Math.PI * 2;
            pet.getLookControl().setLookAt(pet.getX() + Math.cos(angle) * 2, pet.getEyeY(),
                    pet.getZ() + Math.sin(angle) * 2, 4, 15);
        }
    }
    @Override public void stop() {
        ModAttachments.setData(pet, ModAttachments.FARM_ACTIVITY, 0);
        pet.getNavigation().stop();
        destination = null; owner = null; path = null; performing = false;
        nextAttempt = pet.tickCount + 1200 + pet.getRandom().nextInt(1200);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
