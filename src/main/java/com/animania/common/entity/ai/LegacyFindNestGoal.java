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
public final class LegacyFindNestGoal extends net.minecraft.world.entity.ai.goal.Goal {
    private final Animal bird;
    private int delay;

    public LegacyFindNestGoal(PathfinderMob mob, Animal bird) {
        setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.bird = bird;
    }

    @Override
    public boolean canUse() {
        if (++delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (!bird.level().isDay() || ModAttachments.getData(bird, ModAttachments.SLEEPING) || !wellCaredFor()) {
            delay = 0; return false;
        }
        if (bird.getRandom().nextInt(100) == 0) {
            var pos = net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(bird, 20, 4);
            if (pos != null) {
                delay = 0; stop();
                bird.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0D);
                bird.getLookControl().setLookAt(pos.x, pos.y, pos.z, 0, 0);
            }
            return false;
        }
        BlockPos feet = bird.blockPosition();
        if (isNest(feet)) {
            if (bird instanceof AnimaniaChicken chicken) chicken.layEggInNest(feet);
            else if (bird instanceof AnimaniaPeafowl peafowl) peafowl.layEggInNest(feet);
            delay = 0; return false;
        }
        for (int x = -10; x < 10; x++) for (int y = -3; y < 3; y++) for (int z = -10; z < 10; z++) {
            BlockPos nest = pos(x, y, z);
            if (!canAccept(nest)) continue;
            // Hens avoid an occupied nest; the original peahen goal has no crowd gate.
            if (bird instanceof AnimaniaPeafowl || bird.level().getEntitiesOfClass(AnimaniaChicken.class,
                    new AABB(nest).inflate(3), other -> sameLayingKind(other)).isEmpty()) return true;
        }
        delay = 0; return false;
    }

    @Override public void start() {
        if (isNest(bird.blockPosition()) || !bird.getNavigation().isDone()) return;
        BlockPos nest = bird.blockPosition();
        int nearest = 24;
        boolean found = false;
        for (int x = -10; x < 10; x++) for (int y = -3; y < 3; y++) for (int z = -10; z < 10; z++) {
            BlockPos candidate = pos(x, y, z);
            if (!canAccept(candidate)) continue;
            found = true;
            int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
            if (distance >= nearest) continue;
            nearest = distance;
            if (bird.getX() < nest.getX() && isNest(candidate.east())) x++;
            if (bird.getZ() < nest.getZ() && isNest(pos(x, y, z + 1))) z++;
            nest = pos(x, y, z);
        }
        if (found && isNest(nest) && (bird instanceof AnimaniaPeafowl
                || bird.level().getEntities(bird, bird.getBoundingBox().expandTowards(1, 1, 1)).isEmpty())) {
            bird.getNavigation().moveTo(nest.getX() + 0.5D, nest.getY(), nest.getZ() + 0.5D, 1.0D);
            bird.getLookControl().setLookAt(nest.getX(), nest.getY(), nest.getZ(), 10, 10);
        }
    }
    @Override public boolean canContinueToUse() { return !bird.getNavigation().isDone(); }
    @Override public void stop() { bird.getNavigation().stop(); }
    private boolean isNest(BlockPos pos) { return bird.level().getBlockState(pos).is(ModBlocks.NEST.get()); }
    private BlockPos pos(int x, int y, int z) {
        return BlockPos.containing(bird.getX() + x, bird.getY() + y, bird.getZ() + z);
    }

    private boolean isReady() {
        return bird instanceof AnimaniaChicken chicken && chicken.isLookingForNest()
                || bird instanceof AnimaniaPeafowl peafowl && peafowl.isLookingForNest();
    }

    private boolean canAccept(BlockPos pos) {
        if (!isNest(pos)) return false;
        var state = bird.level().getBlockState(pos);
        return bird instanceof AnimaniaChicken chicken && com.animania.farm.world.block.NestBlock.canAccept(state, chicken.breed())
                || bird instanceof AnimaniaPeafowl peafowl && com.animania.farm.world.block.NestBlock.canAcceptPeafowl(state, peafowl.breed());
    }

    private boolean sameLayingKind(Animal other) {
        return bird instanceof AnimaniaChicken && other instanceof AnimaniaChicken chicken
                && chicken.role() == com.animania.farm.chicken.ChickenRole.HEN
                || bird instanceof AnimaniaPeafowl && other instanceof AnimaniaPeafowl peafowl
                && peafowl.role() == com.animania.extra.peafowl.PeafowlRole.PEAHEN;
    }

    private boolean wellCaredFor() {
        return ModAttachments.getData(bird, ModAttachments.FED) && ModAttachments.getData(bird, ModAttachments.WATERED);
    }
}
