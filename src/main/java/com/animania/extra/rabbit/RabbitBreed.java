package com.animania.extra.rabbit;

import net.minecraft.util.StringRepresentable;

public enum RabbitBreed implements StringRepresentable {
    COTTONTAIL("cottontail", false), CHINCHILLA("chinchilla", true), DUTCH("dutch", false),
    HAVANA("havana", false), JACK("jack", false), NEW_ZEALAND("new_zealand", true),
    REX("rex", true), LOP("lop", false);

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
