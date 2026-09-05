package com.animania.farm.dairy;

import net.minecraft.util.StringRepresentable;

public enum DairyStage implements StringRepresentable {
    EMPTY("empty", null, false),
    HOLSTEIN_MILK("holstein_milk", MilkType.HOLSTEIN, false),
    HOLSTEIN_CHEESE("holstein_cheese", MilkType.HOLSTEIN, true),
    FRIESIAN_MILK("friesian_milk", MilkType.FRIESIAN, false),
    FRIESIAN_CHEESE("friesian_cheese", MilkType.FRIESIAN, true),
    JERSEY_MILK("jersey_milk", MilkType.JERSEY, false),
    JERSEY_CHEESE("jersey_cheese", MilkType.JERSEY, true),
    GOAT_MILK("goat_milk", MilkType.GOAT, false),
    GOAT_CHEESE("goat_cheese", MilkType.GOAT, true),
    SHEEP_MILK("sheep_milk", MilkType.SHEEP, false),
    SHEEP_CHEESE("sheep_cheese", MilkType.SHEEP, true),
    WATER("water", null, false),
    SALT("salt", null, true);

    private final String id;
    private final MilkType milk;
    private final boolean cheese;

    DairyStage(String id, MilkType milk, boolean cheese) {
        this.id = id;
        this.milk = milk;
        this.cheese = cheese;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public MilkType milk() {
        return milk;
    }

    public boolean isCheese() {
        return cheese;
    }

    public static DairyStage milk(MilkType type) {
        return valueOf(type.name() + "_MILK");
    }

    public static DairyStage cheese(MilkType type) {
        return valueOf(type.name() + "_CHEESE");
    }
}
