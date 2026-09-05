package com.animania.farm.livestock;

import net.minecraft.util.StringRepresentable;

public enum CowBreed implements StringRepresentable {
    ANGUS("angus", true),
    FRIESIAN("friesian", false),
    HEREFORD("hereford", true),
    HOLSTEIN("holstein", false),
    LONGHORN("longhorn", true),
    HIGHLAND("highland", true),
    JERSEY("jersey", true),
    MOOSHROOM("mooshroom", false);

    private final String id;
    private final boolean prime;

    CowBreed(String id, boolean prime) {
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

    public static CowBreed fromPath(String path) {
        for (CowBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return ANGUS;
    }
}
