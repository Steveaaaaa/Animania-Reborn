package com.animania.farm.livestock;

import net.minecraft.util.StringRepresentable;

public enum PigBreed implements StringRepresentable {
    DUROC("duroc", true),
    HAMPSHIRE("hampshire", true),
    LARGE_BLACK("large_black", true),
    LARGE_WHITE("large_white", false),
    OLD_SPOT("old_spot", true),
    YORKSHIRE("yorkshire", false);

    private final String id;
    private final boolean prime;

    PigBreed(String id, boolean prime) {
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

    public static PigBreed fromPath(String path) {
        for (PigBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return DUROC;
    }
}
