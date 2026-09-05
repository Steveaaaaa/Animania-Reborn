package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.FarmAnimalRole;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;

/** Daytime buck rivalry behavior from EntityAIButtHeadsGoats. */
public final class LegacyHeadButtGoal extends Goal {
    private final AnimaniaGoat buck;
    private AnimaniaGoat rival;
    private int delay;
    private int fightTimer;
    private boolean struck;

    public LegacyHeadButtGoal(AnimaniaGoat buck) {
        this.buck = buck;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (++delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get() * 20) return false;
        delay = 0;
        if (buck.role() != FarmAnimalRole.MALE || !buck.level().isDay()
                || buck.getData(ModAttachments.SLEEPING)) return false;
        rival = buck.level().getEntitiesOfClass(AnimaniaGoat.class, buck.getBoundingBox().inflate(10.0D),
                        goat -> goat != buck && goat.role() == FarmAnimalRole.MALE && goat.isAlive()
                                && !goat.getData(ModAttachments.SLEEPING))
                .stream().min(Comparator.comparingDouble(buck::distanceToSqr)).orElse(null);
        return rival != null && buck.getRandom().nextInt(20) != 0;
    }

    @Override
    public void start() {
        fightTimer = 100 + buck.getRandom().nextInt(50);
        struck = false;
    }

    @Override
    public boolean canContinueToUse() {
        return rival != null && rival.isAlive() && fightTimer-- > 0;
    }

    @Override
    public void tick() {
        buck.getLookControl().setLookAt(rival, 10.0F, buck.getMaxHeadXRot());
        buck.getNavigation().moveTo(rival, 1.3D);
        rival.getLookControl().setLookAt(buck, 10.0F, rival.getMaxHeadXRot());
        if (!struck && buck.distanceToSqr(rival) < 3.0D) {
            struck = true;
            rival.knockback(1.0D, buck.getX() - rival.getX(), buck.getZ() - rival.getZ());
            buck.playSound(SoundEvents.GOAT_RAM_IMPACT, 0.8F, 0.9F + buck.getRandom().nextFloat() * 0.2F);
        }
    }

    @Override
    public void stop() {
        buck.getNavigation().stop();
        rival = null;
        delay = 0;
    }
}
