package com.animania.common.config;

import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import java.util.*;
import java.util.regex.Pattern;

/** Per-type decisions are rebuilt when the server's configured lists change. */
public final class AnimalNeedsExclusions {
    private static final Filter HUNGER = new Filter();
    private static final Filter THIRST = new Filter();
    private AnimalNeedsExclusions() {}
    public static boolean hunger(Animal animal) { return HUNGER.matches(animal, AnimaniaConfig.HUNGER_BLACKLIST.get()); }
    public static boolean thirst(Animal animal) { return THIRST.matches(animal, AnimaniaConfig.THIRST_BLACKLIST.get()); }
    private static final class Filter {
        private List<? extends String> previous = List.of();
        private final Map<EntityType<?>, Boolean> results = new HashMap<>();
        synchronized boolean matches(Animal animal, List<? extends String> entries) {
            if (!previous.equals(entries)) { previous = List.copyOf(entries); results.clear(); }
            return results.computeIfAbsent(animal.getType(), type -> {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                for (String entry : entries) {
                    if (group(animal, entry)) return true;
                    if (entry.equals("*")) return true;
                    if (entry.indexOf(':') < 0) continue;
                    String expression = Arrays.stream(entry.split("\\*", -1))
                            .map(Pattern::quote).collect(java.util.stream.Collectors.joining(".*"));
                    if (id.matches(expression)) return true;
                }
                return false;
            });
        }
    }
    private static boolean group(Animal animal, String group) {
        return switch (group) {
            case "axolotls" -> animal instanceof com.animania.modern.ModernAxolotl;
            case "cats" -> animal instanceof AnimaniaCat;
            case "dogs" -> animal instanceof AnimaniaDog dog && !dog.breed().name().equals("FOX") && !dog.breed().isWolf();
            case "wolves" -> animal instanceof AnimaniaDog dog && dog.breed().isWolf();
            case "foxes" -> animal instanceof com.animania.modern.ModernFox || animal instanceof AnimaniaDog dog && dog.breed().name().equals("FOX");
            case "cows" -> animal instanceof com.animania.farm.livestock.AnimaniaCow;
            case "pigs" -> animal instanceof com.animania.farm.livestock.AnimaniaPig;
            case "sheep" -> animal instanceof com.animania.farm.livestock.AnimaniaSheep;
            case "goats" -> animal instanceof com.animania.farm.livestock.AnimaniaGoat || animal instanceof com.animania.modern.MountainGoat;
            case "horses" -> animal instanceof com.animania.farm.livestock.AnimaniaHorse;
            case "chickens" -> animal instanceof com.animania.farm.chicken.AnimaniaChicken;
            case "peafowl" -> animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl;
            case "rabbits" -> animal instanceof com.animania.extra.rabbit.AnimaniaRabbit;
            case "hamsters" -> animal instanceof AnimaniaRodent rodent && rodent.kind() == AnimaniaRodent.Kind.HAMSTER;
            case "ferrets" -> animal instanceof AnimaniaRodent rodent && rodent.kind().isFerret();
            case "hedgehogs" -> animal instanceof AnimaniaRodent rodent && rodent.kind().name().startsWith("HEDGEHOG");
            default -> false;
        };
    }
}
