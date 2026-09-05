package com.animania.farm.world.block;

import com.animania.farm.chicken.ChickenBreed;
import com.animania.extra.peafowl.PeafowlBreed;
import net.minecraft.util.StringRepresentable;

public enum NestBreed implements StringRepresentable {
    EMPTY("empty", null, null),
    LEGHORN("leghorn", ChickenBreed.LEGHORN, null),
    ORPINGTON("orpington", ChickenBreed.ORPINGTON, null),
    PLYMOUTH_ROCK("plymouth_rock", ChickenBreed.PLYMOUTH_ROCK, null),
    RHODE_ISLAND_RED("rhode_island_red", ChickenBreed.RHODE_ISLAND_RED, null),
    WYANDOTTE("wyandotte", ChickenBreed.WYANDOTTE, null),
    PEACOCK_CHARCOAL("peacock_charcoal", null, PeafowlBreed.CHARCOAL),
    PEACOCK_OPAL("peacock_opal", null, PeafowlBreed.OPAL),
    PEACOCK_PEACH("peacock_peach", null, PeafowlBreed.PEACH),
    PEACOCK_PURPLE("peacock_purple", null, PeafowlBreed.PURPLE),
    PEACOCK_TAUPE("peacock_taupe", null, PeafowlBreed.TAUPE),
    PEACOCK_BLUE("peacock_blue", null, PeafowlBreed.BLUE),
    PEACOCK_WHITE("peacock_white", null, PeafowlBreed.WHITE);

    private final String id;
    private final ChickenBreed chickenBreed;
    private final PeafowlBreed peafowlBreed;

    NestBreed(String id, ChickenBreed chickenBreed, PeafowlBreed peafowlBreed) {
        this.id = id;
        this.chickenBreed = chickenBreed;
        this.peafowlBreed = peafowlBreed;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public ChickenBreed chickenBreed() {
        return chickenBreed;
    }

    public static NestBreed of(ChickenBreed breed) {
        return valueOf(breed.name());
    }

    public PeafowlBreed peafowlBreed() {
        return peafowlBreed;
    }

    public static NestBreed of(PeafowlBreed breed) {
        return valueOf("PEACOCK_" + breed.name());
    }
}
