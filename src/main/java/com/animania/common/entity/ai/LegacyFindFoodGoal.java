package com.animania.common.entity.ai;

import com.animania.catsdogs.block.PetBowlBlock;
import com.animania.catsdogs.block.PetBowlContent;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModFluids;
import com.animania.common.world.block.TroughBlock;
import com.animania.farm.livestock.AnimaniaPig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Forge port of Animania 1.12's GenericAIFindFood. */
public final class LegacyFindFoodGoal extends LegacySearchBlockGoal {
    private final Animal foodAnimal;
    private final LegacyAnimalNeeds.Profile profile;
    private int foodDelay;

    public LegacyFindFoodGoal(PathfinderMob animal, LegacyAnimalNeeds.Profile profile) {
        super(animal, profile.speed(), DestinationOffsets.HORIZONTAL);
        this.foodAnimal = (Animal) animal;
        this.profile = profile;
    }

    @Override
    public boolean canUse() {
        if (++foodDelay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (LegacyAnimalNeeds.isFed(foodAnimal) || foodAnimal.isVehicle()
                || ModAttachments.getData(foodAnimal, ModAttachments.SLEEPING)
                || LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get()
                && !LegacyAnimalNeeds.isInteracted(foodAnimal)) {
            foodDelay = 0;
            return false;
        }
        if (foodAnimal.getRandom().nextInt(3) != 0) return false;
        foodDelay = 0;
        return searchForDestination();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !LegacyAnimalNeeds.isFed(foodAnimal);
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        return TroughBlock.hasFoodFor(level, pos, foodAnimal)
                || PetBowlBlock.hasContent(level, pos, foodAnimal, PetBowlContent.FOOD);
    }

    @Override
    protected boolean hasSecondaryTarget() {
        return profile.eatBlocks();
    }

    @Override
    protected boolean shouldMoveToSecondary(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (foodAnimal instanceof AnimaniaPig && isSlopSource(pos)) return true;
        return switch (profile.foodBlockMode()) {
            case NONE -> false;
            case SEEDS -> state.is(ModBlocks.SEEDS.get());
            case RABBIT -> state.is(Blocks.CARROTS)
                    || state.getBlock() instanceof TallGrassBlock
                    || state.getBlock() instanceof FlowerBlock;
            case DEFAULT -> state.getBlock() instanceof BushBlock;
        };
    }

    @Override
    protected boolean targetStillValid() {
        return seekingBlockPos != null && (shouldMoveTo(seekingBlockPos)
                || profile.eatBlocks() && shouldMoveToSecondary(seekingBlockPos));
    }

    @Override
    protected void onArriveAtDestination() {
        if (seekingBlockPos == null) return;
        boolean worldSlop = isSlopSource(seekingBlockPos);
        boolean slop = TroughBlock.containsSlop(level, seekingBlockPos) || worldSlop;
        boolean providerConsumed = TroughBlock.consumeFood(level, seekingBlockPos, foodAnimal)
                || PetBowlBlock.consume(level, seekingBlockPos, foodAnimal, PetBowlContent.FOOD);
        boolean consumed = providerConsumed;
        if (!consumed && profile.eatBlocks() && shouldMoveToSecondary(seekingBlockPos)) {
            consumed = true;
            if (worldSlop || LegacyConfig.PLANTS_REMOVED_AFTER_EATING.get()) {
                level.destroyBlock(seekingBlockPos, false);
            }
        }
        if (!consumed) return;
        // The 1.12 provider path deliberately counted as hand feeding; eating a
        // plant or a placed slop source did not mark the animal as interacted.
        LegacyAnimalNeeds.feed(foodAnimal, providerConsumed,
                slop && foodAnimal instanceof AnimaniaPig);
        if (profile.automaticEatAnimation()) ModAttachments.setData(foodAnimal, ModAttachments.EATING_TICKS, 80);
        foodDelay = 0;
    }

    private boolean isSlopSource(BlockPos pos) {
        var fluid = level.getFluidState(pos);
        return fluid.isSource() && fluid.getType().isSame(ModFluids.SLOP.source());
    }
}
