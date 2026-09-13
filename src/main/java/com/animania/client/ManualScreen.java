package com.animania.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.item.ItemStack;

public final class ManualScreen {
    private ManualScreen() {}
    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new BookViewScreen(BookViewScreen.BookAccess.fromItem(stack)));
    }
}
