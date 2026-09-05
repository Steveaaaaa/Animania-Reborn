package com.animania.catsdogs.cat;

import net.minecraft.util.StringRepresentable;

public enum CatBreed implements StringRepresentable {
    RAGDOLL("ragdoll"),
    AMERICAN_SHORTHAIR("american_shorthair"),
    ASIATIC("asiatic"),
    EXOTIC("exotic"),
    NORWEGIAN("norwegian"),
    OCELOT("ocelot"),
    SIAMESE("siamese"),
    TABBY("tabby");

    private final String id;

    CatBreed(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public static CatBreed fromPath(String path) {
        for (CatBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return RAGDOLL;
    }
}
