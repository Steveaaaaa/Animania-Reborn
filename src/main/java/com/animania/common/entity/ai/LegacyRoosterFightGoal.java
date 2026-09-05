package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.chicken.ChickenRole;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;

/** Optional rooster-versus-rooster combat controlled by the original farm setting. */
public final class LegacyRoosterFightGoal extends Goal {
    private final AnimaniaChicken rooster;
    private AnimaniaChicken target;
    private int attackCooldown;

    public LegacyRoosterFightGoal(AnimaniaChicken rooster) {
        this.rooster = rooster;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!LegacyConfig.ROOSTERS_FIGHT.get() || !LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get()
                || rooster.role() != ChickenRole.ROOSTER || rooster.getRandom().nextInt(80) != 0) return false;
        target = rooster.level().getEntitiesOfClass(AnimaniaChicken.class, rooster.getBoundingBox().inflate(16.0D),
                        bird -> bird != rooster && bird.role() == ChickenRole.ROOSTER && bird.isAlive())
                .stream().min(Comparator.comparingDouble(rooster::distanceToSqr)).orElse(null);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return LegacyConfig.ROOSTERS_FIGHT.get() && target != null && target.isAlive()
                && rooster.distanceToSqr(target) < 256.0D;
    }

    @Override
    public void tick() {
        rooster.getLookControl().setLookAt(target, 10.0F, rooster.getMaxHeadXRot());
        rooster.getNavigation().moveTo(target, 1.0D);
        attackCooldown = Math.max(0, attackCooldown - 1);
        if (attackCooldown == 0 && rooster.distanceToSqr(target) <= 2.0D) {
            attackCooldown = 20;
            rooster.doHurtTarget(target);
        }
    }

    @Override
    public void stop() {
        rooster.getNavigation().stop();
        target = null;
    }
}
