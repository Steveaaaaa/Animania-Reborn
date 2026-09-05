package com.animania.farm.livestock;

public enum FarmAnimalRole {
    YOUNG,
    FEMALE,
    MALE;

    public static FarmAnimalRole cowRole(String path) {
        if (path.startsWith("calf_")) return YOUNG;
        if (path.startsWith("bull_")) return MALE;
        return FEMALE;
    }

    public static FarmAnimalRole goatRole(String path) {
        if (path.startsWith("kid_")) return YOUNG;
        if (path.startsWith("buck_")) return MALE;
        return FEMALE;
    }

    public static FarmAnimalRole pigRole(String path) {
        if (path.startsWith("piglet_")) return YOUNG;
        if (path.startsWith("hog_")) return MALE;
        return FEMALE;
    }

    public static FarmAnimalRole sheepRole(String path) {
        if (path.startsWith("lamb_")) return YOUNG;
        if (path.startsWith("ram_")) return MALE;
        return FEMALE;
    }

    public static FarmAnimalRole horseRole(String path) {
        if (path.startsWith("foal_")) return YOUNG;
        if (path.startsWith("stallion_")) return MALE;
        return FEMALE;
    }
}
