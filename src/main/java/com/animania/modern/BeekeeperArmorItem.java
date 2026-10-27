package com.animania.modern;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;

public final class BeekeeperArmorItem extends ArmorItem {
    private static final ArmorMaterial MATERIAL = new ArmorMaterial() {
        public int getDurabilityForType(Type type) { return 160; }
        public int getDefenseForType(Type type) { return type == Type.CHESTPLATE || type == Type.LEGGINGS ? 2 : 1; }
        public int getEnchantmentValue() { return 9; }
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.LEATHER); }
        public String getName() { return "animania:beekeeper"; }
        public float getToughness() { return 0; }
        public float getKnockbackResistance() { return 0; }
    };
    public BeekeeperArmorItem(Type type) { super(MATERIAL, type, new Item.Properties()); }
    @Override public String getArmorTexture(ItemStack stack, Entity wearer, EquipmentSlot slot, String type) {
        return "animania:textures/models/armor/beekeeper_layer_" + (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }
    @Override public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.add(net.minecraft.network.chat.Component.translatable("tooltip.animania.beekeeper").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
