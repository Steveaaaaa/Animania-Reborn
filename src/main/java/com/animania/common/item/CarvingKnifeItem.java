package com.animania.common.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/** Durable recipe tool; one durability point is consumed whenever it remains in a crafting grid. */
public final class CarvingKnifeItem extends SwordItem {
    public CarvingKnifeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack result = stack.copyWithCount(1);
        result.setDamageValue(result.getDamageValue() + 1);
        return result.getDamageValue() >= result.getMaxDamage() ? ItemStack.EMPTY : result;
    }
}
