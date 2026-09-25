package com.animania.farm.dairy;

import net.minecraft.util.StringRepresentable;

public enum MilkType implements StringRepresentable {
    HOLSTEIN("holstein"),
    FRIESIAN("friesian"),
    JERSEY("jersey"),
    GOAT("goat"),
    SHEEP("sheep"),
    PINTO("pinto"),
    NORWEGIAN_RED("norwegian_red"),
    CREAM("cream");

    private final String id;

    MilkType(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
