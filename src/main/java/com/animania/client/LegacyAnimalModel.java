package com.animania.client;

import com.animania.common.registry.ModAttachments;
import com.animania.common.config.LegacyConfig;
import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bakes the mechanically converted 1.12 ModelRenderer data without changing its
 * texture offsets, cube coordinates, pivots, rotations, offsets, or hierarchy.
 */
public final class LegacyAnimalModel<T extends Entity> extends EntityModel<T> {
    private static final Gson GSON = new Gson();
    private static final Map<String, LegacyAnimalModel<?>> CACHE = new ConcurrentHashMap<>();
    private static final float LEGACY_PLANE_HALF_THICKNESS = 0.01F;

    private final String key;
    private final GeneratedLegacyMotion.Motion motion;
    private float partialTick;
    private final ModelPart root;
    private final Map<String, ModelPart> parts = new HashMap<>();
    private final Map<String, Float> rootScales = new HashMap<>();
    private final List<String> rootNames;

    private LegacyAnimalModel(String key, ModelData data) {
        this.key = key;
        MeshDefinition mesh = new MeshDefinition();
        Map<String, NodeData> nodes = new HashMap<>();
        for (NodeData node : data.nodes) nodes.put(node.name, node);
        // Original models can animate detached parts that they never draw (for
        // example ModelPeacock.FeatherD1). Bake those too, while keeping the
        // original render-root list separate from the animation part map.
        List<String> animationRoots = data.nodes.stream().filter(node -> node.parent == null)
                .map(node -> node.name).toList();
        for (String rootName : animationRoots) {
            NodeData node = nodes.get(rootName);
            if (node == null) throw new IllegalStateException("Missing legacy root node " + rootName);
            addNode(mesh.getRoot(), node, nodes, new float[]{0, 0, 0}, key);
        }
        root = LayerDefinition.create(mesh, data.textureWidth, data.textureHeight).bakeRoot();
        rootNames = List.copyOf(data.roots);
        collect(root, animationRoots, nodes);
        motion = GeneratedLegacyMotion.create(key, parts);
        for (String rootName : rootNames) {
            NodeData node = nodes.get(rootName);
            rootScales.put(rootName, node == null ? 1.0F : node.renderScale);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> LegacyAnimalModel<T> load(String key) {
        return (LegacyAnimalModel<T>) CACHE.computeIfAbsent(key, LegacyAnimalModel::loadFresh);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> LegacyAnimalModel<T> loadCraftStudio(String key) {
        return (LegacyAnimalModel<T>) CACHE.computeIfAbsent("craftstudio:" + key,
                ignored -> loadFresh("craftstudio_models", key));
    }

    private static LegacyAnimalModel<?> loadFresh(String key) {
        return loadFresh("legacy_models", key);
    }

    private static LegacyAnimalModel<?> loadFresh(String directory, String key) {
        String resource = "/assets/animania/" + directory + "/" + key + ".json";
        try (InputStream stream = LegacyAnimalModel.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing converted legacy model " + resource);
            ModelData data = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), ModelData.class);
            if (data == null || data.nodes == null || data.roots == null) {
                throw new IllegalStateException("Invalid converted legacy model " + resource);
            }
            return new LegacyAnimalModel<>(key, data);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load converted legacy model " + resource, exception);
        }
    }

    private static PartDefinition addNode(PartDefinition parent, NodeData node,
                                          Map<String, NodeData> nodes, float[] parentOffset,
                                          String modelKey) {
        CubeListBuilder cubes = CubeListBuilder.create().texOffs(node.u, node.v).mirror(node.mirror);
        // ModelChick's one-pixel hip cubes sit through the body's lower face.
        // The legacy fixed-function renderer tolerated that intersection, but
        // the modern batched renderer produces visible depth shimmer while the
        // leg roots rotate. The actual leg children already meet the body at
        // the same joint, so keep these nodes as animation pivots without
        // emitting their redundant internal cubes.
        boolean hiddenChickHip = modelKey.endsWith("/modelchick")
                && (node.name.equals("leg1Top") || node.name.equals("leg2Top"));
        for (BoxData box : hiddenChickHip ? List.<BoxData>of() : node.boxes) {
            float x = box.from[0] + node.offset[0];
            float y = box.from[1] + node.offset[1];
            float z = box.from[2] + node.offset[2];
            float width = box.size[0];
            float height = box.size[1];
            float depth = box.size[2];
            // Legacy models used zero-sized cubes as double-sided feather and
            // crest planes. Modern entityCutoutNoCull emits two coplanar faces,
            // which fight in the depth buffer. A 0.02-pixel slab preserves the
            // silhouette and UVs while separating the two faces imperceptibly.
            if (width == 0 && box.deformation == 0) {
                x -= LEGACY_PLANE_HALF_THICKNESS;
                width = LEGACY_PLANE_HALF_THICKNESS * 2;
            }
            if (height == 0 && box.deformation == 0) {
                y -= LEGACY_PLANE_HALF_THICKNESS;
                height = LEGACY_PLANE_HALF_THICKNESS * 2;
            }
            if (depth == 0 && box.deformation == 0) {
                z -= LEGACY_PLANE_HALF_THICKNESS;
                depth = LEGACY_PLANE_HALF_THICKNESS * 2;
            }
            cubes.addBox(x, y, z, width, height, depth,
                    new CubeDeformation(box.deformation));
        }
        PartPose pose = PartPose.offsetAndRotation(node.pivot[0] + parentOffset[0],
                node.pivot[1] + parentOffset[1], node.pivot[2] + parentOffset[2],
                node.rotation[0], node.rotation[1], node.rotation[2]);
        PartDefinition definition = parent.addOrReplaceChild(node.name, cubes, pose);
        for (NodeData child : nodes.values()) {
            if (node.name.equals(child.parent)) addNode(definition, child, nodes, node.offset, modelKey);
        }
        return definition;
    }

    private void collect(ModelPart parent, List<String> names, Map<String, NodeData> nodes) {
        for (String name : names) collect(parent.getChild(name), nodes.get(name), nodes);
    }

    private void collect(ModelPart part, NodeData node, Map<String, NodeData> nodes) {
        parts.put(node.name, part);
        for (NodeData child : nodes.values()) {
            if (node.name.equals(child.parent)) collect(part.getChild(child.name), child, nodes);
        }
    }

    /** Used by layers that must follow the legacy head transform. */
    public ModelPart head() {
        for (String candidate : List.of("Head", "head", "Head1", "headBase", "HeadBase")) {
            ModelPart part = parts.get(candidate);
            if (part != null) return part;
        }
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        applyModestySetting();
        animate(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        restorePetRotationOrder();
    }

    private void restorePetRotationOrder() {
        if (!key.startsWith("catsdogs/client/models/cats/")
                && !key.startsWith("catsdogs/client/models/dogs/")) return;
        // ModelRendererAnimania used CraftStudio's Y-X-Z quaternion order.
        // ModelPart consumes Z-Y-X angles; convert after the original pose animation.
        // JOML 1.10.5's quaternion-to-ZYX getter has an incorrect X denominator.
        // Decompose the original Y-X-Z matrix directly, including its singular pose.
        for (ModelPart part : parts.values()) {
            double sx = Math.sin(part.xRot), cx = Math.cos(part.xRot);
            double sy = Math.sin(part.yRot), cy = Math.cos(part.yRot);
            double sz = Math.sin(part.zRot), cz = Math.cos(part.zRot);
            double m00 = cy * cz + sy * sx * sz;
            double m10 = cx * sz;
            double m21 = sy * sz + cy * sx * cz;
            double m22 = cy * cx;
            double horizontal = Math.hypot(m00, m10);
            double x = horizontal > 1.0E-7 ? Math.atan2(m21, m22) : 0;
            double y = Math.atan2(sy * cz - cy * sx * sz, horizontal);
            double z = horizontal > 1.0E-7 ? Math.atan2(m10, m00)
                    : Math.atan2(cy * sz - sy * sx * cz, cx * cz);
            part.setRotation((float) x, (float) y, (float) z);
        }
    }

    /** Matches the source models' conditional render calls for adult males. */
    private void applyModestySetting() {
        boolean show = LegacyConfig.SHOW_PARTS.get();
        if (key.contains("/modelbull")) setVisible(show, "sac", "penis");
        if (key.contains("/modelhog")) setVisible(show, "BlockA", "BlockB");
        if (key.contains("/modelbuck")) setVisible(show, "Reproductive1", "Reproductive2");
        if (key.endsWith("/modeldrafthorsestallion")) setVisible(show, "Block1", "Block2");
    }

    private void setVisible(boolean visible, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) part.visible = visible;
        }
    }

