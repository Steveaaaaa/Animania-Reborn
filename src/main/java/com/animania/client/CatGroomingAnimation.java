package com.animania.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import java.util.Map;

/** Grooming uses the original shoulder/elbow hierarchy and keeps the other paw planted. */
final class CatGroomingAnimation {
    private CatGroomingAnimation() {}

    static void apply(String key, Map<String, ModelPart> parts, float time, float blend) {
        LegacyPose.load(key, "sitting").blend(parts, blend);
        ModelPart upper = parts.get("leg_l1");
        if (upper == null) return;
        // Ocelots use different names; leg_l2 is their opposite shoulder, not this elbow.
        ModelPart lower = upper.hasChild("leg_l2") ? upper.getChild("leg_l2")
                : upper.hasChild("leg_l21") ? upper.getChild("leg_l21") : null;
        if (lower == null) return;

        float lift = ease((time - 18) / 18) * (1 - ease((time - 94) / 16));
        float contact = ease((time - 35) / 10) * (1 - ease((time - 84) / 10));
        float lick = contact * Mth.sin((time - 45) * Mth.TWO_PI / 18);
        float weight = blend * lift;
        upper.xRot = Mth.lerp(weight, upper.xRot, -0.55F);
        upper.zRot = Mth.lerp(weight, upper.zRot, 0.10F);
        lower.xRot = Mth.lerp(weight, lower.xRot, -2.05F + 0.025F * lick);
        lower.yRot = Mth.lerp(weight, lower.yRot, 0);
        lower.zRot = Mth.lerp(weight, lower.zRot, 0);

        ModelPart neck = parts.get("neck1");
        ModelPart head = parts.get("head_base");
        if (neck != null) {
            neck.xRot = Mth.lerp(blend, neck.xRot, neck.getInitialPose().xRot + 0.30F * lift);
            neck.yRot = Mth.lerp(blend, neck.yRot, -0.12F * lift);
            neck.zRot = Mth.lerp(blend, neck.zRot, 0);
        }
        if (head != null) {
            head.xRot = Mth.lerp(blend, head.xRot, head.getInitialPose().xRot + 0.28F * lift + 0.045F * lick);
            head.yRot = Mth.lerp(blend, head.yRot, 0);
            head.zRot = Mth.lerp(blend, head.zRot, -0.08F * lift);
        }
        ModelPart jaw = parts.get("jaw");
        if (jaw != null) jaw.xRot += 0.045F * Math.max(0, lick) * blend;
    }

    private static float ease(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }
}
