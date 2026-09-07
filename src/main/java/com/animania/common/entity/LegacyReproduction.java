package com.animania.common.entity;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;

/** Shared female fertility/dry-period clock from GenericBehavior.livingUpdateFemale. */
public final class LegacyReproduction {
    private LegacyReproduction() {
    }

    public static void tickFertility(Animal animal) {
        if (animal.level().isClientSide() || AnimalInformation.gender(animal) != AnimalInformation.Gender.FEMALE
                || animal instanceof com.animania.farm.chicken.AnimaniaChicken
                || animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl) return;
        int dryTimer = ModAttachments.getData(animal, ModAttachments.DRY_TIMER);
        if (!ModAttachments.getData(animal, ModAttachments.FERTILE) && dryTimer > -1) {
            ModAttachments.setData(animal, ModAttachments.DRY_TIMER, dryTimer - 1);
        } else {
            ModAttachments.setData(animal, ModAttachments.FERTILE, true);
            ModAttachments.setData(animal, ModAttachments.DRY_TIMER,
                    LegacyConfig.GESTATION_TIMER.get() / 9 + animal.getRandom().nextInt(50));
        }
    }

    /** The old port released a persistent mate when that mate was no longer alive within 30 blocks. */
    public static void tickMateReset(Animal animal) {
        if (animal.level().isClientSide() || animal.getRandom().nextInt(200) != 0
                || AnimalInformation.gender(animal) == AnimalInformation.Gender.YOUNG) return;
        String mateId = ModAttachments.getData(animal, ModAttachments.LAST_MATE);
        if (mateId.isEmpty()) return;
        boolean present = animal.level().getEntitiesOfClass(Animal.class,
                        animal.getBoundingBox().inflate(30.0D), other -> other != animal
                                && other.getClass() == animal.getClass() && other.isAlive()
                                && other.getUUID().toString().equals(mateId))
                .stream().findAny().isPresent();
        if (!present) ModAttachments.setData(animal, ModAttachments.LAST_MATE, "");
    }

    public static void conceived(Animal female) {
        ModAttachments.setData(female, ModAttachments.FERTILE, false);
    }

    public static void completedPregnancy(Animal female) {
        ModAttachments.setData(female, ModAttachments.FERTILE, false);
    }

    public static void wakeForBirth(Animal female, int gestation) {
        if (gestation < 200 && ModAttachments.getData(female, ModAttachments.SLEEPING)) {
            ModAttachments.setData(female, ModAttachments.SLEEPING, false);
        }
    }
}
