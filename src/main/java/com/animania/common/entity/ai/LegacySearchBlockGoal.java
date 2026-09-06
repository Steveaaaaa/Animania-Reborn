package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** NeoForge port of Animania 1.12's GenericAISearchBlock. */
abstract class LegacySearchBlockGoal extends Goal {
    protected final PathfinderMob animal;
    protected final ServerLevel level;
    protected final double speed;
    private final DestinationOffsets offsets;
    private final int fixedSearchRange;
    protected BlockPos seekingBlockPos;
    private BlockPos destinationBlock;
    private BlockPos oldBlockPos;
    private final Set<BlockPos> nonValidPositions = new HashSet<>();
    private int blacklistTimer;
    private int walkTries;
    private boolean atDestination;
    private boolean done;

    protected LegacySearchBlockGoal(PathfinderMob animal, double speed, DestinationOffsets offsets) {
        this(animal, speed, offsets, -1);
    }

    protected LegacySearchBlockGoal(PathfinderMob animal, double speed, DestinationOffsets offsets,
                                    int fixedSearchRange) {
        this.animal = animal;
        this.level = (ServerLevel) animal.level();
        this.speed = speed;
        this.offsets = offsets;
        this.fixedSearchRange = fixedSearchRange;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected final boolean searchForDestination() {
        if (++blacklistTimer > 10) {
            nonValidPositions.clear();
            blacklistTimer = 0;
            seekingBlockPos = null;
        }
        BlockPos origin = animal.blockPosition();
        if (origin.equals(oldBlockPos)) return false;
        oldBlockPos = origin;

        int searchRange = fixedSearchRange > 0 ? fixedSearchRange : LegacyConfig.AI_BLOCK_SEARCH_RANGE.get();
        int ySearchRange = Math.max(1, searchRange / 2);
        BlockPos secondaryDestination = null;
        BlockPos secondarySeeking = null;

        for (int range = 0; range < searchRange; range++) {
            for (int y = 0; y <= ySearchRange; y = y > 0 ? -y : 1 - y) {
                for (int x = 0; x <= range; x = x > 0 ? -x : 1 - x) {
                    for (int z = x < range && x > -range ? range : 0;
                         z <= range; z = z > 0 ? -z : 1 - z) {
                        BlockPos candidate = origin.offset(x, y - 1, z);
                        if (nonValidPositions.contains(candidate)) continue;
                        boolean primary = shouldMoveTo(candidate);
                        boolean secondary = !primary && hasSecondaryTarget() && shouldMoveToSecondary(candidate);
                        if (!primary && !secondary) continue;

                        BlockPos destination = findReachableDestination(candidate);
                        if (destination == null) continue;
                        if (primary) {
                            destinationBlock = destination;
                            seekingBlockPos = candidate;
                            return true;
                        }
                        if (secondarySeeking == null) {
                            secondaryDestination = destination;
                            secondarySeeking = candidate;
                        }
                    }
                }
            }
        }
        if (secondarySeeking != null) {
            destinationBlock = secondaryDestination;
            seekingBlockPos = secondarySeeking;
            return true;
        }
        return false;
    }

    private BlockPos findReachableDestination(BlockPos target) {
        if (offsets == DestinationOffsets.NONE || level.getBlockState(target).getCollisionShape(level, target).isEmpty()) {
            return animal.getNavigation().createPath(target, 0) != null ? target : null;
        }
        if (offsets == DestinationOffsets.UP) {
            BlockPos above = target.above();
            return animal.getNavigation().createPath(above, 0) != null ? above : null;
        }
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        int start = animal.getRandom().nextInt(directions.length);
        for (int i = 0; i < directions.length; i++) {
            BlockPos adjacent = target.relative(directions[(start + i) % directions.length]);
            if (animal.getNavigation().createPath(adjacent, 0) != null) return adjacent;
        }
        return null;
    }

    @Override
    public boolean canContinueToUse() {
        return destinationBlock != null && seekingBlockPos != null && !done && targetStillValid();
    }

    @Override
    public void start() {
        animal.getNavigation().moveTo(destinationBlock.getX() + 0.5D, destinationBlock.getY(),
                destinationBlock.getZ() + 0.5D, speed);
        walkTries = 0;
    }

    @Override
    public void stop() {
        atDestination = false;
        destinationBlock = null;
        seekingBlockPos = null;
        walkTries = 0;
        done = false;
    }

    @Override
    public void tick() {
        if (destinationBlock == null || seekingBlockPos == null) return;
        double distance = animal.distanceToSqr(destinationBlock.getX() + 0.5D,
                destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
        if (distance > 2.5D) {
            atDestination = false;
            walkTries++;
            boolean still = animal.xo == animal.getX() && animal.yo == animal.getY() && animal.zo == animal.getZ();
            if (still && walkTries % 40 == 0) {
                animal.getNavigation().moveTo(destinationBlock.getX() + 0.5D, destinationBlock.getY(),
                        destinationBlock.getZ() + 0.5D, speed);
                animal.getLookControl().setLookAt(seekingBlockPos.getX() + 0.5D, seekingBlockPos.getY(),
                        seekingBlockPos.getZ() + 0.5D, 10.0F, animal.getMaxHeadXRot());
            }
            if (still && walkTries > 100) {
                nonValidPositions.add(seekingBlockPos);
                stop();
                if (searchForDestination()) start();
            }
            return;
        }
        atDestination = true;
        blacklistTimer = 0;
        nonValidPositions.clear();
        animal.getLookControl().setLookAt(seekingBlockPos.getX() + 0.5D, seekingBlockPos.getY(),
                seekingBlockPos.getZ() + 0.5D, 10.0F, animal.getMaxHeadXRot());
        onArriveAtDestination();
        done = true;
    }

    protected boolean hasSecondaryTarget() {
        return false;
    }

    protected abstract boolean shouldMoveTo(BlockPos pos);

    protected boolean shouldMoveToSecondary(BlockPos pos) {
        return false;
    }

    protected abstract boolean targetStillValid();

    protected abstract void onArriveAtDestination();

    protected enum DestinationOffsets {
        NONE,
        HORIZONTAL,
        UP
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
