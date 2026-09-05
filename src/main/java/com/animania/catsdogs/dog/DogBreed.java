package com.animania.catsdogs.dog;

import net.minecraft.util.StringRepresentable;

public enum DogBreed implements StringRepresentable {
    BLOOD_HOUND("blood_hound", 1), CHIHUAHUA("chihuahua", 2), COLLIE("collie", 2),
    CORGI("corgi", 1), DACHSHUND("dachshund", 1), FOX("fox", 1),
    GERMAN_SHEPHERD("german_shepherd", 1), GREAT_DANE("great_dane", 1),
    GREYHOUND("greyhound", 1), HUSKY("husky", 1), LABRADOR("labrador", 3),
    POMERANIAN("pomeranian", 1), POODLE("poodle", 3), PUG("pug", 1), WOLF("wolf", 8);

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
        return this == FOX || this == WOLF;
    }

    public static DogBreed fromPath(String path) {
        for (DogBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return BLOOD_HOUND;
    }
}
