package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** Reachable shelter and brief exploration/digging without creating burrows or free food. */
public final class SmallAnimalActivityGoal extends Goal {
    private final Animal animal;
    private final int activity;
    private Vec3 destination;
    private Path path;
    private int nextSearch, travel, remaining;
    private boolean performing;
    private Player disturbance;

    public SmallAnimalActivityGoal(Animal animal, int activity) {
        this.animal = animal;
        this.activity = activity;
        nextSearch = animal.tickCount + 40 + animal.getRandom().nextInt(160);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }
    private boolean free() {
        if (!animal.isAlive() || animal.isPassenger() || animal.isVehicle() || animal.isLeashed()
                || animal.isOnFire() || animal.isInWaterOrBubble() || animal.hurtTime > 0
                || animal.getTarget() != null || ModAttachments.getData(animal, ModAttachments.SLEEPING)
                || ModAttachments.getData(animal, ModAttachments.EATING_TICKS) > 0) return false;
        if (animal instanceof TamableAnimal tame) {
            if (tame.isOrderedToSit() || tame.isInSittingPose()) return false;
            if (tame.getOwner() != null && animal.distanceToSqr(tame.getOwner()) > 100) return false;
        }
        if (animal instanceof AnimaniaRodent rodent && (rodent.isInBall() || rodent.isHamsterStanding())) return false;
        return !FarmHerdGoal.hasFoodLure(animal);
    }
    private boolean restingTime() { return LegacyConfig.ANIMALS_SLEEP.get() && LegacySleepGoal.shouldSleepNow(animal); }
    private boolean sheltered(BlockPos foot) {
        for (int height = 1; height <= 3; height++) {
            BlockPos roof = foot.above(height);
            if (!animal.level().getBlockState(roof).getCollisionShape(animal.level(), roof).isEmpty()) return true;
        }
        return false;
    }
    private boolean scratchable(BlockPos foot) {
        var ground = animal.level().getBlockState(foot.below());
        return ground.is(Blocks.DIRT) || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.COARSE_DIRT)
                || ground.is(Blocks.SAND) || ground.is(Blocks.RED_SAND);
    }
    private boolean safe(Vec3 pos) {
        BlockPos foot = BlockPos.containing(pos);
        if (!animal.level().hasChunkAt(foot) || !animal.level().getFluidState(foot).isEmpty()
                || !animal.level().getBlockState(foot.below()).isFaceSturdy(animal.level(), foot.below(), Direction.UP)
                || !animal.level().noCollision(animal, animal.getBoundingBox().move(pos.subtract(animal.position())))
                || animal.level().isRainingAt(foot)) return false;
        if (animal instanceof TamableAnimal tame && tame.getOwner() != null
                && pos.distanceToSqr(tame.getOwner().position()) > 64) return false;
        return (activity != FarmActivityGoal.SHELTER || sheltered(foot))
                && (activity != FarmActivityGoal.SCRATCH || scratchable(foot));
    }
    private Player findDisturbance() {
        return animal.level().players().stream().filter(p -> !p.isSpectator() && !p.isCreative() && p.isSprinting()
                && p.distanceToSqr(animal) < 25 && animal.hasLineOfSight(p)
                && (!(animal instanceof TamableAnimal tame) || !p.getUUID().equals(tame.getOwnerUUID())))
                .findFirst().orElse(null);
    }
    private boolean prepare(Vec3 pos) {
        if (!safe(pos) || disturbance != null && pos.distanceToSqr(disturbance.position())
                < animal.distanceToSqr(disturbance)) return false;
        Path route = animal.getNavigation().createPath(BlockPos.containing(pos), 0);
        if (route == null || !route.canReach()) return false;
        path = route; destination = pos;
        return true;
    }
    @Override public boolean canUse() {
        if (animal.tickCount < nextSearch) return false;
        nextSearch = animal.tickCount + 100 + animal.getRandom().nextInt(120);
        if (!free() || !animal.onGround()) return false;
        disturbance = activity == FarmActivityGoal.SHELTER ? findDisturbance() : null;
        if (activity == FarmActivityGoal.SHELTER) {
            if (!restingTime() && disturbance == null && !animal.level().isRainingAt(animal.blockPosition())) return false;
            if (disturbance == null && (!LegacyAnimalNeeds.isFed(animal) || !LegacyAnimalNeeds.isWatered(animal))) return false;
            if (safe(animal.position()) && disturbance == null) { destination = animal.position(); path = null; return true; }
            int attempts = 0;
            for (BlockPos pos : BlockPos.betweenClosed(animal.blockPosition().offset(-6, -1, -6), animal.blockPosition().offset(6, 1, 6))) {
                Vec3 target = Vec3.atBottomCenterOf(pos);
                if (!safe(target)) continue;
                if (++attempts > 8) break;
                if (prepare(target)) return true;
            }
            return false;
        }
        if (restingTime() || !LegacyAnimalNeeds.isFed(animal) || !LegacyAnimalNeeds.isWatered(animal)) return false;
        if (activity == FarmActivityGoal.SCRATCH && safe(animal.position())) {
            destination = animal.position(); path = null; return true;
        }
        for (int i = 0; i < 4; i++) {
            Vec3 pos = DefaultRandomPos.getPos(animal, 4, 1);
            if (pos != null && prepare(pos)) return true;
        }
        return false;
    }
    @Override public void start() {
        performing = false; travel = 0; remaining = FarmActivityGoal.duration(activity);
        if (path == null) begin(); else animal.getNavigation().moveTo(path, disturbance == null ? 0.85 : 1.3);
    }
    private void begin() {
        performing = true; animal.getNavigation().stop();
        ModAttachments.setData(animal, ModAttachments.FARM_ACTIVITY, activity);
        ModAttachments.setData(animal, ModAttachments.FARM_ACTIVITY_START, (int) animal.level().getGameTime());
        if (activity == FarmActivityGoal.SHELTER && disturbance == null && restingTime())
            ModAttachments.setData(animal, ModAttachments.SLEEPING, true);
    }
    @Override public boolean canContinueToUse() {
        return free() && remaining > 0 && travel < 180 && destination != null && safe(destination)
                && (activity == FarmActivityGoal.SHELTER || !restingTime())
                && (!performing || animal.distanceToSqr(destination) < 2.25);
    }
    @Override public void tick() {
        if (!performing) {
            travel++;
            if (animal.distanceToSqr(destination) < 0.65 && safe(animal.position())) begin();
            else if (animal.getNavigation().isDone()) travel = 180;
            return;
        }
        remaining--; animal.getNavigation().stop();
        if (activity == FarmActivityGoal.SCRATCH && remaining > 15 && remaining % 16 == 0
                && animal.level() instanceof ServerLevel server) {
            var state = server.getBlockState(animal.blockPosition().below());
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), animal.getX(), animal.getY() + 0.05,
                    animal.getZ(), 2, 0.12, 0.02, 0.12, 0.01);
        }
        if (activity == FarmActivityGoal.SMALL_EXPLORE && remaining % 30 == 0) {
            double angle = animal.getRandom().nextDouble() * Math.PI * 2;
            animal.getLookControl().setLookAt(animal.getX() + Math.cos(angle), animal.getEyeY() - 0.1,
                    animal.getZ() + Math.sin(angle), 4, 15);
        }
    }
    @Override public void stop() {
        ModAttachments.setData(animal, ModAttachments.FARM_ACTIVITY, 0); animal.getNavigation().stop();
        destination = null; path = null; disturbance = null; performing = false;
        nextSearch = animal.tickCount + (activity == FarmActivityGoal.SHELTER ? 100 : 600) + animal.getRandom().nextInt(400);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
