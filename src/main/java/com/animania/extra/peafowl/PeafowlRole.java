package com.animania.extra.peafowl;

public enum PeafowlRole {
    PEACHICK, PEAHEN, PEACOCK;

    public static PeafowlRole fromPath(String path) {
        if (path.startsWith("peachick_")) return PEACHICK;
        if (path.startsWith("peacock_")) return PEACOCK;
        return PEAHEN;
    }
}
