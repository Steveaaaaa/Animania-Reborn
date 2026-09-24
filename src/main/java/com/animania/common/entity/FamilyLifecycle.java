package com.animania.common.entity;

import com.animania.common.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

/** Species policies are expressed in growth stages and game ticks, not biological days. */
public final class FamilyLifecycle {
    private FamilyLifecycle() {}
    public static boolean bird(Animal a) {
        return a instanceof com.animania.farm.chicken.AnimaniaChicken || a instanceof com.animania.extra.peafowl.AnimaniaPeafowl;
    }
    public static boolean mammal(Animal a) {
        return a instanceof com.animania.farm.livestock.AnimaniaCow || a instanceof com.animania.farm.livestock.AnimaniaPig
                || a instanceof com.animania.farm.livestock.AnimaniaGoat || a instanceof com.animania.farm.livestock.AnimaniaSheep
                || a instanceof com.animania.farm.livestock.AnimaniaHorse || a instanceof com.animania.extra.rabbit.AnimaniaRabbit
                || a instanceof com.animania.extra.rodent.AnimaniaRodent || a instanceof com.animania.catsdogs.cat.AnimaniaCat
                || a instanceof com.animania.catsdogs.dog.AnimaniaDog || a instanceof com.animania.modern.ModernAnimal;
    }
    public static boolean nestYoung(Animal a) {
        return a instanceof com.animania.extra.rabbit.AnimaniaRabbit || a instanceof com.animania.extra.rodent.AnimaniaRodent
                || a instanceof com.animania.catsdogs.cat.AnimaniaCat || a instanceof com.animania.catsdogs.dog.AnimaniaDog
                || a instanceof com.animania.modern.ModernFox;
    }
    public static int weaningStage(Animal a) {
        if (a instanceof com.animania.farm.livestock.AnimaniaHorse) return 65;
        if (a instanceof com.animania.farm.livestock.AnimaniaCow || a instanceof com.animania.farm.livestock.AnimaniaSheep
                || a instanceof com.animania.farm.livestock.AnimaniaGoat || a instanceof com.animania.modern.MountainGoat) return 55;
        return 40;
    }
    public static boolean milkDependent(Animal a) {
        return mammal(a) && a.isBaby() && a.getData(ModAttachments.CHILD_GROWTH) < weaningStage(a);
    }
    public static Animal mother(Animal child) {
        if (!(child.level() instanceof ServerLevel level)) return null;
        try {
            var entity = level.getEntity(java.util.UUID.fromString(child.getData(ModAttachments.PARENT)));
            return entity instanceof Animal parent && parent.isAlive() && !parent.isBaby()
                    && parent.getClass() == child.getClass() ? parent : null;
        } catch (IllegalArgumentException ex) { return null; }
    }
    public static boolean staysNearBirthplace(Animal child) {
        return child.isBaby() && nestYoung(child) && child.getData(ModAttachments.CHILD_GROWTH) < 15
                && mother(child) != null;
    }
    public static void tick(Animal animal) {
        int poseTime = animal.getData(ModAttachments.FAMILY_POSE_TTL);
        if (poseTime > 0) {
            animal.setData(ModAttachments.FAMILY_POSE_TTL, poseTime - 1);
            if (poseTime == 1) FamilyAnimationState.set(animal, 0);
        }
        if (mammal(animal) && animal.tickCount % 200 == 0 && !animal.getData(ModAttachments.NURSING_ID).isEmpty()
                && (!animal.isBaby() || animal.getData(ModAttachments.CHILD_GROWTH) >= weaningStage(animal))) weaned(animal);
        if (animal.tickCount % 200 == 0) FamilyUpdates.receive(animal);
        int recovery = animal.getData(ModAttachments.RECOVERY);
        if (recovery > 0) animal.setData(ModAttachments.RECOVERY, Math.max(0, recovery - HusbandryMood.work(animal)));
        int cooldown = animal.getData(ModAttachments.CARE_COOLDOWN);
        if (cooldown > 0) animal.setData(ModAttachments.CARE_COOLDOWN, cooldown - 1);
        int fertile = animal.getData(ModAttachments.FERTILIZED_TIMER);
        if (fertile > 0) animal.setData(ModAttachments.FERTILIZED_TIMER, fertile - 1);
    }
    public static int recoveryTicks(Animal a) {
        if (!mammal(a)) return 0;
        if (a instanceof com.animania.farm.livestock.AnimaniaHorse || a instanceof com.animania.farm.livestock.AnimaniaCow) return 6000;
        if (nestYoung(a) || a instanceof com.animania.farm.livestock.AnimaniaPig) return 4000;
        return 3000;
    }
    public static int careInterval(Animal a) { return a instanceof com.animania.extra.rabbit.AnimaniaRabbit ? 1200 : 300; }
    public static int courtshipTicks(Animal a) {
        if (bird(a)) return 80;
        if (a instanceof com.animania.farm.livestock.AnimaniaHorse) return 100;
        return nestYoung(a) ? 60 : 40;
    }
    public static void weaned(Animal child) { FamilyUpdates.weaned(child); }
    public static void endLactation(Animal mother) {
        if (mother instanceof com.animania.modern.MountainGoat goat) goat.care().milked();
        else if (mother instanceof com.animania.farm.livestock.AnimaniaCow cow) cow.childMatured();
        else if (mother instanceof com.animania.farm.livestock.AnimaniaGoat goat) goat.childMatured();
        else if (mother instanceof com.animania.farm.livestock.AnimaniaSheep sheep) sheep.childMatured();
    }
}
