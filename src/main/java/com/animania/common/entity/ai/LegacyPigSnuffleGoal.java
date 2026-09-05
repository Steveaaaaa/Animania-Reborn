package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModItems;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.FarmAnimalRole;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.EnumSet;

/** Port of EntityAIPigSnuffle, including its leash/forest truffle rule. */
public final class LegacyPigSnuffleGoal extends Goal {
    private final AnimaniaPig pig;
    private int eatingTimer;
    private boolean spawned;
    private boolean eaten;

    public LegacyPigSnuffleGoal(AnimaniaPig pig) {
        this.pig = pig;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockPos below = pig.blockPosition().below();
        return !pig.level().getBlockState(below).is(ModBlocks.MUD.get())
                && !pig.getData(ModAttachments.SLEEPING)
                && !LegacyAnimalNeeds.isFed(pig)
                && pig.getRandom().nextInt(120) == 50;
    }

    @Override
    public boolean canContinueToUse() {
        return eatingTimer > 0;
    }

    @Override
    public void start() {
        eatingTimer = 160;
        pig.setData(ModAttachments.EATING_TICKS, 80);
        pig.getNavigation().stop();
    }

    @Override
    public void tick() {
        eatingTimer = Math.max(0, eatingTimer - 1);
        BlockPos below = pig.blockPosition().below();
        if (!pig.level().getBlockState(below).is(Blocks.GRASS_BLOCK)) {
            eatingTimer = 0;
            return;
        }
        var biome = pig.level().getBiome(below);
        boolean forest = biome.is(BiomeTags.IS_FOREST) || biome.is(Tags.Biomes.IS_FOREST);
        if (eatingTimer > 80 && forest && pig.role() != FarmAnimalRole.YOUNG
                && pig.getLeashHolder() instanceof Player && !spawned) {
            pig.level().levelEvent(2001, below, Block.getId(pig.level().getBlockState(below)));
            pig.spawnAtLocation(ModItems.TRUFFLE.get(), 1 + pig.getRandom().nextInt(2));
            spawned = true;
        }
        if (eatingTimer < 100 && !eaten) {
            for (ItemEntity item : pig.level().getEntitiesOfClass(ItemEntity.class,
                    pig.getBoundingBox().inflate(3.0D))) {
                if (!item.getItem().is(ModItems.TRUFFLE.get())) continue;
                item.getItem().shrink(64);
                if (item.getItem().isEmpty()) item.discard();
                LegacyAnimalNeeds.setFed(pig, true);
                eaten = true;
            }
        }
    }

    @Override
    public void stop() {
        eatingTimer = 0;
        spawned = false;
        eaten = false;
    }
}
