package com.animania.client;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Losslessly renders the cuboids exported by the CraftStudio API used by
 * Animania 1.12. This deliberately does not route the data through ModelPart:
 * CraftStudio has a different per-face UV layout, supports skewed cuboids, and
 * composes its Euler angles with a quaternion in a different order.
 */
public final class CraftStudioModel {
    private static final Gson GSON = new Gson();
    private static final Map<String, CraftStudioModel> CACHE = new ConcurrentHashMap<>();
    private static final int[][] FACES = {
            {5, 1, 2, 6}, {0, 4, 7, 3}, {5, 4, 0, 1},
            {2, 3, 7, 6}, {1, 0, 3, 2}, {4, 5, 6, 7}
    };

    private final int textureWidth;
    private final int textureHeight;
    private final int nodeCount;
    private final List<Node> roots;

    private CraftStudioModel(ModelData data) {
        textureWidth = data.textureWidth;
        textureHeight = data.textureHeight;
        nodeCount = data.nodes.size();
        Map<String, Node> byId = new HashMap<>();
        for (NodeData node : data.nodes) {
            if (node.id == null || byId.put(node.id, new Node(node)) != null) {
                throw new IllegalStateException("Missing or duplicate CraftStudio node ID " + node.id);
            }
        }
        for (NodeData node : data.nodes) {
            if (node.parentId != null) {
                Node parent = byId.get(node.parentId);
                if (parent == null) throw new IllegalStateException("Missing CraftStudio parent " + node.parentId);
                parent.children.add(byId.get(node.id));
            }
        }
        roots = data.roots.stream().map(byId::get).toList();
        if (roots.stream().anyMatch(node -> node == null)) {
            throw new IllegalStateException("Missing CraftStudio root node");
        }
        validateHierarchy();
    }

    public static CraftStudioModel load(String key) {
        return CACHE.computeIfAbsent(key, CraftStudioModel::loadFresh);
    }

