package com.animania.catsdogs.block;

import net.minecraft.util.StringRepresentable;

public enum PetBowlContent implements StringRepresentable {
    EMPTY("empty"), FOOD("food"), WATER("water");

    private final String id;

    PetBowlContent(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
