package com.animania.client;

import com.animania.common.entity.ai.FarmActivityGoal;
import com.animania.common.registry.ModAttachments;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import java.util.List;
import java.util.Map;

final class FarmActivityAnimation {
    private FarmActivityAnimation() {}
    static float elapsed(Entity entity, float partial) {
        return (int) entity.level().getGameTime() - ModAttachments.getData(entity, ModAttachments.FARM_ACTIVITY_START) + partial;
    }
    static float blend(Entity entity, float partial) {
        int activity = ModAttachments.getData(entity, ModAttachments.FARM_ACTIVITY);
        if (activity == 0) return 0;
        float time = elapsed(entity, partial);
        return Mth.clamp(Math.min(time / 20, (FarmActivityGoal.duration(activity) - time) / 20), 0, 1);
    }
    static void apply(Entity entity, String key, Map<String, ModelPart> parts, List<String> roots, float partial) {
        int activity = ModAttachments.getData(entity, ModAttachments.FARM_ACTIVITY);
        if (activity == 0 || ModAttachments.getData(entity, ModAttachments.SLEEPING)) return;
        float time = elapsed(entity, partial), blend = blend(entity, partial);
        if (activity == FarmActivityGoal.PREEN) {
            float side = (entity.getUUID().getLeastSignificantBits() & 1L) == 0 ? 1 : -1;
            ModelPart neck = first(parts, "Neck", "Head", "head");
            if (neck != null) {
                neck.yRot = Mth.lerp(blend, neck.yRot, side * 1.15F);
                neck.xRot += (0.22F + 0.07F * Mth.sin(time * 0.24F)) * blend;
                neck.zRot += side * 0.18F * blend;
            }
            ModelPart wing = side > 0 ? first(parts, "Wing1", "WingL") : first(parts, "Wing2", "WingR");
            if (wing != null) wing.zRot += side * (0.18F + 0.03F * Mth.sin(time * 0.24F)) * blend;
            if (key.endsWith("/modelpeacock")) PeacockSleepingFan.apply(parts, blend);
        } else if (activity == FarmActivityGoal.SCRATCH) {
            ModelPart left = first(parts, "LegL1", "hamsterLegFrontLeft");
            ModelPart right = first(parts, "LegR1", "hamsterLegFrontRight");
            if (left != null) left.xRot += 0.35F * Mth.sin(time * 0.4F) * blend;
            if (right != null) right.xRot -= 0.35F * Mth.sin(time * 0.4F) * blend;
            ModelPart neck = first(parts, "Neck1");
            if (neck != null) neck.xRot += 0.10F * blend;
        } else if (activity == FarmActivityGoal.SMALL_EXPLORE) {
            ModelPart nose = first(parts, "hamsterNose", "nose", "Snout");
            if (nose != null) nose.z += 0.05F * Mth.sin(time * 0.25F) * blend;
        } else if (activity == FarmActivityGoal.GROOM) {
            if (key.startsWith("catsdogs/client/models/cats/")) {
                CatGroomingAnimation.apply(key, parts, time, blend);
                return;
            }
            LegacyPose.load(key, "sitting").blend(parts, blend);
            ModelPart head = first(parts, "head_base", "head");
            ModelPart paw = first(parts, "leg_l1", "front_left");
            if (head != null) {
                head.xRot += (0.3F + 0.05F * Mth.sin(time * 0.25F)) * blend;
                head.zRot += 0.12F * blend;
            }
            if (paw != null) paw.xRot -= (0.65F + 0.08F * Mth.sin(time * 0.25F)) * blend;
        } else if (activity == FarmActivityGoal.PET_REST) {
            LegacyPose.load(key, "sleeping").blend(parts, blend);
        } else if (activity == FarmActivityGoal.SNIFF) {
            ModelPart neck = first(parts, "neck1", "neck", "neck_base", "head", "pug_head", "head_base");
            if (neck != null) neck.xRot += (0.3F + 0.025F * Mth.sin(time * 0.3F)) * blend;
        } else if (activity == FarmActivityGoal.GREET) {
            ModelPart tail = parts.get("tail");
            if (tail != null) tail.yRot += 0.2F * Mth.sin(time * 0.3F) * blend;
            ModelPart head = first(parts, "head_base", "head", "pug_head");
            if (head != null) head.zRot += 0.10F * Mth.sin(time * 0.06F) * blend;
        } else if (activity == FarmActivityGoal.RUMINATE) {
            GeneratedLegacySleep.apply(key, parts, -0.55F * blend);
            ModelPart head = first(parts, "Head", "head", "HeadNode");
            if (head != null) {
                head.yRot = 0.025F * Mth.sin(time * 0.12F) * blend;
                head.xRot = head.getInitialPose().xRot + 0.025F * Mth.sin(time * 0.23F) * blend;
            }
            ModelPart muzzle = first(parts, "Snout", "snout", "Muzzle", "Nose");
            if (muzzle != null) muzzle.x += 0.12F * Mth.sin(time * 0.24F) * blend;
        } else if (activity == FarmActivityGoal.FORAGE) {
            ModelPart neck = first(parts, "Neck", "Head", "head");
            if (neck != null) neck.xRot += (0.35F + 0.45F * Math.max(0, Mth.sin(time * 0.35F))) * blend;
            for (String name : new String[]{"leg1Pivot", "leg2Pivot"}) {
                ModelPart leg = parts.get(name);
                if (leg != null) leg.xRot += 0.25F * Mth.sin(time * 0.4F + (name.equals("leg1Pivot") ? 0 : Mth.PI)) * blend;
            }
        } else if (activity == FarmActivityGoal.DUST_BATH) {
            if (key.endsWith("/modelpeacock")) PeacockSleepingFan.apply(parts, blend);
            for (String name : roots) {
                ModelPart part = parts.get(name);
                if (part != null) part.y += 2.0F * blend;
            }
            ModelPart left = first(parts, "Wing1", "WingL");
            ModelPart right = first(parts, "Wing2", "WingR");
            if (left != null) left.zRot += (0.4F + 0.22F * Mth.sin(time * 0.45F)) * blend;
            if (right != null) right.zRot -= (0.4F + 0.22F * Mth.sin(time * 0.45F + 1)) * blend;
            ModelPart neck = first(parts, "Neck", "Head");
            if (neck != null) neck.zRot += 0.15F * Mth.sin(time * 0.2F) * blend;
        } else if (activity == FarmActivityGoal.STANDING_REST) {
            ModelPart head = first(parts, "HeadNode", "Head");
            if (head != null) {
                head.xRot = head.getInitialPose().xRot + (0.18F + 0.015F * Mth.sin(time * 0.08F)) * blend;
                head.yRot = 0;
            }
            ModelPart hindLeg = parts.get("BackLeftMuscle");
            if (hindLeg != null) hindLeg.xRot += 0.10F * blend;
        } else if (activity == FarmActivityGoal.ALERT) {
            ModelPart head = first(parts, "HeadNode", "Head");
            if (head != null) head.xRot -= 0.16F * blend;
            for (String name : new String[]{"EarL", "EarR"}) {
                ModelPart ear = parts.get(name);
                if (ear != null) ear.xRot -= 0.12F * blend;
            }
        } else if (activity == FarmActivityGoal.BROWSE) {
            ModelPart head = first(parts, "HeadNode", "Head");
            if (head != null) head.xRot -= (0.25F + 0.025F * Mth.sin(time * 0.3F)) * blend;
            ModelPart muzzle = first(parts, "Snout", "Nose", "Mouth");
            if (muzzle != null) muzzle.x += 0.08F * Mth.sin(time * 0.3F) * blend;
        } else if (activity == FarmActivityGoal.SPAR) {
            ModelPart head = first(parts, "HeadNode", "Head");
            if (head != null) head.xRot = head.getInitialPose().xRot
                    + (0.2F + 0.05F * Mth.sin(time * 0.12F)) * blend;
        } else if (activity == FarmActivityGoal.WALLOW) {
            for (var entry : parts.entrySet()) {
                if (entry.getKey().toLowerCase(java.util.Locale.ROOT).startsWith("leg"))
                    entry.getValue().xRot += 0.2F * Mth.sin(time * 0.25F) * blend;
            }
        }
    }
    private static ModelPart first(Map<String, ModelPart> parts, String... names) {
        for (String name : names) if (parts.containsKey(name)) return parts.get(name);
        return null;
    }
}
