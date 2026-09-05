package com.animania.common.world.block;

import net.minecraft.util.StringRepresentable;

public enum TroughPart implements StringRepresentable {
    MAIN("main"),
    EXTENSION("extension");

    private final String serializedName;

    TroughPart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
