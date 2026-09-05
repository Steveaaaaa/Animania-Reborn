package com.animania.common.config;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;

/** Shared implementation of the old global breeding, pairing and litter options. */
public final class LegacyBreedingRules {
    private LegacyBreedingRules() {}

    public static boolean canMate(Animal first, Animal second) {
        if (!first.getData(ModAttachments.FED) || !first.getData(ModAttachments.WATERED)
                || !second.getData(ModAttachments.FED) || !second.getData(ModAttachments.WATERED)) return false;
        if (LegacyConfig.FEED_TO_BREED.get()
                && (!first.getData(ModAttachments.HAND_FED) || !second.getData(ModAttachments.HAND_FED))) return false;
        if (LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get()
                && (!first.getData(ModAttachments.INTERACTED) || !second.getData(ModAttachments.INTERACTED))) return false;
        if (first.level() instanceof ServerLevel level) {
            int radius = LegacyConfig.ANIMAL_CAP_SEARCH_RANGE.get();
            int count = level.getEntitiesOfClass(first.getClass(), first.getBoundingBox().inflate(radius)).size();
            if (count + 1 >= LegacyConfig.ENTITY_BREEDING_LIMIT.get()) return false;
        }
        String firstMate = first.getData(ModAttachments.LAST_MATE);
        String secondMate = second.getData(ModAttachments.LAST_MATE);
        if (!LegacyConfig.MALES_MATE_MULTIPLE_FEMALES.get()) {
            return (firstMate.isEmpty() || firstMate.equals(second.getUUID().toString()))
                    && (secondMate.isEmpty() || secondMate.equals(first.getUUID().toString()));
        }
        Animal female = AnimalInformation.gender(first) == AnimalInformation.Gender.FEMALE ? first : second;
        Animal male = female == first ? second : first;
        String femaleMate = female.getData(ModAttachments.LAST_MATE);
        return femaleMate.isEmpty() || femaleMate.equals(male.getUUID().toString());
    }

    public static int litterSize(RandomSource random) {
        int count = 1;
        double chance = LegacyConfig.BIRTH_MULTIPLE_CHANCE.get();
        while (count < 16 && random.nextDouble() <= Math.pow(chance, count)) count++;
        return count;
    }

    /** The 1.12 mating AI consumed the female's hand-fed state after conception. */
    public static void recordConception(Animal first, Animal second) {
        Animal female = AnimalInformation.gender(first) == AnimalInformation.Gender.FEMALE ? first : second;
        female.setData(ModAttachments.HAND_FED, false);
    }

    public static boolean shouldLosePregnancy(Animal mother, RandomSource random) {
        return mother.getData(ModAttachments.HUNGER) <= 20
                && mother.getData(ModAttachments.THIRST) <= 20
                && random.nextDouble() <= LegacyConfig.ANIMAL_LOSS_CHANCE.get();
    }
}
