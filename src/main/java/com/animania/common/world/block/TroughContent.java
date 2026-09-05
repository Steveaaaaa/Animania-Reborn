package com.animania.common.world.block;

import net.minecraft.util.StringRepresentable;

public enum TroughContent implements StringRepresentable {
    EMPTY("empty"),
    FEED("feed"),
    WATER("water");

    private final String serializedName;

    TroughContent(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}

