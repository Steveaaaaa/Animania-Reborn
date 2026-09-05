package com.animania.farm.dairy;

import net.minecraft.util.StringRepresentable;

public enum MilkType implements StringRepresentable {
    HOLSTEIN("holstein"),
    FRIESIAN("friesian"),
    JERSEY("jersey"),
    GOAT("goat"),
    SHEEP("sheep");

    private final String id;

    MilkType(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
