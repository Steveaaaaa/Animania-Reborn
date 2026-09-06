package com.animania.common.entity.ai;

import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.chicken.ChickenRole;
import com.animania.farm.livestock.*;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;

/** Restores the base-class registrations after removing inherited modern animal defaults. */
public final class LegacyGoalRegistration {
    private LegacyGoalRegistration() { }
    public static void restore(Animal animal) {
        boolean cat = animal instanceof AnimaniaCat;
        boolean dog = animal instanceof AnimaniaDog;
        boolean pet = cat || dog;
        boolean cow = animal instanceof AnimaniaCow;
        boolean goat = animal instanceof AnimaniaGoat;
        boolean sheep = animal instanceof AnimaniaSheep;
        boolean pig = animal instanceof AnimaniaPig;
        boolean horse = animal instanceof AnimaniaHorse;
        boolean chicken = animal instanceof AnimaniaChicken;
        boolean peafowl = animal instanceof AnimaniaPeafowl;
        boolean rabbit = animal instanceof AnimaniaRabbit;
        AnimaniaRodent rodent = animal instanceof AnimaniaRodent r ? r : null;
        boolean hamster = rodent != null && rodent.kind() == AnimaniaRodent.Kind.HAMSTER;
        boolean ferret = rodent != null && rodent.kind().isFerret();
        boolean hedgehog = rodent != null && rodent.kind().isHedgehog();

        if (rabbit) animal.goalSelector.removeAllGoals(goal -> goal instanceof LegacyRodentGrazeGoal
                || goal.getClass().getName().contains("RaidGardenGoal"));
        if (pet || ferret || hedgehog || rabbit)
            animal.goalSelector.addGoal(rabbit ? 8 : hedgehog ? 12 : 11, new LegacyGrazeGoal(animal, false));

        if (cow || sheep) animal.goalSelector.addGoal(6, new LegacyTemptGoal(animal, 1.25D,
                stack -> stack.is(net.minecraft.world.item.Items.DANDELION)
                        || stack.is(net.minecraft.world.item.Items.POPPY), false));
        if (pig) {
            animal.goalSelector.addGoal(9, new LegacyTemptGoal(animal, 1.2D,
                    stack -> stack.is(net.minecraft.world.item.Items.CARROT_ON_A_STICK), false));
            animal.goalSelector.addGoal(10, new LegacyTemptGoal(animal, 1.2D,
                    stack -> net.minecraft.world.item.ItemStack.matches(stack,
                            new net.minecraft.world.item.ItemStack(com.animania.common.registry.ModItems.SLOP_BUCKET.get())), true));
        }
        if (cat) animal.targetSelector.removeAllGoals(goal -> goal instanceof OwnerHurtByTargetGoal
                || goal instanceof OwnerHurtTargetGoal || goal instanceof HurtByTargetGoal);
        if (animal instanceof AnimaniaDog d) {
            animal.targetSelector.removeAllGoals(goal -> goal instanceof OwnerHurtByTargetGoal || goal instanceof OwnerHurtTargetGoal);
            animal.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(d) {
                private boolean awake() { return !d.getData(ModAttachments.SLEEPING) && !d.isInSittingPose(); }
                @Override public boolean canUse() { return awake() && super.canUse(); }
                @Override public boolean canContinueToUse() { return awake() && super.canContinueToUse(); }
            });
            animal.targetSelector.addGoal(2, new OwnerHurtTargetGoal(d) {
                private boolean awake() { return !d.getData(ModAttachments.SLEEPING) && !d.isInSittingPose(); }
                @Override public boolean canUse() { return awake() && super.canUse(); }
                @Override public boolean canContinueToUse() { return awake() && super.canContinueToUse(); }
            });
        }

