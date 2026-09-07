package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;

/** EntityAICatAttack inherits the stalking/sprinting speeds of OcelotAttack. */
public final class LegacyCatAttackGoal extends OcelotAttackGoal {
    private final Mob cat;
    public LegacyCatAttackGoal(Mob cat) { super(cat); this.cat = cat; }
    @Override public boolean canUse() { return !ModAttachments.getData(cat, ModAttachments.SLEEPING) && super.canUse(); }
    @Override public boolean canContinueToUse() { return !ModAttachments.getData(cat, ModAttachments.SLEEPING) && super.canContinueToUse(); }
}
