package com.animania.common.item;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public final class BonusFoodItem extends Item {
    private final List<MobEffectInstance> effects;

    public BonusFoodItem(Properties properties, MobEffectInstance... effects) {
        super(properties);
        this.effects = List.of(effects);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide() && LegacyConfig.FOODS_GIVE_BONUS_EFFECTS.get()) {
            effects.forEach(effect -> living.addEffect(new MobEffectInstance(effect)));
        }
        return result;
    }
}
