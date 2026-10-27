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
    MOOSHROOM("mooshroom", false),
    SIMMENTAL("simmental", true),
    FIGHTING("fighting", true),
    UMBRA("umbra", true),
    WOOLY("wooly", true),
    WARM("warm", true),
    PINTO("pinto", false),
    ALBINO("albino", false),
    NORWEGIAN_RED("norwegian_red", false),
    CREAM("cream", false),
    COOKIE("cookie", true);

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

    /** Adult appearance of the represented line, not a simulation of polled genetics. */
    public boolean hasAdultHorns() {
        return switch (this) {
            case ANGUS, COOKIE -> false;
            case FRIESIAN, HEREFORD, HOLSTEIN, LONGHORN, HIGHLAND, JERSEY, SIMMENTAL,
                    NORWEGIAN_RED, FIGHTING, MOOSHROOM, UMBRA, WOOLY, WARM, PINTO, ALBINO, CREAM -> true;
        };
    }

    public boolean hasWoolCoat() {
        return this == UMBRA || this == WOOLY;
    }

    public boolean isPrime() {
        return prime;
    }

    public static CowBreed fromPath(String path) {
        for (CowBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return ANGUS;
    }
}
