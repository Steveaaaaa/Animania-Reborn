package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.farm.chicken.AnimaniaChicken;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;

/** Daytime nest search used by hens and peahens; an egg is inserted only after arrival. */
public final class LegacyFindNestGoal extends LegacySearchBlockGoal {
    private final Animal bird;
    private int delay;

    public LegacyFindNestGoal(PathfinderMob mob, Animal bird) {
        super(mob, 1.0D, DestinationOffsets.UP, 10);
        this.bird = bird;
    }

    @Override
    public boolean canUse() {
        if (++delay < LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        delay = 0;
        if (!bird.level().isDay() || bird.getData(ModAttachments.SLEEPING)
                || !isReady() || !wellCaredFor()) return false;
        // The old goal occasionally wandered instead of committing to a nest search.
        if (bird.getRandom().nextInt(100) == 0) return false;
        return searchForDestination();
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        if (!level.getBlockState(pos).is(ModBlocks.NEST.get()) || !canAccept(pos)) return false;
        AABB nestArea = new AABB(pos).inflate(3.0D);
        return level.getEntitiesOfClass(bird.getClass(), nestArea,
                other -> other != bird && sameLayingKind(other)).isEmpty();
    }

    @Override
    protected boolean targetStillValid() {
        return seekingBlockPos != null && isReady() && canAccept(seekingBlockPos);
    }

    @Override
    protected void onArriveAtDestination() {
        if (seekingBlockPos == null) return;
        boolean laid = bird instanceof AnimaniaChicken chicken && chicken.layEggInNest(seekingBlockPos)
                || bird instanceof AnimaniaPeafowl peafowl && peafowl.layEggInNest(seekingBlockPos);
        if (laid) delay = 0;
    }

    private boolean isReady() {
        return bird instanceof AnimaniaChicken chicken && chicken.isLookingForNest()
                || bird instanceof AnimaniaPeafowl peafowl && peafowl.isLookingForNest();
    }

    private boolean canAccept(BlockPos pos) {
        return bird instanceof AnimaniaChicken chicken && chicken.canUseNest(pos)
                || bird instanceof AnimaniaPeafowl peafowl && peafowl.canUseNest(pos);
    }

    private boolean sameLayingKind(Animal other) {
        return bird instanceof AnimaniaChicken && other instanceof AnimaniaChicken chicken
                && chicken.role() == com.animania.farm.chicken.ChickenRole.HEN
                || bird instanceof AnimaniaPeafowl && other instanceof AnimaniaPeafowl peafowl
                && peafowl.role() == com.animania.extra.peafowl.PeafowlRole.PEAHEN;
    }

    private boolean wellCaredFor() {
        return bird.getData(ModAttachments.FED) && bird.getData(ModAttachments.WATERED);
    }
}
