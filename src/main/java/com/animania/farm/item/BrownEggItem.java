package com.animania.farm.item;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The legacy brown egg only becomes a projectile when that option is enabled. */
public final class BrownEggItem extends EggItem {
    public BrownEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return LegacyConfig.ALLOW_EGG_THROWING.get()
                ? super.use(level, player, hand)
                : InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