    private static CraftStudioModel loadFresh(String key) {
        String resource = "/assets/animania/craftstudio_models/" + key + ".json";
        try (InputStream stream = CraftStudioModel.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing converted CraftStudio model " + resource);
            ModelData data = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), ModelData.class);
            if (data == null || data.nodes == null || data.roots == null) {
                throw new IllegalStateException("Invalid converted CraftStudio model " + resource);
            }
            return new CraftStudioModel(data);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load converted CraftStudio model " + resource, exception);
        }
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        render(poseStack, consumer, light, overlay, color, null, 0, false);
    }

    public void renderAnimated(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color,
                               CraftStudioAnimation animation, float frame, boolean reverse) {
        render(poseStack, consumer, light, overlay, color, animation, frame, reverse);
    }

    private void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color,
                        CraftStudioAnimation animation, float frame, boolean reverse) {
        for (Node root : roots) root.render(poseStack, consumer, light, overlay, color,
                textureWidth, textureHeight, animation, frame, reverse);
    }

    int nodeCount() {
        return nodeCount;
    }

    private void validateHierarchy() {
        Set<Node> visiting = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node root : roots) validateNode(root, visiting, visited);
        if (visited.size() != nodeCount) {
            throw new IllegalStateException("Disconnected CraftStudio hierarchy: reached "
                    + visited.size() + " of " + nodeCount + " nodes");
        }
    }

    private static void validateNode(Node node, Set<Node> visiting, Set<Node> visited) {
        if (!visiting.add(node)) {
            throw new IllegalStateException("Cyclic CraftStudio hierarchy at node " + node.name);
        }
        if (!visited.add(node)) {
            throw new IllegalStateException("CraftStudio node has multiple parents: " + node.name);
        }
        for (Node child : node.children) validateNode(child, visiting, visited);
        visiting.remove(node);
    }

    static Quaternionf craftStudioRotation(float[] degrees) {
        float x = (float) Math.toRadians(degrees[0]);
        float y = (float) Math.toRadians(degrees[1]);
        float z = (float) Math.toRadians(degrees[2]);
        float cx = (float) Math.cos(x * 0.5F);
        float cy = (float) Math.cos(y * 0.5F);
        float cz = (float) Math.cos(z * 0.5F);
        float sx = (float) Math.sin(x * 0.5F);
        float sy = (float) Math.sin(y * 0.5F);
        float sz = (float) Math.sin(z * 0.5F);
        return new Quaternionf(
                sx * cy * cz + cx * sy * sz,
                cx * sy * cz - sx * cy * sz,
                cx * cy * sz - sx * sy * cz,
                cx * cy * cz + sx * sy * sz
        );
    }

    private static final class Node {
        private final float[] pivot;
        private final float[] offset;
        private final float[] rotation;
        private final float[] stretch;
        private final String name;
        private final float[][] vertices;
        private final int[][] uv;
        private final boolean invertedWinding;
        private final List<Node> children = new ArrayList<>();

        private Node(NodeData data) {
            name = data.name;
            pivot = data.pivot;
            offset = data.offset;
            rotation = data.rotation;
            stretch = data.stretch;
            vertices = data.vertices;
            uv = data.uv;
            // Some CraftStudio blocks mirror their custom vertex coordinates
            // instead of using a negative scale. Their winding is therefore
            // reversed even though the geometry and UVs are correct. Keep the
            // original vertices/UVs intact, but correct both the normal and the
            // emitted winding. Shader pipelines also derive normals/tangents
            // and front-facing state from the submitted vertex order.
            Vector3f origin = vector(0);
            Vector3f axisX = vector(1).sub(origin, new Vector3f());
            Vector3f axisY = vector(3).sub(origin, new Vector3f());
            Vector3f axisZ = vector(4).sub(origin, new Vector3f());
            invertedWinding = axisX.dot(axisY.cross(axisZ, new Vector3f())) < 0.0F;
        }

        private void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                            int color, int textureWidth, int textureHeight,
                            CraftStudioAnimation animation, float frame, boolean reverse) {
            CraftStudioAnimation.Transform transform = animation == null ? null
                    : animation.sample(name, pivot, offset, rotation, stretch, frame, reverse);
            float[] renderedPivot = transform == null ? pivot : transform.position();
            float[] renderedOffset = transform == null ? offset : transform.offset();
            float[] renderedStretch = transform == null ? stretch : transform.stretch();
            Quaternionf renderedRotation = transform == null
                    ? craftStudioRotation(rotation) : transform.rotation();
            poseStack.pushPose();
            poseStack.translate(renderedPivot[0] / 16.0F, renderedPivot[1] / 16.0F, renderedPivot[2] / 16.0F);
            poseStack.mulPose(renderedRotation);
            poseStack.translate(renderedOffset[0] / 16.0F, renderedOffset[1] / 16.0F, renderedOffset[2] / 16.0F);
            poseStack.scale(renderedStretch[0], renderedStretch[1], renderedStretch[2]);
            emitCube(poseStack, consumer, light, overlay, color, textureWidth, textureHeight);
            for (Node child : children) {
                child.render(poseStack, consumer, light, overlay, color, textureWidth, textureHeight,
                        animation, frame, reverse);
            }
            poseStack.popPose();
        }

        private void emitCube(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                              int color, int textureWidth, int textureHeight) {
            // A reflected pose reverses winding once more. The normal matrix
            // already handles that reflection for the supplied lighting normal.
            boolean reverseWinding = invertedWinding ^ (poseStack.last().pose().determinant3x3() < 0.0F);
            for (int faceIndex = 0; faceIndex < FACES.length; faceIndex++) {
                int[] face = FACES[faceIndex];
                Vector3f a = vector(face[0]);
                Vector3f b = vector(face[1]);
                Vector3f c = vector(face[2]);
                Vector3f normal = b.sub(a, new Vector3f()).cross(c.sub(a, new Vector3f()));
                // CraftStudio files use zero-sized root nodes as transform-only
                // containers. Do not submit their degenerate quads with NaN normals.
                if (normal.lengthSquared() < 1.0E-12F) continue;
                normal.normalize();
                if (invertedWinding) normal.negate();
                int[] rect = uv[faceIndex];
                float u1 = rect[0] / (float) textureWidth;
                float v1 = rect[1] / (float) textureHeight;
                float u2 = rect[2] / (float) textureWidth;
                float v2 = rect[3] / (float) textureHeight;
                vertex(poseStack, consumer, face[0], u2, v1, normal, light, overlay, color);
                if (reverseWinding) {
                    // Keep each UV attached to its original vertex; reverse the
                    // traversal only, without mirroring or rotating the texture.
                    vertex(poseStack, consumer, face[3], u2, v2, normal, light, overlay, color);
                    vertex(poseStack, consumer, face[2], u1, v2, normal, light, overlay, color);
                    vertex(poseStack, consumer, face[1], u1, v1, normal, light, overlay, color);
                } else {
                    vertex(poseStack, consumer, face[1], u1, v1, normal, light, overlay, color);
                    vertex(poseStack, consumer, face[2], u1, v2, normal, light, overlay, color);
                    vertex(poseStack, consumer, face[3], u2, v2, normal, light, overlay, color);
                }
            }
        }

        private Vector3f vector(int index) {
            return new Vector3f(vertices[index][0], vertices[index][1], vertices[index][2]);
        }

        private void vertex(PoseStack poseStack, VertexConsumer consumer, int index, float u, float v,
                            Vector3f normal, int light, int overlay, int color) {
            float[] point = vertices[index];
            consumer.addVertex(poseStack.last(), point[0] / 16.0F, point[1] / 16.0F, point[2] / 16.0F)
                    .setColor(color)
                    .setUv(u, v)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(poseStack.last(), normal.x, normal.y, normal.z);
        }
    }

    private static final class ModelData {
        int textureWidth;
        int textureHeight;
        List<String> roots;
        List<NodeData> nodes;
    }

    private static final class NodeData {
        String id;
        String name;
        float[] pivot;
        float[] offset;
        float[] rotation;
        float[] stretch;
        float[][] vertices;
        int[][] uv;
        String parentId;
    }
}
