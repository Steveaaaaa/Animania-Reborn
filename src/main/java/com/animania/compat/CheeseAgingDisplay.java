package com.animania.compat;

import com.animania.common.registry.ModItems;
import com.animania.farm.dairy.MilkType;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public record CheeseAgingDisplay(MilkType milk, ItemStack input, ItemStack output) {
    public static List<CheeseAgingDisplay> all() {
        return Arrays.stream(MilkType.values())
                .map(type -> new CheeseAgingDisplay(type,
                        new ItemStack(ModItems.milkBucket(type).get()),
                        new ItemStack(ModItems.cheeseWheel(type).get())))
                .toList();
    }
}