        animal.goalSelector.removeAllGoals(goal -> goal instanceof RandomStrollGoal
                || goal instanceof RandomLookAroundGoal || goal instanceof LegacyIdleLookGoal
                || goal instanceof LookAtPlayerGoal || goal instanceof FloatGoal || goal instanceof PanicGoal
                || goal instanceof EatBlockGoal);
        int wanderPriority = pig ? 2 : horse ? 3 : cow || goat || sheep || peafowl || rabbit ? 4
                : hamster ? 5 : chicken ? 6 : hedgehog ? 13 : 12;
        double wanderSpeed = pet || ferret ? 1.2D : rabbit ? 1.8D : hamster ? 1.1D : 1.0D;
        animal.goalSelector.addGoal(wanderPriority, new WaterAvoidingRandomStrollGoal(animal, wanderSpeed) {
            @Override public boolean canUse() {
                return !animal.getData(ModAttachments.SLEEPING)
                        && (!(animal instanceof AnimaniaHorse h) || h.level().isDay() && !h.isPullingVehicle())
                        && super.canUse();
            }
        });
        int panicPriority = horse || goat ? 0 : cow || chicken || hamster ? 1 : peafowl ? 2
                : rabbit ? 3 : pig ? 4 : sheep ? 6 : hedgehog ? 10 : 8;
        double panicSpeed = horse || cow ? 2.0D : sheep ? 2.2D : rabbit ? 2.5D
                : pet || ferret || hedgehog || pig ? 1.5D : 1.4D;
        animal.goalSelector.addGoal(panicPriority, new PanicGoal(animal, panicSpeed) {
            @Override public boolean canUse() {
                if (animal instanceof AnimaniaCow && animal.getLastHurtByMob() != null) return false;
                if (animal.isOnFire()) animal.setData(ModAttachments.SLEEPING, false);
                return super.canUse();
            }
        });
        int floatPriority = cow || goat || sheep || rabbit ? 5 : horse ? 4 : hamster ? 2 : hedgehog ? 1 : 0;
        animal.goalSelector.addGoal(floatPriority, chicken || rodent != null
                ? new LegacySmallCreatureFloatGoal(animal) : new FloatGoal(animal));
        int watchPriority = peafowl ? 5 : chicken || horse ? 7 : hamster ? 8
                : cow || goat || sheep || rabbit ? 10 : hedgehog ? 14 : 13;
        animal.goalSelector.addGoal(watchPriority, chicken || peafowl
                ? new LegacyBirdWatchGoal(animal, 6) : new LookAtPlayerGoal(animal, Player.class, 6));
        animal.goalSelector.addGoal(horse ? 8 : hamster ? 9 : pig || hedgehog ? 15 : pet || ferret ? 14 : 11,
                new LegacyIdleLookGoal(animal));
        // Keep the original priority relationships between needs, combat, sleep and play.
        for (var wrapped : java.util.List.copyOf(animal.goalSelector.getAvailableGoals())) {
            Goal goal = wrapped.getGoal();
            int priority = wrapped.getPriority();
            if (goal instanceof LegacySleepGoal) priority = pet ? 14 : ferret ? 15 : hedgehog ? 16
                    : hamster || horse || goat ? 10 : cow ? 9 : pig || chicken ? 8 : peafowl ? 6 : rabbit ? 12 : 11;
            else if (goal instanceof LegacyFindSaltLickGoal) {
                if (!(cow || goat || sheep || horse || pig)) { animal.goalSelector.removeGoal(goal); continue; }
                priority = horse ? 9 : 12;
            } else if (goal instanceof LegacyFindMudGoal) priority = 1;
            else if (goal instanceof LegacyHeadButtGoal) priority = 3;
            else if (goal instanceof LegacyFindNestGoal) priority = peafowl ? 1 : 6;
            else if (goal instanceof LegacyRaidNestGoal) priority = ferret ? 2 : 3;
            else if (goal instanceof LegacyFollowOwnerGoal) priority = hedgehog ? 11 : 7;
            else if (goal instanceof SitWhenOrderedToGoal) priority = hedgehog ? 5 : 4;
            else if (goal instanceof LegacyCatAttackGoal) priority = 6;
            else if (goal instanceof LeapAtTargetGoal && pet) priority = 5;
            else if (goal instanceof MeleeAttackGoal && (pet || ferret)) priority = 6;
            else if (goal instanceof LegacyMateGoal) priority = goat || sheep ? 5 : rabbit ? 6 : pet ? 8 : 3;
            if (priority != wrapped.getPriority()) {
                animal.goalSelector.removeGoal(goal); animal.goalSelector.addGoal(priority, goal);
            }
        }
        if (hamster || hedgehog) animal.goalSelector.addGoal(hamster ? 4 : 6, new FleeSunGoal(animal, 1.0D));
        if (goat || sheep || rabbit) animal.goalSelector.addGoal(9, new AvoidEntityGoal<>(animal, Wolf.class,
                goat ? 20 : 24, goat ? 2.2D : rabbit ? 3.0D : 2.0D, rabbit ? 3.5D : 2.2D));
        if (rabbit) animal.goalSelector.addGoal(9, new AvoidEntityGoal<>(animal, Monster.class, 16, 2.2D, 2.2D));
        if (animal instanceof AnimaniaCat c) animal.goalSelector.addGoal(4,
                new AvoidEntityGoal<>(c, Player.class, 16, 0.8D, 1.33D, p -> !c.isTame()));
        if (LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get()) {
            if (hedgehog) animal.targetSelector.addGoal(1,
                    new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(animal, Silverfish.class, false));
            if (cow && ((AnimaniaCow) animal).role() == FarmAnimalRole.FEMALE) {
                animal.goalSelector.addGoal(4, new MeleeAttackGoal(animal, 1.2D, false));
                animal.targetSelector.addGoal(1, new HurtByTargetGoal(animal).setAlertOthers());
            }
            if (peafowl) {
                animal.goalSelector.addGoal(8, new LeapAtTargetGoal(animal, 0.2F));
                animal.goalSelector.addGoal(9, new MeleeAttackGoal(animal, 1.0D, true));
            }
            if (animal instanceof AnimaniaDog d && (d.breed() == DogBreed.FOX || d.breed() == DogBreed.WOLF))
                animal.targetSelector.addGoal(4, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(animal, Chicken.class,
                        false, prey -> !d.isTame()));
            if (animal instanceof AnimaniaChicken c && c.role() == ChickenRole.ROOSTER) {
                animal.goalSelector.removeAllGoals(goal -> goal instanceof LegacyRoosterFightGoal
                        || goal instanceof MeleeAttackGoal || goal instanceof LeapAtTargetGoal);
                animal.goalSelector.addGoal(3, new LeapAtTargetGoal(animal, 0.2F));
                animal.goalSelector.addGoal(3, new MeleeAttackGoal(animal, 1.0D, true));
                animal.targetSelector.addGoal(8, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(animal, AnimaniaChicken.class,
                        80, false, true, prey -> LegacyConfig.ROOSTERS_FIGHT.get()
                        && prey instanceof AnimaniaChicken other && other.role() == ChickenRole.ROOSTER));
            }
        }
    }
}