    @Override
    public void prepareMobModel(T entity, float swing, float amount, float partial) {
        partialTick = partial;
        super.prepareMobModel(entity, swing, amount, partial);
    }

    private void animate(T entity, float swing, float amount, float age, float yaw, float pitch) {
        if (entity.getData(ModAttachments.SLEEPING)) {
            if ((key.endsWith("/modelhamster") || key.endsWith("/modelpeacock")) && motion != null)
                motion.apply(new LegacyMotionContext(entity), 0, 0, 1, 0, 0, 0);
            if (key.startsWith("catsdogs/")) {
                LegacyPose.load(key, "sleeping").blend(parts, LegacySleepAnimation.petBlend(entity, partialTick));
            } else if (!GeneratedLegacySleep.apply(key, parts, LegacySleepAnimation.timer(entity, partialTick))) {
                LegacyPose.load(key, "sleeping").apply(parts);
            }
            if (key.endsWith("/modelpeacock")) {
                PeacockSleepingFan.apply(parts, LegacySleepAnimation.petBlend(entity, partialTick));
            }
            // Sleeping entities retain a fixed pose, including during blinking layers.
            return;
        }
        if (motion != null) motion.apply(new LegacyMotionContext(entity), swing, amount, age, yaw, pitch, partialTick);
        if (key.startsWith("farm/client/model/goats/")) {
            // All 21 original goat models reset head yaw in their awake render branch.
            ModelPart head = parts.get("HeadNode");
            if (head != null) head.yRot = 0;
        }
        if (key.endsWith("/modelpeacock"))
            PeacockSleepingFan.apply(parts, LegacySleepAnimation.petBlend(entity, partialTick));
        if (key.startsWith("catsdogs/") && !(entity instanceof TamableAnimal tame && tame.isInSittingPose()))
            LegacyPose.load(key, "sleeping").blend(parts, LegacySleepAnimation.petBlend(entity, partialTick));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        for (String rootName : rootNames) {
            ModelPart part = parts.get(rootName);
            if (part == null) continue;
            float scale = rootScales.getOrDefault(rootName, 1.0F);
            if (Math.abs(scale - 1.0F) < 0.00001F) {
                part.render(poseStack, consumer, light, overlay, color);
            } else {
                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);
                part.render(poseStack, consumer, light, overlay, color);
                poseStack.popPose();
            }
        }
    }

    private static final class ModelData {
        int textureWidth;
        int textureHeight;
        List<String> roots;
        List<NodeData> nodes;
    }

    private static final class NodeData {
        String name;
        int u;
        int v;
        boolean mirror;
        float[] pivot;
        float[] offset;
        float[] rotation;
        List<BoxData> boxes;
        String parent;
        float renderScale = 1.0F;
    }

    private static final class BoxData {
        float[] from;
        float[] size;
        float deformation;
    }
}
