package com.animania.common.item;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
public final class AnimaniaManualItem extends WrittenBookItem {
    public AnimaniaManualItem(Properties properties) { super(properties); }
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.hasTag() || !stack.getTag().contains("pages"))
            stack.setTag(getDefaultInstance().getTag().copy());
        if (level.isClientSide()) com.animania.client.ManualScreen.open(stack);
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

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
