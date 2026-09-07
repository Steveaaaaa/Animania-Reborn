package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.FarmAnimalRole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.AABB;
import java.util.EnumSet;

/** EntityAIFindMud: entertainment, capacity, search order and role-specific approach. */
public final class LegacyFindMudGoal extends Goal {
    private final AnimaniaPig pig;
    private int delay;
    public LegacyFindMudGoal(AnimaniaPig pig) {
        this.pig = pig;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean canUse() {
        if (++delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get()) return false;
        if (pig.hasPlayed() || !pig.level().isDay() || ModAttachments.getData(pig, ModAttachments.SLEEPING)) {
            delay = 0;
            return false;
        }
        if (pig.getRandom().nextInt(100) == 0) {
            var pos = DefaultRandomPos.getPos(pig, 20, 4);
            if (pos != null) {
                delay = 0;
                pig.getNavigation().stop();
                pig.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.2D);
            }
            return false;
        }
        if (pig.isInMud()) { pig.refreshPlay(); delay = 0; return false; }
        for (int x = -10; x < 10; x++) for (int y = -2; y < 2; y++) for (int z = -10; z < 10; z++) {
            BlockPos pos = BlockPos.containing(pig.getX() + x, pig.getY() + y, pig.getZ() + z);
            if (AnimaniaPig.isMud(pig.level(), pos)
                    && pig.level().getEntitiesOfClass(AnimaniaPig.class, new AABB(pos).inflate(2)).size() < 2) {
                if (pig.getRandom().nextInt(200) == 0 || pig.horizontalCollision
                        && pig.getDeltaMovement().x == 0 && pig.getDeltaMovement().z == 0) {
                    delay = 0;
                    return false;
                }
                return true;
            }
        }
        delay = 0;
        return false;
    }
    @Override public void start() {
        BlockPos mud = pig.blockPosition();
        int nearest = 24;
        boolean found = false;
        search:
        for (int x = -10; x < 10; x++) for (int y = -2; y < 2; y++) for (int z = -10; z < 10; z++) {
            BlockPos pos = BlockPos.containing(pig.getX() + x, pig.getY() + y, pig.getZ() + z);
            if (!AnimaniaPig.isMud(pig.level(), pos)) continue;
            found = true;
            int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
            if (distance >= nearest) continue;
            nearest = distance;
            boolean adjacent = false;
            if (pig.getX() > mud.getX() && AnimaniaPig.isMud(pig.level(), pos.east())) {
                pos = pos.east(); x++; adjacent = true;
            }
            if (pig.getZ() > mud.getZ() && AnimaniaPig.isMud(pig.level(), pos.south())) {
                pos = pos.south(); z++; adjacent = true;
            }
            mud = pos;
            if (adjacent) break search;
        }
        if (!found || !AnimaniaPig.isMud(pig.level(), mud) || pig.isInMud()) return;
        int offset = 0;
        if (pig.role() != FarmAnimalRole.FEMALE) {
            if (pig.getX() < mud.getX() && pig.getZ() < mud.getZ())
                offset = pig.role() == FarmAnimalRole.YOUNG ? 2 : 3;
            else if (pig.role() == FarmAnimalRole.YOUNG || pig.getX() > mud.getX() && pig.getZ() > mud.getZ()) offset = -1;
        }
        pig.getNavigation().moveTo(mud.getX() + offset, mud.getY(), mud.getZ() + offset, 1.2D);
    }
    @Override public boolean canContinueToUse() { return !pig.getNavigation().isDone(); }
    @Override public void stop() { pig.getNavigation().stop(); }
}
