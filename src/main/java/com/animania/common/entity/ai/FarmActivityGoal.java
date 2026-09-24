package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.livestock.AnimaniaPig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import java.util.EnumSet;

/** Finite daytime activities; movement and looking belong to one goal throughout each action. */
public final class FarmActivityGoal extends LegacySearchBlockGoal {
    public static final int FORAGE = 1, DUST_BATH = 2, RUMINATE = 3, WALLOW = 4, STANDING_REST = 5, ALERT = 6, BROWSE = 7, EXPLORE = 8, SPAR = 9, GROOM = 10, PET_EXPLORE = 11, SNIFF = 12, GREET = 13, PET_REST = 14, SMALL_EXPLORE = 15, SHELTER = 16, SCRATCH = 17, PREEN = 18, FAMILY_REGROUP = 19, AQUATIC_REST = 20, SHORE_REST = 21;
    private final Animal subject;
    private final int activity;
    private int nextAttempt, remaining, travelTicks;
    private boolean acting;

    public FarmActivityGoal(Animal animal, int activity) {
        super(animal, 0.85D, DestinationOffsets.UP, 7);
        subject = animal;
        this.activity = activity;
        nextAttempt = animal.tickCount + 100 + animal.getRandom().nextInt(400);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public static int duration(int activity) {
        return switch (activity) { case DUST_BATH -> 160; case RUMINATE -> 400; case WALLOW -> 220; case STANDING_REST -> 300; case EXPLORE -> 180; case SPAR -> 120; case GROOM -> 120; case PET_EXPLORE -> 80; case SNIFF -> 100; case GREET -> 100; case PET_REST -> 300; case SMALL_EXPLORE -> 100; case SHELTER -> 200; case SCRATCH -> 100; case PREEN -> 140; case FAMILY_REGROUP -> 200; case AQUATIC_REST -> 200; case SHORE_REST -> 200; default -> 100; };
    }

    private boolean available() {
        return subject.isAlive() && subject.onGround() && !subject.isInWaterOrBubble()
                && !subject.isOnFire() && subject.hurtTime == 0 && subject.getTarget() == null
                && !subject.isVehicle() && !subject.isPassenger() && !subject.isLeashed()
                && !subject.getData(ModAttachments.SLEEPING) && !LegacySleepGoal.shouldSleepNow(subject)
                && (acting || subject.getData(ModAttachments.EATING_TICKS) == 0)
                && (!(subject instanceof com.animania.farm.livestock.AnimaniaHorse horse) || !horse.isPullingVehicle())
                && (!(subject instanceof com.animania.farm.livestock.AnimaniaGoat goat) || !goat.isSpooked())
                && (!(subject instanceof AnimaniaChicken bird) || !bird.isLookingForNest())
                && (!(subject instanceof com.animania.extra.peafowl.AnimaniaPeafowl bird) || !bird.isLookingForNest())
                && (activity == FORAGE || LegacyAnimalNeeds.isFed(subject) && LegacyAnimalNeeds.isWatered(subject))
                && !subject.level().isRainingAt(subject.blockPosition())
                && subject.level().players().stream().noneMatch(p -> !p.isSpectator()
                    && p.distanceToSqr(subject) < 64 && (subject.isFood(p.getMainHandItem()) || subject.isFood(p.getOffhandItem())));
    }

    @Override public boolean canUse() {
        if (subject.tickCount < nextAttempt) return false;
        nextAttempt = subject.tickCount + 200 + subject.getRandom().nextInt(200);
        if (!available() || subject.isBaby() && activity != FORAGE) return false;
        if (activity == RUMINATE || activity == STANDING_REST || activity == PREEN) {
            seekingBlockPos = subject.blockPosition().below();
            return shouldMoveTo(seekingBlockPos);
        }
        return searchForDestination();
    }

    @Override public void start() {
        remaining = duration(activity);
        travelTicks = 0;
        acting = false;
        if (activity == RUMINATE || activity == STANDING_REST || activity == PREEN) onArriveAtDestination();
        else super.start();
    }

    @Override public boolean canContinueToUse() {
        return available() && remaining > 0 && travelTicks < 240 && targetStillValid()
                && (acting || super.canContinueToUse());
    }

    @Override protected boolean shouldMoveTo(BlockPos pos) {
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                || level.isRainingAt(pos.above()) || !level.getFluidState(pos.above()).isEmpty()) return false;
        var state = level.getBlockState(pos);
        if (activity == RUMINATE && subject instanceof com.animania.modern.MountainGoat) {
            var box = subject.getBoundingBox();
            return state.isFaceSturdy(level, pos, net.minecraft.core.Direction.UP)
                    && !state.is(Blocks.MAGMA_BLOCK) && !state.is(Blocks.CAMPFIRE)
                    && !state.is(Blocks.SOUL_CAMPFIRE) && level.noCollision(subject, box);
        }
        return switch (activity) {
            case PREEN -> state.isFaceSturdy(level, pos, net.minecraft.core.Direction.UP);
            case DUST_BATH -> state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.DIRT);
            case FORAGE -> state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT);
            case WALLOW -> AnimaniaPig.isMud(level, pos);
            default -> state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.HAY_BLOCK) || state.is(Blocks.DIRT);
        };
    }

    @Override protected boolean targetStillValid() {
        return seekingBlockPos != null && shouldMoveTo(seekingBlockPos)
                && (!acting || subject.distanceToSqr(seekingBlockPos.getX() + 0.5,
                    seekingBlockPos.getY() + 1, seekingBlockPos.getZ() + 0.5) < 4);
    }

    @Override protected void onArriveAtDestination() {
        acting = true;
        subject.getNavigation().stop();
        subject.setData(ModAttachments.FARM_ACTIVITY, activity);
        subject.setData(ModAttachments.FARM_ACTIVITY_START, (int) level.getGameTime());
    }

    @Override public void tick() {
        if (!acting) {
            travelTicks++;
            double dx = subject.getX() - seekingBlockPos.getX() - 0.5;
            double dz = subject.getZ() - seekingBlockPos.getZ() - 0.5;
            if (dx * dx + dz * dz < 0.36 && Math.abs(subject.getY() - seekingBlockPos.getY() - 1) < 1.1)
                onArriveAtDestination();
            else if (travelTicks % 20 == 0)
                subject.getNavigation().moveTo(seekingBlockPos.getX() + 0.5, seekingBlockPos.getY() + 1,
                        seekingBlockPos.getZ() + 0.5, 0.85);
            return;
        }
        subject.getNavigation().stop();
        subject.setYHeadRot(subject.getYRot());
        remaining--;
        if (activity != RUMINATE && activity != STANDING_REST && activity != PREEN && remaining > 20 && remaining % 12 == 0) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(seekingBlockPos)),
                    subject.getX(), subject.getY() + 0.12, subject.getZ(), 3, 0.25, 0.08, 0.25, 0.015);
        }
        if (remaining == 0) {
            if (activity == FORAGE && subject instanceof AnimaniaChicken) LegacyAnimalNeeds.setFed(subject, true);
            if (activity == WALLOW && subject instanceof AnimaniaPig pig) pig.refreshPlay();
        }
    }

    @Override public void stop() {
        subject.setData(ModAttachments.FARM_ACTIVITY, 0);
        acting = false;
        remaining = 0;
        nextAttempt = subject.tickCount + 1200 + subject.getRandom().nextInt(1200);
        subject.getNavigation().stop();
        super.stop();
    }
}
