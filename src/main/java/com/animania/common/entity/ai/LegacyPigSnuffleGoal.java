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
import net.minecraftforge.common.Tags;

import java.util.EnumSet;

/** Port of EntityAIPigSnuffle, including its leash/forest truffle rule. */
public final class LegacyPigSnuffleGoal extends Goal {
    private final AnimaniaPig pig;
    private int eatingTimer;
    private boolean spawned;
    private boolean eaten;

    public LegacyPigSnuffleGoal(AnimaniaPig pig) {
        this.pig = pig;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        BlockPos below = pig.blockPosition().below();
        return pig.level().getBlockState(below).is(Blocks.GRASS_BLOCK)
                && pig.onGround() && pig.hurtTime == 0 && pig.getTarget() == null
                && !LegacySleepGoal.shouldSleepNow(pig)
                && !AnimaniaPig.isMud(pig.level(), below)
                && !ModAttachments.getData(pig, ModAttachments.SLEEPING)
                && !LegacyAnimalNeeds.isFed(pig)
                && pig.getRandom().nextInt(120) == 50;
    }

    @Override
    public boolean canContinueToUse() {
        return eatingTimer > 0 && pig.hurtTime == 0 && pig.getTarget() == null
                && !ModAttachments.getData(pig, ModAttachments.SLEEPING) && !LegacySleepGoal.shouldSleepNow(pig);
    }

    @Override
    public void start() {
        eatingTimer = 160;
        ModAttachments.setData(pig, ModAttachments.EATING_TICKS, 80);
        pig.getNavigation().stop();
    }

    @Override
    public void tick() {
        eatingTimer = Math.max(0, eatingTimer - 1);
        if (eatingTimer > 4 && ModAttachments.getData(pig, ModAttachments.EATING_TICKS) < 10)
            ModAttachments.setData(pig, ModAttachments.EATING_TICKS, 40);
        if (eatingTimer > 4 && eatingTimer % 16 == 0)
            pig.level().levelEvent(2001, pig.blockPosition().below(), Block.getId(pig.level().getBlockState(pig.blockPosition().below())));
        BlockPos below = pig.blockPosition().below();
        if (!pig.level().getBlockState(below).is(Blocks.GRASS_BLOCK)) {
            eatingTimer = 0;
            return;
        }
        if (eatingTimer == 4 && !LegacyAnimalNeeds.isFed(pig)) {
            pig.level().levelEvent(2001, below, Block.getId(pig.level().getBlockState(below)));
            if (com.animania.common.config.LegacyConfig.PLANTS_REMOVED_AFTER_EATING.get())
                pig.level().setBlock(below, Blocks.DIRT.defaultBlockState(), 2);
            LegacyAnimalNeeds.setFed(pig, true);
        }
        var biome = pig.level().getBiome(below);
        boolean forest = biome.is(BiomeTags.IS_FOREST);
        if (eatingTimer > 80 && forest && pig.role() != FarmAnimalRole.YOUNG
                && pig.getLeashHolder() instanceof Player) {
            pig.level().levelEvent(2001, below, Block.getId(pig.level().getBlockState(below)));
            if (!spawned) pig.level().addFreshEntity(new ItemEntity(pig.level(), below.getX() + 0.5D, below.getY() + 1.0D,
                    below.getZ() + 0.5D, new net.minecraft.world.item.ItemStack(ModItems.TRUFFLE.get(), 1 + pig.getRandom().nextInt(2))));
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
        ModAttachments.setData(pig, ModAttachments.EATING_TICKS, 0);
        spawned = false;
        eaten = false;
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
