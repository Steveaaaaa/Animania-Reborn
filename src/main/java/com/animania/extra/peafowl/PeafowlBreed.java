package com.animania.extra.peafowl;

import net.minecraft.util.StringRepresentable;

public enum PeafowlBreed implements StringRepresentable {
    CHARCOAL("charcoal", false), OPAL("opal", false), PEACH("peach", true),
    PURPLE("purple", true), TAUPE("taupe", false), BLUE("blue", true), WHITE("white", true);

    private final String id;
    private final boolean prime;

    PeafowlBreed(String id, boolean prime) {
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

    public static PeafowlBreed fromPath(String path) {
        for (PeafowlBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return BLUE;
    }
}
