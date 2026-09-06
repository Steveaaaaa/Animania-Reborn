package com.animania.client;

import com.google.gson.Gson;
import net.minecraft.client.model.geom.ModelPart;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Constant state pose mechanically extracted from an original 1.12 model. */
final class LegacyPose {
    private static final Gson GSON = new Gson();
    private static final LegacyPose EMPTY = new LegacyPose(Map.of());
    private static final Map<String, LegacyPose> CACHE = new ConcurrentHashMap<>();
    private final Map<String, PartData> parts;

    private LegacyPose(Map<String, PartData> parts) {
        this.parts = parts;
    }

    static LegacyPose load(String modelKey, String poseName) {
        String key = modelKey + "/" + poseName;
        return CACHE.computeIfAbsent(key, LegacyPose::loadFresh);
    }

    private static LegacyPose loadFresh(String key) {
        String resource = "/assets/animania/legacy_animation_poses/" + key + ".json";
        try (InputStream stream = LegacyPose.class.getResourceAsStream(resource)) {
            if (stream == null) return EMPTY;
            PoseData data = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PoseData.class);
            return data == null || data.parts == null ? EMPTY : new LegacyPose(data.parts);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load converted legacy pose " + resource, exception);
        }
    }

    void apply(Map<String, ModelPart> modelParts) {
        parts.forEach((name, data) -> {
            ModelPart part = modelParts.get(name);
            if (part == null) return;
            if (data.pivot != null) {
                part.x = data.pivot[0];
                part.y = data.pivot[1];
                part.z = data.pivot[2];
            }
            if (data.rotation != null) {
                if (data.rotation[0] != null) part.xRot = data.rotation[0];
                if (data.rotation[1] != null) part.yRot = data.rotation[1];
                if (data.rotation[2] != null) part.zRot = data.rotation[2];
            }
        });
    }

    void blend(Map<String, ModelPart> modelParts, float slide) {
        if (slide <= 0) return;
        parts.forEach((name, data) -> {
            ModelPart part = modelParts.get(name);
            if (part == null) return;
            if (data.pivot != null) {
                part.x = net.minecraft.util.Mth.lerp(slide, part.x, data.pivot[0]);
                part.y = net.minecraft.util.Mth.lerp(slide, part.y, data.pivot[1]);
                part.z = net.minecraft.util.Mth.lerp(slide, part.z, data.pivot[2]);
            }
            if (data.rotation != null) {
                if (data.rotation[0] != null) part.xRot = blendRotation(part.xRot, data.rotation[0], slide);
                if (data.rotation[1] != null) part.yRot = blendRotation(part.yRot, data.rotation[1], slide);
                if (data.rotation[2] != null) part.zRot = blendRotation(part.zRot, data.rotation[2], slide);
            }
        });
    }

    // ModelPose.lerpRotation uses modulo 180 degrees, rather than shortest-path interpolation.
    private static float blendRotation(float from, float to, float slide) {
        double mul = 180 / Math.PI;
        double a = (from * mul) % 180 + 180, b = (to * mul) % 180 + 180;
        return (float) ((a + (b - a) * slide - 180) / mul);
    }

    int partCount() {
        return parts.size();
    }

    private static final class PoseData {
        Map<String, PartData> parts;
    }

    private static final class PartData {
        Float[] pivot;
        Float[] rotation;
    }
}
