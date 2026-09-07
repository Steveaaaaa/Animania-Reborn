package com.animania.common.config;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.Locale;

/** Maps the 1.12 BiomeDictionary categories used by Animania to modern biome tags. */
public final class LegacyBiomeMatcher {
    private LegacyBiomeMatcher() {}

    public static boolean matches(Holder<Biome> biome, List<? extends String> categories) {
        if (categories.isEmpty()) return false;
        String path = biome.unwrapKey().map(key -> key.location().getPath()).orElse("");
        for (String raw : categories) {
            String category = raw.trim().toUpperCase(Locale.ROOT);
            if (matches(biome, path, category)) return true;
        }
        return false;
    }

    private static boolean matches(Holder<Biome> biome, String path, String category) {
        return switch (category) {
            case "PLAINS" -> biome.is(Tags.Biomes.IS_PLAINS);
            case "FOREST" -> biome.is(net.minecraft.tags.BiomeTags.IS_FOREST);
            case "JUNGLE" -> biome.is(net.minecraft.tags.BiomeTags.IS_JUNGLE);
            case "SWAMP" -> biome.is(Tags.Biomes.IS_SWAMP);
            case "MOUNTAIN" -> biome.is(Tags.Biomes.IS_MOUNTAIN);
            case "HILLS" -> biome.is(net.minecraft.tags.BiomeTags.IS_HILL) || biome.is(Tags.Biomes.IS_MOUNTAIN);
            case "SAVANNA" -> biome.is(net.minecraft.tags.BiomeTags.IS_SAVANNA);
            case "MESA" -> biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS);
            case "WASTELAND" -> biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS) || biome.is(Tags.Biomes.IS_DESERT);
            case "MUSHROOM", "MAGICAL" -> biome.is(Tags.Biomes.IS_MUSHROOM) || path.contains("mushroom");
            case "SANDY" -> biome.is(Tags.Biomes.IS_SANDY);
            case "DENSE" -> biome.is(Tags.Biomes.IS_DENSE);
            case "CONIFEROUS" -> biome.is(Tags.Biomes.IS_CONIFEROUS) || path.contains("taiga");
            case "RIVER" -> biome.is(net.minecraft.tags.BiomeTags.IS_RIVER);
            case "BEACH" -> biome.is(net.minecraft.tags.BiomeTags.IS_BEACH);
            case "SNOWY" -> biome.is(Tags.Biomes.IS_SNOWY);
            case "COLD" -> biome.is(Tags.Biomes.IS_COLD);
            case "HOT" -> biome.is(Tags.Biomes.IS_HOT);
            default -> path.equals(category.toLowerCase(Locale.ROOT));
        };
    }
}
