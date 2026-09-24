package com.animania.common.entity;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.*;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.catsdogs.cat.*;
import com.animania.catsdogs.dog.*;
import com.animania.modern.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

/** Husbandry bonuses reflect sustained conditions, not the last item a player offered. */
public final class HusbandryMood {
    public static final int FOOD=1, WATER=2, SPACE=4, COMPANY=8, ROOTING=16, MUD=32, GRAZING=64,
            SHELTER=128, PERCH=256, BROWSE=512, CLIMB=1024, SCRATCH=2048, QUIET=4096,
            AQUATIC=8192, FLOWERS=16384, HIVE=32768, SOLITUDE=65536, DUST=131072, WHEEL=262144, BANK=524288;
    public enum Kind { COW,PIG,SHEEP,GOAT,MOUNTAIN_GOAT,HORSE,CHICKEN,PEAFOWL,RABBIT,CAT,OCELOT,DOG,WOLF,FOX,HAMSTER,HEDGEHOG,FERRET,BEE,AXOLOTL,FROG,TOAD,DART_FROG }
    private HusbandryMood() {}
    public static Kind kind(Animal a) {
        if (a instanceof AnimaniaCow) return Kind.COW;
        if (a instanceof AnimaniaPig) return Kind.PIG;
        if (a instanceof AnimaniaSheep) return Kind.SHEEP;
        if (a instanceof AnimaniaGoat) return Kind.GOAT;
        if (a instanceof MountainGoat) return Kind.MOUNTAIN_GOAT;
        if (a instanceof AnimaniaHorse) return Kind.HORSE;
        if (a instanceof com.animania.farm.chicken.AnimaniaChicken) return Kind.CHICKEN;
        if (a instanceof com.animania.extra.peafowl.AnimaniaPeafowl) return Kind.PEAFOWL;
        if (a instanceof com.animania.extra.rabbit.AnimaniaRabbit) return Kind.RABBIT;
        if (a instanceof AnimaniaCat cat) return cat.breed() == CatBreed.OCELOT ? Kind.OCELOT : Kind.CAT;
        if (a instanceof AnimaniaDog dog) return dog.breed().isWolf() ? Kind.WOLF : dog.breed() == DogBreed.FOX ? Kind.FOX : Kind.DOG;
        if (a instanceof ModernFox) return Kind.FOX;
        if (a instanceof AnimaniaRodent rodent) return rodent.kind() == AnimaniaRodent.Kind.HAMSTER ? Kind.HAMSTER : rodent.kind().isFerret() ? Kind.FERRET : Kind.HEDGEHOG;
        if (a instanceof ModernBee) return Kind.BEE;
        if (a instanceof ModernAxolotl) return Kind.AXOLOTL;
        if (a instanceof com.animania.extra.amphibian.AnimaniaAmphibian frog) return switch (frog.kind()) {
            case FROG -> Kind.FROG; case TOAD -> Kind.TOAD; case DART_FROG -> Kind.DART_FROG;
        };
        return null;
    }
    public static boolean enabled() { return AnimaniaConfig.ANIMAL_MOOD.get() && !LegacyConfig.AMBIANCE_MODE.get(); }
    public static boolean managed(Animal animal) { return enabled() && animal.getData(ModAttachments.CARE_LEASE) > 0; }
    public static void caredFor(Animal animal) {
        if (animal.level().isClientSide() || kind(animal) == null) return;
        if (animal.getData(ModAttachments.CARE_LEASE) <= 0) {
            animal.setData(ModAttachments.MOOD_SCORE, 50);
            animal.setData(ModAttachments.MOOD_GRACE, AnimaniaConfig.MOOD_GRACE_TICKS.get());
            animal.setData(ModAttachments.MOOD_CALENDAR, animal.level().getDayTime());
        }
        animal.setData(ModAttachments.MOOD_ENROLLED, 1);
        animal.setData(ModAttachments.CARE_LEASE, 48000);
    }
    public static int effect(Animal animal) {
        if (!managed(animal) || animal.getData(ModAttachments.MOOD_GRACE) > 0) return 0;
        int score = animal.getData(ModAttachments.MOOD_SCORE);
        return score >= 75 ? 1 : score < 35 && AnimaniaConfig.MOOD_PENALTIES.get() ? -1 : 0;
    }
    public static boolean breedingAllowed(Animal animal) {
        return effect(animal) >= 0 || animal.getData(ModAttachments.MOOD_SCORE) >= 20;
    }
    /** Progress of a normal one-tick production clock: +25% or -50%. */
    public static int work(Animal animal) {
        int effect = effect(animal);
        return effect > 0 ? animal.tickCount % 4 == 0 ? 2 : 1
                : effect < 0 ? animal.tickCount % 2 == 0 ? 0 : 1 : 1;
    }
    public static int milkCooldown(Animal animal) { return effect(animal) < 0 ? 1200 : 0; }
    public static boolean milkReady(Animal animal) { return effect(animal) >= 0 || animal.getData(ModAttachments.MILK_REST) == 0; }
    public static void milked(Animal animal) { if (managed(animal)) animal.setData(ModAttachments.MILK_REST, milkCooldown(animal)); }
    public static void afterMilking(Animal animal) {
        milked(animal);
        if (effect(animal) <= 0 || animal.getRandom().nextInt(4) != 0) LegacyAnimalNeeds.setWatered(animal, false);
    }
    public static void tick(Animal animal) {
        if (!enabled() || kind(animal) == null || animal.level().isClientSide()) return;
        int lease = animal.getData(ModAttachments.CARE_LEASE);
        if (lease > 0) animal.setData(ModAttachments.CARE_LEASE, lease - 1);
        int grace = animal.getData(ModAttachments.MOOD_GRACE);
        if (grace > 0) {
            long now = animal.level().getDayTime();
            long previous = animal.getData(ModAttachments.MOOD_CALENDAR);
            // Old saves begin from their remaining time. Rewinding the clock never adds time.
            long elapsed = previous < 0 || now <= previous ? 0 : now - previous;
            animal.setData(ModAttachments.MOOD_GRACE, (int) Math.max(0L, grace - elapsed));
            animal.setData(ModAttachments.MOOD_CALENDAR, now);
        }
        int milk = animal.getData(ModAttachments.MILK_REST);
        if (milk > 0) animal.setData(ModAttachments.MILK_REST, milk - 1);
        if (managed(animal) && !animal.isBaby() && animal.getAge() > 0) {
            int work = work(animal);
            animal.setAge(Math.max(0, animal.getAge() + 1 - work));
        }
        if (managed(animal) && animal instanceof ModernBee && animal.isBaby())
            animal.setAge(Math.min(0, animal.getAge() + work(animal) - 1));
        // UUID staggering also applies after saves are loaded or many eggs are used together.
        if (Math.floorMod(animal.tickCount + animal.getId(), 200) != 0) return;
        boolean owned = animal.isLeashed() || animal instanceof TamableAnimal pet && pet.isTame()
                || animal instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse && horse.isTamed()
                || animal instanceof ModernAnimal modern && modern.care().feeder() != null
                || animal instanceof ModernAxolotl axolotl && axolotl.fromBucket();
        // Upgrade existing saves only from explicit feeding/taming, not generic inspection.
        if (owned || animal.getData(ModAttachments.MOOD_ENROLLED) == 0 && animal.getData(ModAttachments.HAND_FED)) caredFor(animal);
        if (!managed(animal) && Math.floorMod(animal.tickCount + animal.getId(), 1200) != 0) return;
        HusbandryHabitat habitat = HusbandryHabitat.inspect(animal);
        if (habitat.enclosed()) caredFor(animal);
        if (!managed(animal)) return;
        if (habitat.incomplete) return;
        evaluate(animal, habitat);
        if (effect(animal) > 0 && animal.getData(ModAttachments.MOOD_MISSING) == 0 && animal.tickCount % 1200 < 200)
            animal.heal(1.0F);
    }
    private static void evaluate(Animal animal, HusbandryHabitat h) {
        Kind kind = kind(animal);
        int required = SPACE | QUIET;
        if (LegacyAnimalNeeds.profile(animal) != null) required |= FOOD | WATER;
        required |= switch (kind) {
            case COW,SHEEP -> COMPANY | GRAZING | SHELTER;
            case PIG -> COMPANY | ROOTING | MUD | SHELTER;
            case GOAT -> COMPANY | BROWSE | CLIMB | SHELTER;
            case MOUNTAIN_GOAT -> COMPANY | CLIMB | SHELTER;
            case HORSE -> COMPANY | GRAZING | SHELTER;
            case CHICKEN,PEAFOWL -> COMPANY | DUST | PERCH | SHELTER;
            case RABBIT -> COMPANY | ROOTING | SHELTER;
            case CAT -> SCRATCH | CLIMB | SHELTER;
            case OCELOT -> CLIMB | SHELTER;
            case DOG,WOLF -> COMPANY | SHELTER;
            case FOX -> ROOTING | SHELTER;
            case HAMSTER -> ROOTING | SHELTER | WHEEL | SOLITUDE;
            case HEDGEHOG -> ROOTING | SHELTER | SOLITUDE;
            case FERRET -> COMPANY | ROOTING | SHELTER;
            case BEE -> FLOWERS | HIVE | WATER;
            case AXOLOTL -> AQUATIC | SHELTER;
            case FROG -> AQUATIC | BANK | SHELTER;
            case TOAD -> ROOTING | BANK | SHELTER;
            case DART_FROG -> AQUATIC | BANK | BROWSE | SHELTER;
        };
        var nearby = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(8),
                other -> other != animal && other.isAlive() && (kind == Kind.BEE || h.cells.contains(other.blockPosition())) && animal.hasLineOfSight(other));
        int companions = 0, adults = 0;
        for (Animal other : nearby) if (kind(other) == kind) {
            companions++;
            if (animal.distanceToSqr(other) < 9 && !other.isBaby() && !other.getUUID().toString().equals(animal.getData(ModAttachments.PARENT))) adults++;
        }
        boolean ownerNear = animal instanceof TamableAnimal pet && pet.getOwner() != null && pet.distanceToSqr(pet.getOwner()) <= 144;
        int perAnimal = switch (kind) {
            case HORSE -> 20; case COW -> 16; case PIG,GOAT,MOUNTAIN_GOAT,SHEEP,PEAFOWL -> 10;
            case BEE -> 1; case CHICKEN,FROG,TOAD,DART_FROG,HAMSTER,HEDGEHOG -> 4; default -> 8;
        };
        int present = 0;
        if (LegacyAnimalNeeds.isFed(animal)) present |= FOOD;
        if (kind == Kind.BEE ? h.water || com.animania.common.config.AnimalNeedsExclusions.thirst(animal) : LegacyAnimalNeeds.isWatered(animal)) present |= WATER;
        if (h.area >= perAnimal * (nearby.size() + 1)) present |= SPACE;
        if (companions > 0 || kind == Kind.DOG && ownerNear) present |= COMPANY;
        if (h.rooting) present |= ROOTING;
        if (h.mud || animal instanceof AnimaniaPig pig && pig.hasPlayed()) present |= MUD;
        if (h.grass) present |= GRAZING;
        if (h.shelter) present |= SHELTER;
        if (h.perch) present |= PERCH;
        if (h.browse) present |= BROWSE;
        if (h.climb) present |= CLIMB;
        if (h.scratch) present |= SCRATCH;
        if (h.wheel) present |= WHEEL;
        if (h.dust) present |= DUST;
        if (h.land) present |= BANK;
        if (h.water && (kind != Kind.AXOLOTL || animal.isInWaterOrBubble())) present |= AQUATIC;
        if (h.flowers) present |= FLOWERS;
        if (h.hive) present |= HIVE;
        if (adults == 0 || animal.isBaby()) present |= SOLITUDE;
        if (!animal.isOnFire() && animal.getTarget() == null
                && (animal.getLastHurtByMob() == null || animal.tickCount - animal.getLastHurtByMobTimestamp() > 600)) present |= QUIET;
        int missing = required & ~present;
        animal.setData(ModAttachments.MOOD_MISSING, missing);
        int target = switch (Integer.bitCount(missing)) { case 0 -> 100; case 1 -> 65; case 2 -> 40; default -> 10; };
        if ((missing & (FOOD | WATER | SPACE | QUIET | AQUATIC)) != 0) target = Math.min(30, target);
        int score = animal.getData(ModAttachments.MOOD_SCORE);
        animal.setData(ModAttachments.MOOD_SCORE, score + Math.max(-2, Math.min(3, target-score)));
    }
    public static void inherit(Animal child, Animal parent) {
        if (managed(parent)) caredFor(child);
    }
    public static void copy(Animal from, Animal to) {
        to.setData(ModAttachments.CARE_LEASE, from.getData(ModAttachments.CARE_LEASE));
        to.setData(ModAttachments.MOOD_ENROLLED, from.getData(ModAttachments.MOOD_ENROLLED));
        to.setData(ModAttachments.MOOD_SCORE, from.getData(ModAttachments.MOOD_SCORE));
        to.setData(ModAttachments.MOOD_GRACE, from.getData(ModAttachments.MOOD_GRACE));
        to.setData(ModAttachments.MOOD_CALENDAR, from.getData(ModAttachments.MOOD_CALENDAR));
        to.setData(ModAttachments.MOOD_MISSING, from.getData(ModAttachments.MOOD_MISSING));
        to.setData(ModAttachments.MILK_REST, from.getData(ModAttachments.MILK_REST));
    }
}
