package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.farm.world.block.NestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import java.util.EnumSet;

/** Original ferret/hedgehog two-pass nest and crop search; consumption occurs at the feet. */
public final class LegacyRaidNestGoal extends Goal {
    private final AnimaniaRodent rodent;
    private int delay;
    public LegacyRaidNestGoal(AnimaniaRodent rodent) {
        this.rodent = rodent; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean canUse() {
        if (++delay <= (rodent.kind().isHedgehog() ? 60 : LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get())) return false;
        if (LegacyAnimalNeeds.isFed(rodent) || rodent.getData(ModAttachments.SLEEPING)) { delay = 0; return false; }
        if (rodent.kind().isFerret() && rodent.getRandom().nextInt(100) == 0) {
            var pos = DefaultRandomPos.getPos(rodent, 20, 4);
            if (pos != null) { delay = 0; stop(); rodent.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0D); }
            return false;
        }
        BlockPos feet = rodent.blockPosition();
        if (rodent.level().getBlockState(feet).is(ModBlocks.NEST.get())) {
            if (NestBlock.takeChickenEggForPredator(rodent.level(), feet)) {
                LegacyAnimalNeeds.setFed(rodent, true);
                LegacyAnimalNeeds.setWatered(rodent, true);
                rodent.setData(ModAttachments.EATING_TICKS, 80);
                delay = 0; return false;
            }
            if (rodent.level().getBlockState(feet).getValue(NestBlock.EGGS) == 0) { delay = 0; return false; }
        }
        if (isCrop(feet)) {
            LegacyAnimalNeeds.setFed(rodent, true);
            rodent.setData(ModAttachments.EATING_TICKS, 80);
            if (LegacyConfig.PLANTS_REMOVED_AFTER_EATING.get()) rodent.level().destroyBlock(feet, false);
            delay = 0; return false;
        }
        for (int x = -16; x < 16; x++) for (int y = -3; y < 3; y++) for (int z = -16; z < 16; z++) {
            if (isFood(pos(x, y, z))) {
                if (rodent.getRandom().nextInt(200) == 0 || rodent.horizontalCollision
                        && rodent.getDeltaMovement().x == 0 && rodent.getDeltaMovement().z == 0) { delay = 0; return false; }
                return true;
            }
        }
        delay = 0; return false;
    }
    @Override public void start() {
        BlockPos food = rodent.blockPosition();
        int nearest = 24;
        boolean found = false;
        for (int x = -16; x < 16; x++) for (int y = -3; y < 3; y++) for (int z = -16; z < 16; z++) {
            BlockPos candidate = pos(x, y, z);
            if (!isFood(candidate)) continue;
            found = true;
            int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
            if (distance >= nearest) continue;
            nearest = distance;
            boolean nest = isChickenNest(candidate);
            if (rodent.getX() < food.getX() && (nest || isCrop(candidate.east()))) x++;
            if (rodent.getZ() < food.getZ() && (nest || isCrop(pos(x, y, z + 1)))) z++;
            food = pos(x, y, z);
        }
        if (found && isFood(food) && !rodent.getNavigation().moveTo(food.getX() + (isChickenNest(food) ? 0.7D : 0),
                food.getY(), food.getZ(), 1.0D)) delay = 0;
    }
    @Override public boolean canContinueToUse() { return !rodent.getNavigation().isDone(); }
    @Override public void stop() { rodent.getNavigation().stop(); }
    private BlockPos pos(int x, int y, int z) { return BlockPos.containing(rodent.getX() + x, rodent.getY() + y, rodent.getZ() + z); }
    private boolean isFood(BlockPos pos) { return isChickenNest(pos) || isCrop(pos); }
    private boolean isChickenNest(BlockPos pos) {
        var state = rodent.level().getBlockState(pos);
        return state.is(ModBlocks.NEST.get()) && state.getValue(NestBlock.EGGS) > 0
                && state.getValue(NestBlock.BREED).chickenBreed() != null;
    }
    private boolean isCrop(BlockPos pos) {
        if (!rodent.kind().isHedgehog()) return false;
        var block = rodent.level().getBlockState(pos).getBlock();
        return block == Blocks.CARROTS || block == Blocks.BEETROOTS || block == Blocks.POTATOES;
    }
}
