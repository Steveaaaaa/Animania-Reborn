package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import java.util.function.Predicate;

/** Sleeping/sitting guards and continued prey eligibility from the generic 1.12 target AIs. */
public final class LegacyNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final Predicate<LivingEntity> predicate;
    public LegacyNearestAttackableTargetGoal(Mob mob, Class<T> type, boolean sight) { this(mob, type, 10, sight, false, null); }
    public LegacyNearestAttackableTargetGoal(Mob mob, Class<T> type, boolean sight, Predicate<LivingEntity> predicate) {
        this(mob, type, 10, sight, false, predicate);
    }
    public LegacyNearestAttackableTargetGoal(Mob mob, Class<T> type, int chance, boolean sight,
                                             boolean nearby, Predicate<LivingEntity> predicate) {
        super(mob, type, chance, sight, nearby, predicate); this.predicate = predicate;
    }
    private boolean available() { return !mob.getData(ModAttachments.SLEEPING)
            && (!(mob instanceof TamableAnimal tame) || !tame.isInSittingPose()); }
    @Override public boolean canUse() { return available() && super.canUse(); }
    @Override public boolean canContinueToUse() {
        return available() && (predicate == null || mob.getTarget() != null && predicate.test(mob.getTarget()))
                && super.canContinueToUse();
    }
}
