package com.animania.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

/** A self-contained modern replacement for the removed 1.12 custom book GUI. */
public final class AnimaniaManualItem extends WrittenBookItem {
    public AnimaniaManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        List<Filterable<Component>> pages = List.of(
                page("manual.animania.page.welcome"), page("manual.animania.page.needs"),
                page("manual.animania.page.farm"), page("manual.animania.page.dairy"),
                page("manual.animania.page.hives"), page("manual.animania.page.extra"),
                page("manual.animania.page.pets"), page("manual.animania.page.vehicles"));
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("Animania Manual"), "Animania", 0, pages, true));
        return stack;
    }

    private static Filterable<Component> page(String key) {
        return Filterable.passThrough(Component.translatable(key));
    }
}
