package com.animania.common.entity;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;

/** Reproduces Animania 1.12's 0.01-per-step, care-gated child growth. */
public final class LegacyGrowth {
    private static final int ADULT_STEP = 85;

    private LegacyGrowth() {
    }

    public static void tick(Animal animal) {
        if (animal.level().isClientSide() || AnimalInformation.gender(animal) != AnimalInformation.Gender.YOUNG) {
            return;
        }

        int step = animal.getData(ModAttachments.CHILD_GROWTH);
        int timer = Math.min(20_000_000, animal.getData(ModAttachments.CHILD_GROWTH_TIMER) + 1);
        int stepTicks = LegacyConfig.CHILD_GROWTH_TICK.get();
        if (timer >= stepTicks && LegacyAnimalNeeds.isFed(animal)
                && LegacyAnimalNeeds.isWatered(animal)
                && !animal.getData(ModAttachments.SLEEPING)) {
            timer = 0;
            step = Math.min(ADULT_STEP, step + 1);
            animal.setData(ModAttachments.CHILD_GROWTH, step);
            if (step == ADULT_STEP) notifyMother(animal);
        }
        animal.setData(ModAttachments.CHILD_GROWTH_TIMER, timer);

        // The old renderer encoded the same progress through growingAge.  Continually
        // overriding vanilla's counter also prevents unattended children growing up.
        animal.setAge(step >= ADULT_STEP ? 0 : -(ADULT_STEP - step) * stepTicks);
    }

    private static void notifyMother(Animal child) {
        String parentId = child.getData(ModAttachments.PARENT);
        if (parentId.isEmpty()) return;
        child.level().getEntitiesOfClass(Animal.class, child.getBoundingBox().inflate(15.0D),
                possible -> possible.getClass() == child.getClass()
                        && possible.getUUID().toString().equals(parentId)).stream().findFirst().ifPresent(mother -> {
            if (mother instanceof com.animania.farm.livestock.AnimaniaCow cow) cow.childMatured();
            else if (mother instanceof com.animania.farm.livestock.AnimaniaGoat goat) goat.childMatured();
            else if (mother instanceof com.animania.farm.livestock.AnimaniaSheep sheep) sheep.childMatured();
        });
    }
}
