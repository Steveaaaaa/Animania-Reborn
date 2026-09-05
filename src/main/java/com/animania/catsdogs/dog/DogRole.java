package com.animania.catsdogs.dog;

public enum DogRole {
    PUPPY("puppy"), FEMALE("female"), MALE("male");

    private final String prefix;

    DogRole(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    public static DogRole fromPath(String path) {
        if (path.startsWith("puppy_")) return PUPPY;
        if (path.startsWith("male_")) return MALE;
        return FEMALE;
    }
}
