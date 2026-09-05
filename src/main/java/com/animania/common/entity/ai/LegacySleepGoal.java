package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Bed-searching sleep AI corresponding to GenericAISleep in Animania 1.12. */
public final class LegacySleepGoal extends LegacySearchBlockGoal {
    private final Animal sleeper;
    private final Block preferred;
    private final Block backup;
    private int delay;

    public LegacySleepGoal(PathfinderMob mob, Animal sleeper) {
        super(mob, 0.8D, DestinationOffsets.UP);
        this.sleeper = sleeper;
        String key = bedKey(sleeper);
        this.preferred = resolve(LegacyConfig.PREFERRED_BEDS.get(key).get());
        this.backup = resolve(LegacyConfig.BACKUP_BEDS.get(key).get());
    }

    @Override
    public boolean canUse() {
        if (!LegacyConfig.ANIMALS_SLEEP.get() || sleeper.isPassenger()
                || sleeper instanceof TamableAnimal tame && tame.isInSittingPose()) return false;
        if (++delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get() + sleeper.getRandom().nextInt(100)) {
            return false;
        }
        delay = 0;
        if (sleeper.getData(ModAttachments.SLEEPING)) return false;
        return shouldSleepNow(sleeper) && !sleeper.level().isRainingAt(sleeper.blockPosition())
                && sleeper.getRandom().nextInt(3) == 0 && searchForDestination();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !sleeper.getData(ModAttachments.SLEEPING)
                && shouldSleepNow(sleeper);
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        return preferred != Blocks.AIR && level.getBlockState(pos).is(preferred);
    }

    @Override
    protected boolean hasSecondaryTarget() {
        return backup != Blocks.AIR;
    }

    @Override
    protected boolean shouldMoveToSecondary(BlockPos pos) {
        return level.getBlockState(pos).is(backup);
    }

    @Override
    protected boolean targetStillValid() {
        if (seekingBlockPos == null) return false;
        Block block = level.getBlockState(seekingBlockPos).getBlock();
        return block == preferred || block == backup;
    }

    @Override
    protected void onArriveAtDestination() {
        sleeper.setData(ModAttachments.SLEEPING, true);
        sleeper.getNavigation().stop();
        delay = 0;
    }

    public static boolean shouldSleepNow(Animal animal) {
        long time = animal.level().getDayTime() % 24_000L;
        if (animal instanceof AnimaniaRodent rodent
                && (rodent.kind() == AnimaniaRodent.Kind.HAMSTER || rodent.kind().isHedgehog())) {
            return time < 13_000L;
        }
        if (animal instanceof com.animania.extra.rabbit.AnimaniaRabbit) {
            return time > 20_000L || time > 10_000L && time < 15_000L;
        }
        return time >= 13_000L;
    }

    private static String bedKey(Animal animal) {
        if (animal instanceof com.animania.farm.livestock.AnimaniaCow) return "cow";
        if (animal instanceof com.animania.farm.livestock.AnimaniaGoat) return "goat";
        if (animal instanceof com.animania.farm.livestock.AnimaniaHorse) return "horse";
        if (animal instanceof com.animania.farm.livestock.AnimaniaPig) return "pig";
        if (animal instanceof com.animania.farm.livestock.AnimaniaSheep) return "sheep";
        if (animal instanceof com.animania.farm.chicken.AnimaniaChicken) return "chicken";
        if (animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl) return "peacock";
        if (animal instanceof com.animania.extra.rabbit.AnimaniaRabbit) return "rabbit";
        if (animal instanceof com.animania.catsdogs.cat.AnimaniaCat) return "cat";
        if (animal instanceof com.animania.catsdogs.dog.AnimaniaDog) return "dog";
        if (animal instanceof AnimaniaRodent rodent) {
            if (rodent.kind() == AnimaniaRodent.Kind.HAMSTER) return "hamster";
            if (rodent.kind().isHedgehog()) return "hedgehog";
            return "ferret";
        }
        throw new IllegalArgumentException("No legacy bed mapping for " + animal.getType());
    }

    private static Block resolve(String configured) {
        if (configured == null || configured.isBlank()) return Blocks.AIR;
        if (configured.equals("minecraft:grass")) return Blocks.GRASS_BLOCK;
        ResourceLocation id = ResourceLocation.tryParse(configured);
        return id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(id);
    }
}
