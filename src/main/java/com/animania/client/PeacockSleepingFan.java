package com.animania.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import java.util.List;
import java.util.Map;

/** Closes the sleeping fan; 1.12 only tipped the expanded fan backwards. */
final class PeacockSleepingFan {
    static void apply(Map<String, ModelPart> parts, float progress) {
        if (progress <= 0) return;
        float blend = progress * progress * (3 - 2 * progress);
        for (String name : List.of("FanNodeA", "FanNodeB", "FanNodeC", "FanNodeD")) {
            ModelPart fan = parts.get(name);
            fan.xRot = Mth.lerp(blend, fan.xRot, -1.5F);
        }
        // Rotate the existing feather blades towards the central tail axis and
        // bring their spread-out attachment points together. Keep blade size,
        // texture and a small stagger so overlapping planes do not coincide.
        parts.forEach((name, feather) -> {
            if (!name.startsWith("Feather")) return;
            var bind = feather.getInitialPose();
            float side = Math.signum(bind.zRot);
            float rank = Math.abs(bind.zRot);
            feather.x = Mth.lerp(blend, feather.x, bind.x * 0.12F);
            feather.y = Mth.lerp(blend, feather.y, bind.y * 0.18F);
            feather.z = Mth.lerp(blend, feather.z, bind.z + rank * 0.12F + side * 0.035F);
            feather.zRot = Mth.lerp(blend, feather.zRot, bind.zRot * 0.06F);
            feather.xRot = Mth.lerp(blend, feather.xRot, -0.2617994F - rank * 0.015F);
            feather.yRot = Mth.lerp(blend, feather.yRot, 0);
        });
    }
}
