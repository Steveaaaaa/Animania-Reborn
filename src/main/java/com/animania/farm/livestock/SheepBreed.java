package com.animania.farm.livestock;

import net.minecraft.util.StringRepresentable;

public enum SheepBreed implements StringRepresentable {
    DORPER("dorper", true),
    DORSET("dorset", true),
    FRIESIAN("friesian", false),
    JACOB("jacob", false),
    MERINO("merino", false),
    SUFFOLK("suffolk", true),
    FLECKED("flecked", false),
    FUZZY("fuzzy", false),
    INKY("inky", false),
    LONG_NOSED("long_nosed", true),
    PATCHED("patched", false),
    ROCKY("rocky", false);

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

    public boolean isEarthBreed() {
        return switch (this) {
            case FLECKED, FUZZY, INKY, LONG_NOSED, PATCHED, ROCKY -> true;
            default -> false;
        };
    }

    public boolean hasPatternedWool() {
        return this == FLECKED || this == INKY || this == LONG_NOSED || this == ROCKY;
    }

    public boolean isPrime() {
        return prime;
    }

    public static SheepBreed fromPath(String path) {
        for (SheepBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return DORPER;
    }
}
