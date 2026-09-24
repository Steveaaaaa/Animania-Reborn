package com.animania.common.entity;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;

/** Transient server-owned poses; tracking clients receive the same behavior stage. */
public final class FamilyAnimationState {
    public static final int COURT = 1, NURSE = 2, SUCKLE = 3, NUZZLE = 4, BROOD = 5;
    private FamilyAnimationState() {}
    public static void set(Animal animal, int pose) {
        ModAttachments.setData(animal, ModAttachments.FAMILY_POSE_TTL, pose == 0 ? 0 : 4);
        if (ModAttachments.getData(animal, ModAttachments.FAMILY_POSE) != pose) ModAttachments.setData(animal, ModAttachments.FAMILY_POSE, pose);
    }
    public static void clear(Animal animal, int pose) {
        if (animal != null && ModAttachments.getData(animal, ModAttachments.FAMILY_POSE) == pose) set(animal, 0);
    }
}
