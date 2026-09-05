package com.animania.catsdogs.cat;

public enum CatRole {
    KITTEN("kitten"), QUEEN("queen"), TOM("tom");

    private final String prefix;

    CatRole(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    public static CatRole fromPath(String path) {
        if (path.startsWith("kitten_")) return KITTEN;
        if (path.startsWith("tom_")) return TOM;
        return QUEEN;
    }
}
