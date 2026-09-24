package com.animania.common.entity;

import com.animania.Animania;
import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.catsdogs.dog.DogRole;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.peafowl.PeafowlRole;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.rabbit.RabbitRole;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.chicken.ChickenRole;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.AnimaniaSheep;
import com.animania.farm.livestock.FarmAnimalRole;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.Animal;

/** Shared classification and relationship helpers for Animania husbandry systems. */
public final class AnimalInformation {
    public enum Gender {
        MALE,
        FEMALE,
        YOUNG,
        NONE
    }

    private AnimalInformation() {
    }

    public static boolean isAnimaniaAnimal(Animal animal) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).getNamespace().equals(Animania.MOD_ID);
    }

    public static Gender gender(Animal animal) {
        if (animal instanceof com.animania.modern.ModernAxolotl) return animal.isBaby() ? Gender.YOUNG : Gender.NONE;
        if (animal instanceof com.animania.modern.ModernAnimal modern)
            return animal.isBaby() ? Gender.YOUNG : modern.isFemale() ? Gender.FEMALE : Gender.MALE;
        if (animal instanceof com.animania.extra.rodent.AnimaniaRodent rodent)
            return rodent.isBaby() ? Gender.YOUNG : rodent.isFemale() ? Gender.FEMALE : Gender.MALE;
        if (animal instanceof AnimaniaCow cow) return farmGender(cow.role());
        if (animal instanceof AnimaniaGoat goat) return farmGender(goat.role());
        if (animal instanceof AnimaniaPig pig) return farmGender(pig.role());
        if (animal instanceof AnimaniaSheep sheep) return farmGender(sheep.role());
        if (animal instanceof AnimaniaHorse horse) return farmGender(horse.role());
        if (animal instanceof AnimaniaChicken chicken) {
            return switch (chicken.role()) {
                case ROOSTER -> Gender.MALE;
                case HEN -> Gender.FEMALE;
                case CHICK -> Gender.YOUNG;
            };
        }
        if (animal instanceof AnimaniaPeafowl peafowl) {
            return switch (peafowl.role()) {
                case PEACOCK -> Gender.MALE;
                case PEAHEN -> Gender.FEMALE;
                case PEACHICK -> Gender.YOUNG;
            };
        }
        if (animal instanceof AnimaniaRabbit rabbit) {
            return switch (rabbit.role()) {
                case BUCK -> Gender.MALE;
                case DOE -> Gender.FEMALE;
                case KIT -> Gender.YOUNG;
            };
        }
        if (animal instanceof AnimaniaCat cat) {
            return switch (cat.role()) {
                case TOM -> Gender.MALE;
                case QUEEN -> Gender.FEMALE;
                case KITTEN -> Gender.YOUNG;
            };
        }
        if (animal instanceof AnimaniaDog dog) {
            return switch (dog.role()) {
                case MALE -> Gender.MALE;
                case FEMALE -> Gender.FEMALE;
                case PUPPY -> Gender.YOUNG;
            };
        }
        return Gender.NONE;
    }

    private static Gender farmGender(FarmAnimalRole role) {
        return switch (role) {
            case MALE -> Gender.MALE;
            case FEMALE -> Gender.FEMALE;
            case YOUNG -> Gender.YOUNG;
        };
    }

    /** Birds were not sterilizable in the original mod. */
    public static boolean canBeSterilized(Animal animal) {
        return gender(animal) == Gender.MALE
                && !(animal instanceof AnimaniaChicken)
                && !(animal instanceof AnimaniaPeafowl);
    }

    public static boolean isSterilized(Animal animal) {
        return canBeSterilized(animal) && animal.getData(ModAttachments.STERILIZED);
    }

    /** Social pair bonds apply to wild canids, not to livestock or domestic pets. */
    public static boolean formsPairBond(Animal animal) {
        return animal instanceof com.animania.modern.ModernFox
                || animal instanceof AnimaniaDog dog && (dog.breed().isWolf()
                || dog.breed() == com.animania.catsdogs.dog.DogBreed.FOX);
    }

    public static void recordMating(Animal first, Animal second) {
        first.setData(ModAttachments.LAST_MATE, formsPairBond(first) ? second.getUUID().toString() : "");
        second.setData(ModAttachments.LAST_MATE, formsPairBond(second) ? first.getUUID().toString() : "");
        com.animania.common.config.LegacyBreedingRules.recordConception(first, second);
        Animal mother = gender(first) == Gender.FEMALE ? first : second;
        Animal father = mother == first ? second : first;
        mother.setData(ModAttachments.LAST_SIRE, father.getUUID().toString());
        if (FamilyLifecycle.bird(mother)) mother.setData(ModAttachments.FERTILIZED_TIMER, 12000);
    }

    public static void recordParent(Animal child, Animal parent) {
        HusbandryMood.inherit(child, parent);
        child.setData(ModAttachments.PARENT, parent.getUUID().toString());
        child.setData(ModAttachments.FATHER, parent.getData(ModAttachments.LAST_SIRE));
        if (FamilyLifecycle.mammal(child)) {
            child.setData(ModAttachments.NURSING_ID, child.getUUID().toString());
            java.util.Set<String> young = new java.util.LinkedHashSet<>(java.util.Arrays.asList(parent.getData(ModAttachments.NURSING_YOUNG).split(",")));
            young.remove(""); young.add(child.getUUID().toString());
            parent.setData(ModAttachments.NURSING_YOUNG, String.join(",", young));
        }
    }
}
