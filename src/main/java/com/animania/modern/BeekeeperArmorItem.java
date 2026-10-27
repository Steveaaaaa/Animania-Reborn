package com.animania.modern;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Map;
import java.util.List;

public final class BeekeeperArmorItem extends ArmorItem {
    private static final Holder<ArmorMaterial> MATERIAL = Holder.direct(new ArmorMaterial(
            Map.of(Type.HELMET, 1, Type.CHESTPLATE, 2, Type.LEGGINGS, 2, Type.BOOTS, 1, Type.BODY, 1),
            9, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("animania", "beekeeper"))), 0, 0));
    public BeekeeperArmorItem(Type type) { super(MATERIAL, type, new Item.Properties().durability(160)); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.add(net.minecraft.network.chat.Component.translatable("tooltip.animania.beekeeper").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
