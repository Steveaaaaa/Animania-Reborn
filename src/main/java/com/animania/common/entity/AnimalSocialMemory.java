package com.animania.common.entity;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.modern.MountainGoat;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import java.util.*;

/** Familiarity is not a mating bond. Keep at most four recently encountered companions. */
public final class AnimalSocialMemory {
    private record Contact(UUID id, int familiarity, long seen) {}
    private AnimalSocialMemory() {}

    public static boolean supported(Animal animal) {
        var kind = HusbandryMood.kind(animal);
        return kind != null && switch (kind) {
            case COW, PIG, SHEEP, GOAT, HORSE, CHICKEN, PEAFOWL, RABBIT, CAT, DOG, FERRET -> true;
            case MOUNTAIN_GOAT -> ((MountainGoat) animal).isFemale();
            default -> false;
        };
    }

    public static boolean compatible(Animal animal, Animal other) {
        if (animal == other || !supported(animal) || !supported(other)
                || HusbandryMood.kind(animal) != HusbandryMood.kind(other)) return false;
        // Owned pets do not wander off to join another player's household.
        if (animal instanceof TamableAnimal a && other instanceof TamableAnimal b
                && !Objects.equals(a.getOwnerUUID(), b.getOwnerUUID())) return false;
        var kind = HusbandryMood.kind(animal);
        // Do not override the existing rooster/peacock rivalry with a friendship goal.
        if (kind == HusbandryMood.Kind.CHICKEN || kind == HusbandryMood.Kind.PEAFOWL)
            return AnimalInformation.gender(animal) == AnimalInformation.Gender.FEMALE && AnimalInformation.gender(other) == AnimalInformation.Gender.FEMALE;
        return true;
    }

    private static List<Contact> read(Animal animal) {
        List<Contact> contacts = new ArrayList<>();
        long now = animal.level().getGameTime();
        for (String entry : animal.getData(ModAttachments.SOCIAL_CONTACTS).split(";")) {
            if (contacts.size() == 4) break;
            String[] fields = entry.split(",");
            if (fields.length != 3) continue;
            try {
                Contact c = new Contact(UUID.fromString(fields[0]), Math.min(120, Math.max(0, Integer.parseInt(fields[1]))), Long.parseLong(fields[2]));
                if (now >= c.seen && now - c.seen <= 168000) contacts.add(c);
            } catch (IllegalArgumentException ignored) { }
        }
        return contacts;
    }

    public static int familiarity(Animal animal, Animal other) {
        return read(animal).stream().filter(c -> c.id.equals(other.getUUID())).mapToInt(Contact::familiarity).findFirst().orElse(0);
    }

    public static boolean familiar(Animal animal, Animal other) {
        int threshold = HusbandryMood.kind(animal) == HusbandryMood.Kind.CAT ? 48 : 24;
        return compatible(animal, other) && familiarity(animal, other) >= threshold
                && familiarity(other, animal) >= threshold;
    }

    public static void tick(Animal animal) {
        if (!AnimaniaConfig.ANIMAL_SOCIAL_BEHAVIOR.get() || !supported(animal) || animal.isBaby()
                || Math.floorMod(animal.tickCount + animal.getId(), 200) != 0
                || animal.hurtTime > 0 || animal.getTarget() != null || animal.isOnFire()) return;
        List<Contact> contacts = read(animal);
        var neighbors = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(6),
                other -> other.isAlive() && !other.isBaby() && compatible(animal, other)
                        && other.hurtTime == 0 && other.getTarget() == null && animal.hasLineOfSight(other));
        neighbors.sort(Comparator.comparingDouble(animal::distanceToSqr));
        long now = animal.level().getGameTime();
        for (Animal other : neighbors.subList(0, Math.min(4, neighbors.size()))) {
            Contact old = contacts.stream().filter(c -> c.id.equals(other.getUUID())).findFirst().orElse(null);
            if (old != null) contacts.remove(old);
            else if (contacts.size() == 4) {
                // A newcomer cannot displace companions that are still nearby.
                Contact absent = contacts.stream().filter(c -> now - c.seen > 12000)
                        .min(Comparator.comparingLong(Contact::seen)).orElse(null);
                if (absent == null) continue;
                contacts.remove(absent);
            }
            contacts.add(new Contact(other.getUUID(), old == null ? 1 : Math.min(120, old.familiarity + 1), now));
        }
        animal.setData(ModAttachments.SOCIAL_CONTACTS, contacts.stream()
                .map(c -> c.id + "," + c.familiarity + "," + c.seen).collect(java.util.stream.Collectors.joining(";")));
    }
}
