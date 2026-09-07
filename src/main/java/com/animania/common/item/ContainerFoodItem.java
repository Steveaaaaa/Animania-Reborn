package com.animania.common.item;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Food or drink that returns a bowl or bottle without losing containers from stacked items. */
public final class ContainerFoodItem extends Item {
    public enum Container { BOWL, BOTTLE }

    private final Container container;
    private final boolean clearsEffects;
    private final java.util.List<MobEffectInstance> bonusEffects;

    public ContainerFoodItem(Properties properties, Container container) {
        this(properties, container, false);
    }

    public ContainerFoodItem(Properties properties, Container container, boolean clearsEffects,
                             MobEffectInstance... bonusEffects) {
        super(properties);
        this.container = container;
        this.clearsEffects = clearsEffects;
        this.bonusEffects = java.util.List.of(bonusEffects);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide()) {
            if (clearsEffects) living.removeAllEffects();
            if (LegacyConfig.FOODS_GIVE_BONUS_EFFECTS.get()) {
                bonusEffects.forEach(effect -> living.addEffect(new MobEffectInstance(effect)));
            }
        }
        ItemStack empty = new ItemStack(container == Container.BOWL ? Items.BOWL : Items.GLASS_BOTTLE);
        if (result.isEmpty()) return empty;
        if (living instanceof Player player && !player.getAbilities().instabuild && !player.getInventory().add(empty)) {
            player.drop(empty, false);
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return container == Container.BOTTLE ? UseAnim.DRINK : UseAnim.EAT;
    }
}
