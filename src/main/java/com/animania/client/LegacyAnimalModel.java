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
    private final ModelPart root;
    private final Map<String, ModelPart> parts = new HashMap<>();
    private final Map<String, Float> rootScales = new HashMap<>();
    private final List<String> rootNames;

    private LegacyAnimalModel(String key, ModelData data) {
        this.key = key;
        MeshDefinition mesh = new MeshDefinition();
        Map<String, NodeData> nodes = new HashMap<>();
        for (NodeData node : data.nodes) nodes.put(node.name, node);
        for (String rootName : data.roots) {
            NodeData node = nodes.get(rootName);
            if (node == null) throw new IllegalStateException("Missing legacy root node " + rootName);
            addNode(mesh.getRoot(), node, nodes, new float[]{0, 0, 0}, key);
        }
        root = LayerDefinition.create(mesh, data.textureWidth, data.textureHeight).bakeRoot();
        rootNames = List.copyOf(data.roots);
        collect(root, data.roots, nodes);
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

    /**
     * Replays the shared equations used by the original 1.12 model families. The
     * converted bind pose remains authoritative; animation is applied only after
     * resetPose(), so it cannot accumulate from one rendered entity to the next.
     */
    private void animate(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                         float netHeadYaw, float headPitch) {
        boolean sleeping = entity != null && entity.getData(ModAttachments.SLEEPING);
        if (sleeping) {
            LegacyPose.load(key, "sleeping").apply(parts);
            // No watch-player, eating or idle animation may overwrite a resting
            // pose. Families without an extracted pose retain their bind pose.
            return;
        }
        float walkA = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float walkB = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        float tailSway = Mth.sin(ageInTicks * Mth.PI * 0.05F)
                * Mth.sin(ageInTicks * Mth.PI * 0.0015F) * 0.15F * Mth.PI;
        int eatingTicks = entity == null ? 0 : entity.getData(ModAttachments.EATING_TICKS);
        float eatAnchor = eatingTicks <= 0 ? 0.0F
                : eatingTicks >= 4 && eatingTicks <= 76 ? 1.0F
                : eatingTicks < 4 ? eatingTicks / 4.0F : (80.0F - eatingTicks) / 4.0F;
        float eatAngle = eatingTicks > 4 && eatingTicks <= 76
                ? Mth.PI / 5.0F + Mth.PI * 7.0F / 150.0F
                * Mth.sin((eatingTicks - 4.0F) / 24.0F * 28.7F)
                : eatingTicks > 0 ? Mth.PI / 5.0F : Float.NaN;

        if (key.contains("/pig/")) {
            face(netHeadYaw, headPitch, "Head");
            applyEating(eatAnchor, eatAngle, 5.5F, "Head");
            setX(walkA, "Leg1", "Leg4");
            setX(walkB, "Leg2", "Leg3");
            addY(tailSway * 0.45F, "Tail1");
            return;
        }
        if (key.contains("/cow/")) {
            face(netHeadYaw, headPitch, "Head", "head");
            applyEating(eatAnchor, eatAngle, 9.0F, "Head", "head");
            setX(walkA, "Leg0", "Leg3", "leg1", "leg4");
            setX(walkB, "Leg1", "Leg2", "leg2", "leg3");
            // The legacy render method initially used -PI, then replaced it with
            // this value every frame. Keeping -PI made the tail fold into the body.
            setY(tailSway, "Tail");
            return;
        }
        if (key.contains("/goats/")) {
            face(netHeadYaw, headPitch, "HeadNode");
            applyEating(eatAnchor, eatAngle, 6.0F, "HeadNode");
            setX(walkA, "BackLeg_L", "BackLegWool_L", "FrontLeg_R", "FrontLegWool_R");
            setX(walkB, "BackLeg_R", "BackLegWool_R", "FrontLeg_L", "FrontLegWool_L");
            setY(tailSway, "Tail");
            return;
        }
        if (key.contains("/sheep/")) {
            face(netHeadYaw, headPitch, "HeadNode");
            applyEating(eatAnchor, eatAngle, 4.0F, "HeadNode");
            setX(walkA, "LeftBackLeg", "LeftBackLegWool", "RightFrontLeg", "RightFrontLegWool");
            setX(walkB, "RightBackLeg", "RightBackLegWool", "LeftFrontLeg", "LeftFrontLegWool");
            setY(tailSway, "Tail");
            return;
        }
        if (key.contains("/horse/")) {
            face(netHeadYaw, headPitch, "HeadNode");
            applyEating(eatAnchor, Float.isNaN(eatAngle) ? eatAngle : 0.687F + eatAngle,
                    10.0F, "HeadNode");
            float horseA = walkA / 1.4F;
            float horseB = walkB / 1.4F;
            setX(horseA, "BackLeftMuscle", "FrontRightMuscle");
            setX(horseB, "BackRightMuscle", "FrontLeftMuscle");
            addY(tailSway * 0.35F, "TailNode");
            return;
        }
        if (key.contains("/chicken/")) {
            face(netHeadYaw, headPitch, "Neck");
            if (key.endsWith("/modelchick")) {
                setX(walkA, "leg1Top");
                setX(walkB, "leg2Top");
            } else {
                setX(walkA, "leg1Pivot", "Leg1Pivot");
                setX(walkB, "leg2Pivot", "Leg2Pivot");
            }
            float flapStrength = entity != null && !entity.onGround() ? 0.75F
                    : Mth.clamp(limbSwingAmount * 0.08F, 0, 0.08F);
            float flap = Mth.sin(ageInTicks * 0.8F) * flapStrength;
            addZ(flap, "Wing1", "wing1", "wing3");
            addZ(-flap, "Wing2", "wing2", "wing4");
            return;
        }
        if (key.contains("/peafowl/")) {
            face(netHeadYaw, headPitch, "Neck");
            setX(walkA, "leg1Top");
            setX(walkB, "leg2Top");
            float flapStrength = entity != null && !entity.onGround() ? 0.75F
                    : Mth.clamp(limbSwingAmount * 0.08F, 0, 0.08F);
            float flap = Mth.sin(ageInTicks * 0.8F) * flapStrength;
            addZ(flap, "Wing1");
            addZ(-flap, "Wing2");
            return;
        }
        if (key.contains("/rabbits/")) {
            face(netHeadYaw, headPitch, "Neck1");
            addX(walkA, "BackLegL1", "BackLegL2", "BackLegR1", "BackLegR2");
            addX(walkB, "LegL1", "LegR1");
            addY(tailSway * 0.25F, "Tail");
            return;
        }
        if (key.endsWith("/modelferret")) {
            face(netHeadYaw, headPitch, "Head");
            setX(walkA, "PawLF", "PawRB");
            setX(walkB, "PawRF", "PawLB");
            addY(tailSway * 0.5F, "Tail");
            return;
        }
        if (key.endsWith("/modelhamster")) {
            faceAll(netHeadYaw, headPitch, "hamsterHead", "hamsterNose", "hamsterEarRight", "hamsterEarLeft");
            float hamsterA = Mth.cos(limbSwing * 1.5F) * 1.4F * limbSwingAmount;
            float hamsterB = Mth.cos(limbSwing * 1.5F + Mth.PI) * 1.4F * limbSwingAmount;
            setX(hamsterA, "hamsterLegBackRight", "hamsterLegFrontLeft");
            setX(hamsterB, "hamsterLegBackLeft", "hamsterLegFrontRight");
            addZ(tailSway, "hamsterTail");
            return;
        }
        if (key.endsWith("/modelhedgehog")) {
            face(netHeadYaw, headPitch, "HeadNode", "Head");
            setX(walkA, "LegFrontLeft", "LegFrontLeftFoot", "LegFrontRight", "LegFrontLeftFoot3");
            setX(walkB, "LegBackLeft", "LegFrontLeftFoot1", "LegBackRight", "LegFrontLeftFoot2");
            return;
        }
        if (key.contains("/cats/")) {
            faceScaled(netHeadYaw, headPitch, 0.001453292F, "neck1");
            boolean sitting = entity instanceof TamableAnimal tame && tame.isInSittingPose();
            if (!sitting) {
                addX(walkA * 0.6F, "back_leg_l1", "leg_r1");
                addX(walkB * 0.6F, "back_leg_r1", "leg_l1");
            }
            setY(tailSway, "tail");
            if (sitting) LegacyPose.load(key, "sitting").apply(parts);
            return;
        }
        if (key.contains("/dogs/")) {
            faceScaled(netHeadYaw, headPitch, 0.001453292F, "neck1", "neck", "pug_head");
            boolean sitting = entity instanceof TamableAnimal tame && tame.isInSittingPose();
            if (!sitting) {
                addX(walkA * 0.6F, "back_leg_l1", "leg_r1");
                addX(walkB * 0.6F, "back_leg_r1", "leg_l1");
                // Dachshund uses the older flat four-leg naming scheme.
                setX(walkA * 0.6F, "leg1", "leg4");
                setX(walkB * 0.6F, "leg2", "leg3");
                // Pomeranian, pug and chihuahua share this compact four-leg
                // layout. These are the exact pairings used by their 1.12
                // setRotationAngles implementations.
                addX(walkA * 0.6F + 0.06981317F, "back_left", "front_right");
                addX(walkB * 0.6F + 0.06981317F, "back_right", "front_left");
            }
            setY(tailSway, "tail");
            faceScaled(netHeadYaw, headPitch, 0.001453292F, "head");
            if (sitting) LegacyPose.load(key, "sitting").apply(parts);
            return;
        }
        // Frogs and toads had only a static legacy render pose. Give their limb
        // roots a restrained stride so locomotion is visible without disturbing it.
        if (key.contains("/amphibians/")) {
            addX(walkA * 0.35F, "HindLegL", "FrontLegRTop");
            addX(walkB * 0.35F, "HindLegR", "FrontLegLTop");
        }
    }

    private void face(float yawDegrees, float pitchDegrees, String... names) {
        ModelPart part = first(names);
        if (part != null) {
            part.yRot = yawDegrees * Mth.DEG_TO_RAD;
            part.xRot = pitchDegrees * Mth.DEG_TO_RAD;
        }
    }

    private void applyEating(float anchor, float angle, float travel, String... names) {
        if (anchor <= 0.0F) return;
        ModelPart part = first(names);
        if (part != null) {
            part.y += anchor * travel;
            if (!Float.isNaN(angle)) part.xRot = angle;
        }
    }

    private void faceScaled(float yawDegrees, float pitchDegrees, float pitchScale, String... names) {
        ModelPart part = first(names);
        if (part != null) {
            part.yRot = yawDegrees * Mth.DEG_TO_RAD;
            part.xRot += pitchDegrees * pitchScale;
        }
    }

    private void faceAll(float yawDegrees, float pitchDegrees, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) {
                part.yRot = yawDegrees * Mth.DEG_TO_RAD;
                part.xRot = pitchDegrees * Mth.DEG_TO_RAD;
            }
        }
    }

    private ModelPart first(String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) return part;
        }
        return null;
    }

    private void setX(float value, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) part.xRot = value;
        }
    }

    private void setY(float value, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) part.yRot = value;
        }
    }

    private void addX(float value, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) part.xRot += value;
        }
    }

    private void addY(float value, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) part.yRot += value;
        }
    }

    private void addZ(float value, String... names) {
        for (String name : names) {
            ModelPart part = parts.get(name);
            if (part != null) part.zRot += value;
        }
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
