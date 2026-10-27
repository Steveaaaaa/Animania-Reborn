package com.animania.modern;

import com.animania.common.config.AnimaniaConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class BeekeepingProtection {
    private BeekeepingProtection() {}
    public static float protection(LivingEntity wearer) {
        if (!AnimaniaConfig.BEE_PROTECTION.get()) return 0;
        float protection = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
            if (wearer.getItemBySlot(slot).getItem() instanceof BeekeeperArmorItem)
                protection += slot == EquipmentSlot.HEAD ? .4F : .2F;
        return Math.min(1, protection);
    }
    public static void wear(LivingEntity wearer) {
        if (!AnimaniaConfig.BEE_PROTECTION.get()) return;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
            if (wearer.getItemBySlot(slot).getItem() instanceof BeekeeperArmorItem)
                wearer.getItemBySlot(slot).hurtAndBreak(1, wearer, slot);
    }
}
