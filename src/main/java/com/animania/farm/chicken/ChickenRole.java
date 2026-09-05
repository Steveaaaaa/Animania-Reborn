package com.animania.farm.chicken;

public enum ChickenRole {
    CHICK,
    HEN,
    ROOSTER;

    public static ChickenRole fromEntityPath(String path) {
        if (path.startsWith("rooster_")) {
            return ROOSTER;
        }
        if (path.startsWith("chick_")) {
            return CHICK;
        }
        return HEN;
    }
}
