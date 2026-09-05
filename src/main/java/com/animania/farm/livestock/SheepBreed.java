package com.animania.farm.livestock;

import net.minecraft.util.StringRepresentable;

public enum SheepBreed implements StringRepresentable {
    DORPER("dorper", true),
    DORSET("dorset", true),
    FRIESIAN("friesian", false),
    JACOB("jacob", false),
    MERINO("merino", false),
    SUFFOLK("suffolk", true);

    private final String id;
    private final boolean prime;

    SheepBreed(String id, boolean prime) {
        this.id = id;
        this.prime = prime;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public boolean isPrime() {
        return prime;
    }

    public static SheepBreed fromPath(String path) {
        for (SheepBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return DORPER;
    }
}
