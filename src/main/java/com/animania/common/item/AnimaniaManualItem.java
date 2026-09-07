package com.animania.common.item;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
public final class AnimaniaManualItem extends WrittenBookItem {
    public AnimaniaManualItem(Properties properties) { super(properties); }
    @Override public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        var tag = stack.getOrCreateTag();
        tag.putString("title", "Animania Manual"); tag.putString("author", "Animania");
        tag.putInt("generation", 0); tag.putBoolean("resolved", true);
        ListTag pages = new ListTag();
        for (String page : new String[]{"welcome", "needs", "farm", "dairy", "hives", "extra", "pets", "vehicles"})
            pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("manual.animania.page." + page))));
        tag.put("pages", pages); return stack;
    }
}
