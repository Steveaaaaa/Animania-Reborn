package com.animania.farm.livestock;

import net.minecraft.util.StringRepresentable;

public enum GoatBreed implements StringRepresentable {
    ALPINE("alpine", "alpine", true),
    ANGORA("angora", "angora", false),
    FAINTING("fainting", "fainting", false),
    KIKO("kiko", "kiko", true),
    KINDER("kinder", "kinder", false),
    NIGERIAN_DWARF("nigerian_dwarf", "nigerian", false),
    PYGMY("pygmy", "pygmy", true);

    private final String id;
    private final String texture;
    private final boolean prime;

    GoatBreed(String id, String texture, boolean prime) {
        this.id = id;
        this.texture = texture;
        this.prime = prime;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public String texture() {
        return texture;
    }

    public boolean isPrime() {
        return prime;
    }

    public static GoatBreed fromPath(String path) {
        for (GoatBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return ALPINE;
    }
}
