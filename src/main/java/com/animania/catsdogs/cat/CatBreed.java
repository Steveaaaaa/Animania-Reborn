package com.animania.catsdogs.cat;

import net.minecraft.util.StringRepresentable;

public enum CatBreed implements StringRepresentable {
    RAGDOLL("ragdoll"),
    AMERICAN_SHORTHAIR("american_shorthair"),
    ASIATIC("asiatic"),
    EXOTIC("exotic"),
    NORWEGIAN("norwegian"),
    OCELOT("ocelot"),
    SIAMESE("siamese"),
    TABBY("tabby"),
    ALL_BLACK("all_black"),
    TUXEDO("tuxedo"),
    RED_TABBY("red_tabby"),
    BRITISH_SHORTHAIR("british_shorthair"),
    CALICO("calico"),
    PERSIAN("persian"),
    WHITE("white"),
    JELLIE("jellie");

    private final String id;

    CatBreed(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public boolean isVanillaAddition() {
        return switch (this) {
            case ALL_BLACK, TUXEDO, RED_TABBY, BRITISH_SHORTHAIR, CALICO, PERSIAN, WHITE, JELLIE -> true;
            default -> false;
        };
    }

    public String modelName() {
        return switch (this) {
            case NORWEGIAN -> "modelcatragdoll";
            case ALL_BLACK, TUXEDO, RED_TABBY, CALICO, WHITE, JELLIE -> "modelcattabby";
            case BRITISH_SHORTHAIR -> "modelcatamericanshorthair";
            case PERSIAN -> "modelcatexotic";
            default -> "modelcat" + id.replace("_", "");
        };
    }

    public static CatBreed fromPath(String path) {
        CatBreed result = null;
        for (CatBreed breed : values()) {
            if ((path.equals(breed.id) || path.endsWith("_" + breed.id))
                    && (result == null || breed.id.length() > result.id.length())) result = breed;
        }
        return result == null ? RAGDOLL : result;
    }
}
