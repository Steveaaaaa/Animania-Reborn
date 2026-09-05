package com.animania.extra.rodent;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

/** A reusable clear or dyed exercise ball that can be fitted to a hamster. */
public final class HamsterBallItem extends Item {
    @Nullable
    private final DyeColor color;

    public HamsterBallItem(@Nullable DyeColor color, Properties properties) {
        super(properties.stacksTo(1));
        this.color = color;
    }

    /** -1 is the clear ball; vanilla dye ids identify colored balls. */
    public int ballColor() {
        return color == null ? -1 : color.getId();
    }

    @Nullable
    public DyeColor color() {
        return color;
    }
}
