package com.animania.extra.rabbit;

public enum RabbitRole {
    KIT, DOE, BUCK;

    public static RabbitRole fromPath(String path) {
        if (path.startsWith("kit_")) return KIT;
        if (path.startsWith("buck_")) return BUCK;
        return DOE;
    }
}
