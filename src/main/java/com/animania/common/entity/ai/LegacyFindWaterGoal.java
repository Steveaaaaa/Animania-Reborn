package com.animania.common.entity.ai;

import com.animania.catsdogs.block.PetBowlBlock;
import com.animania.catsdogs.block.PetBowlContent;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.common.world.block.TroughBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

/** Forge port of Animania 1.12's GenericAIFindWater. */
public final class LegacyFindWaterGoal extends LegacySearchBlockGoal {
    private final Animal waterAnimal;
    private final LegacyAnimalNeeds.Profile profile;
    private int waterFindTimer;

    public LegacyFindWaterGoal(PathfinderMob animal, LegacyAnimalNeeds.Profile profile) {
        super(animal, profile.speed(), DestinationOffsets.UP);
        this.waterAnimal = (Animal) animal;
        this.profile = profile;
    }

    @Override
    public boolean canUse() {
        if (++waterFindTimer <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (LegacyAnimalNeeds.isWatered(waterAnimal) || waterAnimal.isVehicle()
                || ModAttachments.getData(waterAnimal, ModAttachments.SLEEPING)
                || LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get()
                && !LegacyAnimalNeeds.isInteracted(waterAnimal)) {
            waterFindTimer = 0;
            return false;
        }
        if (waterAnimal.getRandom().nextInt(3) != 0) return false;
        waterFindTimer = 0;
        return searchForDestination();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !LegacyAnimalNeeds.isWatered(waterAnimal);
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        int amount = profile.halfWater() ? 50 : 100;
        return TroughBlock.hasWater(level, pos, amount)
                || PetBowlBlock.hasWater(level, pos, amount);
    }

    @Override
    protected boolean hasSecondaryTarget() {
        return true;
    }

    @Override
    protected boolean shouldMoveToSecondary(BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.WATER)) return false;
        var biome = level.getBiome(pos);
        return !biome.is(BiomeTags.IS_OCEAN)
                && !biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN)
                && !biome.is(net.minecraft.tags.BiomeTags.IS_BEACH);
    }

    @Override
    protected boolean targetStillValid() {
        return seekingBlockPos != null && (shouldMoveTo(seekingBlockPos) || shouldMoveToSecondary(seekingBlockPos));
    }

    @Override
    protected void onArriveAtDestination() {
        if (seekingBlockPos == null) return;
        int amount = profile.halfWater() ? 50 : 100;
        boolean providerConsumed = TroughBlock.consumeWater(level, seekingBlockPos, amount)
                || PetBowlBlock.consumeWater(level, seekingBlockPos, amount);
        boolean consumed = providerConsumed;
        if (!consumed && shouldMoveToSecondary(seekingBlockPos)) {
            consumed = true;
            if (LegacyConfig.WATER_REMOVED_AFTER_DRINKING.get() && !profile.halfWater()) {
                level.removeBlock(seekingBlockPos, false);
            }
        }
        if (!consumed) return;
        if (providerConsumed) LegacyAnimalNeeds.water(waterAnimal);
        else LegacyAnimalNeeds.setWatered(waterAnimal, true);
        if (profile.automaticEatAnimation()) ModAttachments.setData(waterAnimal, ModAttachments.EATING_TICKS, 80);
        waterFindTimer = 0;
    }
}
