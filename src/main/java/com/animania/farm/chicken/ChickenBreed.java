package com.animania.farm.chicken;

import net.minecraft.util.StringRepresentable;

public enum ChickenBreed implements StringRepresentable {
    LEGHORN("leghorn", false, false, "white"),
    ORPINGTON("orpington", false, true, "golden"),
    PLYMOUTH_ROCK("plymouth_rock", false, true, "specked"),
    RHODE_ISLAND_RED("rhode_island_red", true, true, "red"),
    WYANDOTTE("wyandotte", true, true, "brown");

    private final String id;
    private final boolean brownEgg;
    private final boolean prime;
    private final String texture;

    ChickenBreed(String id, boolean brownEgg, boolean prime, String texture) {
        this.id = id;
        this.brownEgg = brownEgg;
        this.prime = prime;
        this.texture = texture;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public boolean laysBrownEggs() {
        return brownEgg;
    }

    public boolean isPrime() { return prime; }

    public String texture() {
        return texture;
    }

    public static ChickenBreed fromEntityPath(String path) {
        for (ChickenBreed breed : values()) {
            if (path.endsWith(breed.id)) {
                return breed;
            }
        }
        return LEGHORN;
    }
}
