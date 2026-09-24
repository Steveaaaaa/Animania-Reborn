package com.animania.catsdogs.dog;

import net.minecraft.util.StringRepresentable;

public enum DogBreed implements StringRepresentable {
    BLOOD_HOUND("blood_hound", 1), CHIHUAHUA("chihuahua", 2), COLLIE("collie", 2),
    CORGI("corgi", 1), DACHSHUND("dachshund", 1), FOX("fox", 1),
    GERMAN_SHEPHERD("german_shepherd", 1), GREAT_DANE("great_dane", 1),
    GREYHOUND("greyhound", 1), HUSKY("husky", 1), LABRADOR("labrador", 3),
    POMERANIAN("pomeranian", 1), POODLE("poodle", 3), PUG("pug", 1), WOLF("wolf", 8),
    WOLF_ASHEN("wolf_ashen", 1),
    WOLF_BLACK("wolf_black", 1),
    WOLF_CHESTNUT("wolf_chestnut", 1),
    WOLF_RUSTY("wolf_rusty", 1),
    WOLF_SPOTTED("wolf_spotted", 1),
    WOLF_STRIPED("wolf_striped", 1),
    WOLF_SNOWY("wolf_snowy", 1),
    WOLF_WOODS("wolf_woods", 1);

    private final String id;
    private final int variants;

    DogBreed(String id, int variants) {
        this.id = id;
        this.variants = variants;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public int variants() {
        return variants;
    }

    public boolean naturallySpawns() {
        return this == FOX || isWolf();
    }

    public boolean isWolf() {
        return this == WOLF || isNewWolf();
    }

    public boolean isNewWolf() {
        return id.startsWith("wolf_");
    }

    public boolean matchesWolfHabitat(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome) {
        return isNewWolf() && biome.is(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BIOME,
                new net.minecraft.resources.ResourceLocation("animania", id + "_habitats")));
    }

    public static boolean hasNewWolfHabitat(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome) {
        for (DogBreed breed : values()) if (breed.matchesWolfHabitat(biome)) return true;
        return false;
    }

    public static DogBreed fromPath(String path) {
        for (DogBreed breed : values()) if (path.equals(breed.id) || path.endsWith("_" + breed.id)) return breed;
        return BLOOD_HOUND;
    }
}
