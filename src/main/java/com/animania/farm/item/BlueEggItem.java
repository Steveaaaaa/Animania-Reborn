package com.animania.farm.item;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BlueEggItem extends Item {
    public BlueEggItem(Properties properties) { super(properties); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!LegacyConfig.ALLOW_EGG_THROWING.get()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide()) {
            var egg = ModEntities.BLUE_EGG.get().create(level);
            if (egg == null) return InteractionResultHolder.fail(stack);
            egg.setOwner(player);
            egg.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            egg.setItem(stack);
            egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1.0F);
            if (!level.addFreshEntity(egg)) return InteractionResultHolder.fail(stack);
        }
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW,
                SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
