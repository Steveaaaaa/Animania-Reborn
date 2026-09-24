package com.animania.modern;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.entity.ai.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.schedule.Activity;

public final class ModernGoals {
    private ModernGoals() {}

    public static void install(Animal animal, LegacyAnimalNeeds.Profile profile) {
        animal.goalSelector.removeAllGoals(goal -> goal instanceof net.minecraft.world.entity.ai.goal.BreedGoal
                || goal instanceof net.minecraft.world.entity.ai.goal.TemptGoal
                || goal instanceof net.minecraft.world.entity.ai.goal.FollowParentGoal);
        add(animal, 2, new LegacyFindWaterGoal(animal, profile));
        add(animal, 3, new LegacyFindFoodGoal(animal, profile));
        add(animal, 4, new LegacyTemptGoal(animal, 1.1D, animal instanceof ModernFox));
        add(animal, 5, new LegacyFollowParentGoal(animal, animal, 1.1D));
        add(animal, 5, new NursingPauseGoal(animal));
        add(animal, 5, new FamilyCareGoal(animal));
        add(animal, 6, new LegacyMateGoal(animal, animal, 1.0D));
        if (animal instanceof MountainGoat) {
            add(animal, 7, new LegacyGrazeGoal(animal));
            add(animal, 9, new FarmActivityGoal(animal, FarmActivityGoal.RUMINATE));
            add(animal, 10, new FarmHerdGoal(animal));
            add(animal, 11, new GoatExploreGoal(animal));
            add(animal, 12, new LegacyFindSaltLickGoal(animal, animal));
            add(animal, 13, new LegacySleepGoal(animal, animal));
            add(animal, -1, new LegacyStayAsleepGoal(animal));
        }
    }

    private static void add(Animal animal, int priority, Goal delegate) {
        animal.goalSelector.addGoal(priority, new CareGoal(animal, delegate));
    }

    /** Let a native jump or ram finish before a husbandry goal takes control. */
    public static final class CareGoal extends Goal {
        private final Animal animal;
        private final Goal delegate;
        private CareGoal(Animal animal, Goal delegate) {
            this.animal = animal;
            this.delegate = delegate;
            setFlags(delegate.getFlags());
        }
        @Override public boolean canUse() {
            if (animal instanceof MountainGoat goat && (!goat.onGround()
                    || goat.getBrain().isActive(Activity.RAM)
                    || goat.getBrain().isActive(Activity.LONG_JUMP)
                    || goat.getBrain().hasMemoryValue(MemoryModuleType.LONG_JUMP_MID_JUMP))) return false;
            if (animal instanceof ModernFox fox && (fox.isPouncing() || fox.isSleeping())) return false;
            return delegate.canUse();
        }
        @Override public boolean canContinueToUse() { return delegate.canContinueToUse(); }
        @Override public boolean isInterruptable() { return delegate.isInterruptable(); }
        @Override public void start() { delegate.start(); }
        @Override public void tick() { delegate.tick(); }
        @Override public void stop() { delegate.stop(); }
        @Override public boolean requiresUpdateEveryTick() { return delegate.requiresUpdateEveryTick(); }
    }
}
