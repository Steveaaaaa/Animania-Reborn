package com.animania.extra.rabbit;

import net.minecraft.util.StringRepresentable;

public enum RabbitBreed implements StringRepresentable {
    COTTONTAIL("cottontail", false), CHINCHILLA("chinchilla", true), DUTCH("dutch", false),
    HAVANA("havana", false), JACK("jack", false), NEW_ZEALAND("new_zealand", true),
    REX("rex", true), LOP("lop", false),
    DESERT("desert", false),
    BLACK_AND_WHITE("black_and_white", false),
    SALT_AND_PEPPER("salt_and_pepper", false),
    VESTED("vested", false),
    BOLD_STRIPED("bold_striped", false),
    FRECKLED("freckled", false),
    HARELEQUIN("harelequin", false),
    MUDDY_FOOT("muddy_foot", false);

    private final String id;
    private final boolean prime;

    RabbitBreed(String id, boolean prime) {
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

    public static RabbitBreed fromPath(String path) {
        for (RabbitBreed breed : values()) if (path.endsWith(breed.id)) return breed;
        return COTTONTAIL;
    }
}
