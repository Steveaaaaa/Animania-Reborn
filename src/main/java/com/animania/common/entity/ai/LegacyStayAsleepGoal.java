package com.animania.common.entity.ai;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** The legacy watch/idle/wander/target goals all refused to run while asleep. */
public final class LegacyStayAsleepGoal extends Goal {
    private final Animal animal;

    public LegacyStayAsleepGoal(Animal animal) {
        this.animal = animal;
        setFlags(EnumSet.allOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return ModAttachments.getData(animal, ModAttachments.SLEEPING);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        tick();
    }

    @Override
    public void tick() {
        animal.getNavigation().stop();
        animal.setJumping(false);
        animal.setSpeed(0.0F);
        animal.getMoveControl().setWantedPosition(animal.getX(), animal.getY(), animal.getZ(), 0.0D);
        // Replace a pending watch-player request left in LookControl on entry.
        var forward = animal.getViewVector(1.0F);
        animal.getLookControl().setLookAt(animal.getX() + forward.x,
                animal.getEyeY() + forward.y, animal.getZ() + forward.z, 0.0F, 0.0F);
    }
}
