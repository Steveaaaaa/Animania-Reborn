package com.animania.client;

import com.google.gson.Gson;
import org.joml.Quaternionf;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Linear/spherical-keyframe player compatible with CraftStudio API 1.0.1.95. */
public final class CraftStudioAnimation {
    private static final Gson GSON = new Gson();
    private static final Map<String, CraftStudioAnimation> CACHE = new ConcurrentHashMap<>();

    private final float duration;
    private final Map<String, NodeTrack> tracks = new HashMap<>();

    private CraftStudioAnimation(AnimationData data) {
        duration = data.duration;
        if (data.nodeAnimations != null) {
            data.nodeAnimations.forEach((name, node) -> tracks.put(name, new NodeTrack(node)));
        }
    }

    public static CraftStudioAnimation load(String key) {
        return CACHE.computeIfAbsent(key, CraftStudioAnimation::loadFresh);
    }

    private static CraftStudioAnimation loadFresh(String key) {
        String resource = "/assets/animania/craftstudio_animations/" + key + ".json";
        try (InputStream stream = CraftStudioAnimation.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing CraftStudio animation " + resource);
            AnimationData data = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), AnimationData.class);
            if (data == null || data.duration <= 0) {
                throw new IllegalStateException("Invalid CraftStudio animation " + resource);
            }
            return new CraftStudioAnimation(data);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load CraftStudio animation " + resource, exception);
        }
    }

    Transform sample(String nodeName, float[] defaultPosition, float[] defaultOffset,
                     float[] defaultRotation, float[] defaultStretch, float frame, boolean reverse) {
        NodeTrack track = tracks.get(nodeName);
        if (track == null) {
            return new Transform(defaultPosition, defaultOffset,
                    CraftStudioModel.craftStudioRotation(defaultRotation), defaultStretch);
        }
        float time = frame % duration;
        if (time < 0) time += duration;
        if (reverse) time = duration - time;
        if (time >= duration) time = 0;
        float[] position = track.position.sample(defaultPosition, time, duration, true);
        float[] offset = track.offset.sample(defaultOffset, time, duration, true);
        float[] stretch = track.stretch.sample(defaultStretch, time, duration, false);
        Quaternionf rotation = track.rotation.sampleRotation(defaultRotation, time, duration);
        return new Transform(position, offset, rotation, stretch);
    }

    int animatedNodeCount() {
        return tracks.size();
    }

    float duration() {
        return duration;
    }

    record Transform(float[] position, float[] offset, Quaternionf rotation, float[] stretch) {
    }

    private static final class NodeTrack {
        final KeyTrack position;
        final KeyTrack offset;
        final KeyTrack rotation;
        final KeyTrack stretch;

        NodeTrack(NodeAnimationData data) {
            position = new KeyTrack(data.position, true);
            offset = new KeyTrack(data.offsetFromPivot, true);
            rotation = new KeyTrack(data.rotation, true);
            stretch = new KeyTrack(data.stretch, false);
        }
    }

    private static final class KeyTrack {
        final float[] times;
        final float[][] deltas;

        KeyTrack(Map<String, float[]> source, boolean flipYZ) {
            if (source == null || source.isEmpty()) {
                times = new float[0];
                deltas = new float[0][];
                return;
            }
            times = source.keySet().stream().mapToDouble(Double::parseDouble).sorted()
                    .collect(() -> new FloatCollector(source.size()), FloatCollector::add, FloatCollector::addAll)
                    .toArray();
            deltas = new float[times.length][];
            for (int i = 0; i < times.length; i++) {
                float[] raw = source.get(formatFrame(times[i], source));
                if (raw == null) {
                    for (Map.Entry<String, float[]> entry : source.entrySet()) {
                        if (Float.parseFloat(entry.getKey()) == times[i]) {
                            raw = entry.getValue();
                            break;
                        }
                    }
                }
                deltas[i] = raw.clone();
                if (flipYZ) {
                    deltas[i][1] = -deltas[i][1];
                    deltas[i][2] = -deltas[i][2];
                }
            }
        }

        private static String formatFrame(float time, Map<String, float[]> source) {
            String integer = Integer.toString((int) time);
            return source.containsKey(integer) ? integer : Float.toString(time);
        }

        float[] sample(float[] defaults, float time, float duration, boolean ignored) {
            if (times.length == 0) return defaults;
            Span span = span(time, duration);
            float[] from = absolute(defaults, deltas[span.from]);
            float[] to = absolute(defaults, deltas[span.to]);
            return new float[]{
                    lerp(from[0], to[0], span.alpha),
                    lerp(from[1], to[1], span.alpha),
                    lerp(from[2], to[2], span.alpha)
            };
        }

        Quaternionf sampleRotation(float[] defaults, float time, float duration) {
            if (times.length == 0) return CraftStudioModel.craftStudioRotation(defaults);
            Span span = span(time, duration);
            Quaternionf from = CraftStudioModel.craftStudioRotation(absolute(defaults, deltas[span.from]));
            Quaternionf to = CraftStudioModel.craftStudioRotation(absolute(defaults, deltas[span.to]));
            return from.slerp(to, span.alpha, new Quaternionf());
        }

        private Span span(float time, float duration) {
            int next = Arrays.binarySearch(times, time);
            if (next >= 0) return new Span(next, next, 0);
            next = -next - 1;
            int from = next - 1;
            float fromTime;
            float toTime;
            if (from < 0) {
                from = times.length - 1;
                fromTime = times[from] - duration;
            } else {
                fromTime = times[from];
            }
            if (next >= times.length) {
                next = 0;
                toTime = times[0] + duration;
            } else {
                toTime = times[next];
            }
            float denominator = toTime - fromTime;
            float alpha = denominator == 0 ? 0 : (time - fromTime) / denominator;
            return new Span(from, next, Math.max(0, Math.min(1, alpha)));
        }

        private static float[] absolute(float[] defaults, float[] delta) {
            return new float[]{defaults[0] + delta[0], defaults[1] + delta[1], defaults[2] + delta[2]};
        }

        private static float lerp(float from, float to, float alpha) {
            return from + (to - from) * alpha;
        }
    }

    private record Span(int from, int to, float alpha) {
    }

    /** Tiny primitive collector used only while loading JSON, avoiding boxed sort output. */
    private static final class FloatCollector {
        private float[] values;
        private int size;

        FloatCollector(int capacity) {
            values = new float[Math.max(1, capacity)];
        }

        void add(double value) {
            if (size == values.length) values = Arrays.copyOf(values, size * 2);
            values[size++] = (float) value;
        }

        void addAll(FloatCollector other) {
            for (int i = 0; i < other.size; i++) add(other.values[i]);
        }

        float[] toArray() {
            return Arrays.copyOf(values, size);
        }
    }

    private static final class AnimationData {
        int duration;
        boolean holdLastKeyframe;
        Map<String, NodeAnimationData> nodeAnimations;
    }

    private static final class NodeAnimationData {
        Map<String, float[]> position;
        Map<String, float[]> offsetFromPivot;
        Map<String, float[]> size;
        Map<String, float[]> rotation;
        Map<String, float[]> stretch;
    }
}
