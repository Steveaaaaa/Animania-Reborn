package com.animania.common.config;

import com.animania.common.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

/** Resolves old registry-name food lists, including the two OreDictionary aliases used by Animania. */
public final class LegacyItemMatcher {
    private static final Map<String, String> RENAMED_ANIMANIA_ITEMS = Map.of(
            "animania:prime_mutton", "animania:raw_prime_mutton",
            "animania:prime_rabbit", "animania:raw_prime_rabbit",
            "animania:prime_chicken", "animania:raw_prime_chicken",
            "animania:prime_beef", "animania:raw_prime_beef",
            "animania:prime_steak", "animania:raw_prime_steak"
    );
    private static final Map<String, net.minecraft.tags.TagKey<net.minecraft.world.item.Item>> COMPAT_FOODS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private LegacyItemMatcher() {}

    public static boolean matches(ItemStack stack, String listKey) {
        // Keep straw usable even when an existing server config predates its
        // addition to the default trough food list.
        if ("trough".equals(listKey) && stack.is(ModItems.STRAW.get())) return true;
        if (("dog".equals(listKey) || "petBowl".equals(listKey)) && isFarmersDogFood(stack)) return true;
        var extraFoods = COMPAT_FOODS.computeIfAbsent(listKey, key -> net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.tryParse("animania:compat/farmersdelight/feed/" + key.toLowerCase(java.util.Locale.ROOT))));
        if (stack.is(extraFoods)) return true;
        var configured = LegacyConfig.FOOD_LISTS.get(listKey);
        return configured != null && matches(stack, configured.get());
    }

    public static boolean isFarmersDogFood(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
                .equals("farmersdelight:dog_food");
    }

    public static boolean matches(ItemStack stack, List<? extends String> entries) {
        if (stack.isEmpty()) return false;
        for (String configured : entries) {
            if (matchesEntry(stack, configured.trim())) return true;
        }
        return false;
    }

    private static boolean matchesEntry(ItemStack stack, String entry) {
        if (entry.equalsIgnoreCase("minecraft:fish")) {
            return stack.is(Items.COD) || stack.is(Items.SALMON)
                    || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH);
        }
        if (entry.equalsIgnoreCase("listAllbeefraw")) {
            return stack.is(Items.BEEF) || stack.is(ModItems.RAW_PRIME_BEEF.get())
                    || stack.is(ModItems.RAW_PRIME_STEAK.get());
        }
        int metadata = entry.lastIndexOf('#');
        if (metadata > entry.indexOf(':')) entry = entry.substring(0, metadata);
        entry = RENAMED_ANIMANIA_ITEMS.getOrDefault(entry.toLowerCase(java.util.Locale.ROOT), entry);
        ResourceLocation id = ResourceLocation.tryParse(entry.replace("animania_", "animania:"));
        return id != null && BuiltInRegistries.ITEM.getOptional(id).filter(stack::is).isPresent();
    }
}
