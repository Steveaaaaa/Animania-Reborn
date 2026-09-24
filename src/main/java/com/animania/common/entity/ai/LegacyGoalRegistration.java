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
                private boolean awake() { return !ModAttachments.getData(d, ModAttachments.SLEEPING) && !d.isInSittingPose(); }
                @Override public boolean canUse() { return awake() && super.canUse(); }
                @Override public boolean canContinueToUse() { return awake() && super.canContinueToUse(); }
            });
            animal.targetSelector.addGoal(2, new OwnerHurtTargetGoal(d) {
                private boolean awake() { return !ModAttachments.getData(d, ModAttachments.SLEEPING) && !d.isInSittingPose(); }
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
            private boolean busy() {
                return com.animania.common.entity.FamilyLifecycle.staysNearBirthplace(animal)
                        || animal.isVehicle() || ModAttachments.getData(animal, ModAttachments.FARM_ACTIVITY) != 0
                        || ((cow || sheep || chicken || peafowl || pig || goat || horse || pet || rabbit || rodent != null)
                            && LegacyConfig.ANIMALS_SLEEP.get() && LegacySleepGoal.shouldSleepNow(animal))
                        || animal.goalSelector.getAvailableGoals().stream().anyMatch(goal ->
                        goal.isRunning() && (goal.getGoal() instanceof LegacyGrazeGoal
                        || goal.getGoal() instanceof LegacyPigSnuffleGoal || goal.getGoal() instanceof LegacyTemptGoal)) || ModAttachments.getData(animal, ModAttachments.EATING_TICKS) > 0
                        || animal.level().players().stream().anyMatch(player -> !player.isSpectator()
                        && player.distanceToSqr(animal) < 100
                        && (animal.isFood(player.getMainHandItem()) || animal.isFood(player.getOffhandItem())));
            }
            @Override public boolean canContinueToUse() { return !busy() && super.canContinueToUse(); }
            @Override public boolean canUse() {
                return !ModAttachments.getData(animal, ModAttachments.SLEEPING)
                        && (!(animal instanceof AnimaniaHorse h) || h.level().isDay() && !h.isPullingVehicle())
                        && !busy() && super.canUse();
            }
        });
        int panicPriority = horse || goat ? 0 : cow || chicken || hamster ? 1 : peafowl ? 2
                : rabbit ? 3 : pig ? 4 : sheep ? 6 : hedgehog ? 10 : 8;
        double panicSpeed = horse || cow ? 2.0D : sheep ? 2.2D : rabbit ? 2.5D
                : pet || ferret || hedgehog || pig ? 1.5D : 1.4D;
        animal.goalSelector.addGoal(panicPriority, new PanicGoal(animal, panicSpeed) {
            @Override public boolean canUse() {
                if (animal instanceof AnimaniaCow && animal.getLastHurtByMob() != null) return false;
                if (animal.isOnFire()) ModAttachments.setData(animal, ModAttachments.SLEEPING, false);
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
            } else if (goal instanceof LegacyFindMudGoal || goal instanceof LegacyFollowMateHorseGoal) priority = 1;
            else if (goal instanceof LegacyHeadButtGoal) priority = 6;
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
        if (cow || sheep || chicken || peafowl || pig || goat || horse) {
            for (var wrapped : java.util.List.copyOf(animal.goalSelector.getAvailableGoals())) {
                Goal goal = wrapped.getGoal();
                if (goal instanceof LegacyFindMudGoal || goal instanceof LegacyFollowMateHorseGoal) { animal.goalSelector.removeGoal(goal); continue; }
                int priority = goal instanceof PanicGoal || goal instanceof FloatGoal ? 0
                        : goal instanceof AvoidEntityGoal ? 1
                        : goal instanceof LookAtPlayerGoal || goal instanceof LegacyIdleLookGoal ? 14
                        : goal instanceof LegacyTemptGoal ? 2
                        : goal instanceof LegacySleepGoal ? 3
                        : goal instanceof LegacyFindFoodGoal || goal instanceof LegacyFindWaterGoal
                            || goal instanceof LegacyGrazeGoal || goal instanceof LegacyPigSnuffleGoal ? 4
                        : goal instanceof WaterAvoidingRandomStrollGoal ? 12 : wrapped.getPriority();
                if (priority != wrapped.getPriority()) {
                    animal.goalSelector.removeGoal(goal);
                    animal.goalSelector.addGoal(priority, goal);
                }
            }
            if (cow || sheep || goat) {
                animal.goalSelector.addGoal(8, new FarmActivityGoal(animal, FarmActivityGoal.RUMINATE));
                animal.goalSelector.addGoal(9, new FarmHerdGoal(animal));
            }
            if (goat) {
                animal.goalSelector.addGoal(4, new GoatBrowseGoal((AnimaniaGoat) animal));
                animal.goalSelector.addGoal(10, new GoatExploreGoal((AnimaniaGoat) animal));
            }
            if (horse) {
                animal.goalSelector.addGoal(1, new HorseAlertGoal((AnimaniaHorse) animal));
                animal.goalSelector.addGoal(8, new FarmActivityGoal(animal, FarmActivityGoal.STANDING_REST));
                animal.goalSelector.addGoal(9, new FarmHerdGoal(animal));
            }
            if (chicken || peafowl) {
                animal.goalSelector.addGoal(10, new FarmActivityGoal(animal, FarmActivityGoal.PREEN));
                animal.goalSelector.addGoal(8, new FarmActivityGoal(animal, FarmActivityGoal.DUST_BATH));
                if (chicken) animal.goalSelector.addGoal(9, new FarmActivityGoal(animal, FarmActivityGoal.FORAGE));
            }
            if (pig) animal.goalSelector.addGoal(8, new FarmActivityGoal(animal, FarmActivityGoal.WALLOW));
        }
        if (hamster || hedgehog) animal.goalSelector.addGoal(hamster ? 4 : 6, new FleeSunGoal(animal, 1.0D));
        if (goat || sheep || rabbit) animal.goalSelector.addGoal(rabbit ? 9 : 1, new AvoidEntityGoal<>(animal, Wolf.class,
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
            if (animal instanceof AnimaniaDog d && (d.breed() == DogBreed.FOX || d.breed().isWolf()))
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
        boolean domesticCat = animal instanceof AnimaniaCat c && c.breed() != com.animania.catsdogs.cat.CatBreed.OCELOT;
        boolean domesticDog = animal instanceof AnimaniaDog d && !d.breed().isWolf() && d.breed() != DogBreed.FOX;
        if (domesticCat || domesticDog) {
            for (var wrapped : java.util.List.copyOf(animal.goalSelector.getAvailableGoals())) {
                if (wrapped.getGoal() instanceof LegacySleepGoal) {
                    animal.goalSelector.removeGoal(wrapped.getGoal());
                    animal.goalSelector.addGoal(8, wrapped.getGoal());
                }
            }
            TamableAnimal tame = (TamableAnimal) animal;
            if (domesticCat) {
                animal.goalSelector.addGoal(9, new PetActivityGoal(tame, FarmActivityGoal.GROOM));
                animal.goalSelector.addGoal(10, new PetActivityGoal(tame, FarmActivityGoal.PET_EXPLORE));
                animal.goalSelector.addGoal(11, new PetActivityGoal(tame, FarmActivityGoal.PET_REST));
            } else {
                animal.goalSelector.addGoal(9, new PetActivityGoal(tame, FarmActivityGoal.GREET));
                animal.goalSelector.addGoal(10, new PetActivityGoal(tame, FarmActivityGoal.SNIFF));
            }
        }

        if (rabbit || rodent != null) {
            for (var wrapped : java.util.List.copyOf(animal.goalSelector.getAvailableGoals())) {
                Goal goal = wrapped.getGoal();
                if (goal instanceof FleeSunGoal) { animal.goalSelector.removeGoal(goal); continue; }
                int priority = goal instanceof FloatGoal ? 0
                        : goal instanceof PanicGoal || goal instanceof AvoidEntityGoal ? 1
                        : goal instanceof SitWhenOrderedToGoal ? 2
                        : goal instanceof LegacyTemptGoal ? 3
                        : goal instanceof LegacyFollowParentGoal ? 4
                        : goal instanceof LegacyFindFoodGoal || goal instanceof LegacyFindWaterGoal
                            || goal instanceof LegacyGrazeGoal || goal instanceof LegacyRodentGrazeGoal ? 5
                        : goal instanceof LegacyFollowOwnerGoal ? 6
                        : goal instanceof LegacySleepGoal ? 7
                        : goal instanceof LookAtPlayerGoal || goal instanceof LegacyIdleLookGoal ? 14
                        : goal instanceof WaterAvoidingRandomStrollGoal ? 15 : wrapped.getPriority();
                if (priority != wrapped.getPriority()) {
                    animal.goalSelector.removeGoal(goal); animal.goalSelector.addGoal(priority, goal);
                }
            }
            animal.goalSelector.addGoal(6, new SmallAnimalActivityGoal(animal, FarmActivityGoal.SHELTER));
            animal.goalSelector.addGoal(10, new SmallAnimalActivityGoal(animal, FarmActivityGoal.SMALL_EXPLORE));
            if (rabbit || hamster)
                animal.goalSelector.addGoal(9, new SmallAnimalActivityGoal(animal, FarmActivityGoal.SCRATCH));
        }

        boolean nocturnalHunter = LegacySleepGoal.nocturnalWildCatOrFox(animal);
        boolean wolf = animal instanceof AnimaniaDog canine && canine.breed().isWolf();
        if (nocturnalHunter || wolf) {
            for (var wrapped : java.util.List.copyOf(animal.goalSelector.getAvailableGoals())) {
                Goal goal = wrapped.getGoal();
                int priority = goal instanceof FloatGoal ? 0
                        : goal instanceof PanicGoal ? 1
                        : goal instanceof SitWhenOrderedToGoal ? 2
                        : goal instanceof LegacyTemptGoal ? 3
                        : goal instanceof LegacyFollowParentGoal ? 4
                        : goal instanceof LegacyFindFoodGoal || goal instanceof LegacyFindWaterGoal
                            || goal instanceof LegacyGrazeGoal ? 5
                        : goal instanceof LegacyFollowOwnerGoal ? 7
                        : goal instanceof LegacySleepGoal ? 9
                        : goal instanceof LookAtPlayerGoal || goal instanceof LegacyIdleLookGoal ? 14
                        : goal instanceof WaterAvoidingRandomStrollGoal ? 15 : wrapped.getPriority();
                if (priority != wrapped.getPriority()) {
                    animal.goalSelector.removeGoal(goal); animal.goalSelector.addGoal(priority, goal);
                }
            }
            if (nocturnalHunter) {
                animal.goalSelector.addGoal(8, new SmallAnimalActivityGoal(animal, FarmActivityGoal.SHELTER));
                if (cat) {
                    animal.goalSelector.addGoal(10, new PetActivityGoal((TamableAnimal) animal, FarmActivityGoal.GROOM));
                    animal.goalSelector.addGoal(11, new PetActivityGoal((TamableAnimal) animal, FarmActivityGoal.PET_EXPLORE));
                } else {
                    animal.goalSelector.addGoal(11, new PetActivityGoal((TamableAnimal) animal, FarmActivityGoal.SNIFF));
                }
            }
            if (wolf) {
                animal.goalSelector.addGoal(10, new WolfFamilyGoal((AnimaniaDog) animal));
                animal.goalSelector.addGoal(11, new PetActivityGoal((TamableAnimal) animal, FarmActivityGoal.SNIFF));
            }
        }

    }
}
