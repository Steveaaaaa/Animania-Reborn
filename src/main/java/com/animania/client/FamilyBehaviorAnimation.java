package com.animania.client;

import com.animania.common.entity.FamilyAnimationState;
import com.animania.common.registry.ModAttachments;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import java.util.*;

/** Additive poses over each breed's original model, with an eight-tick entry/exit. */
final class FamilyBehaviorAnimation {
    private static final Map<Entity, Transition> TRANSITIONS = new WeakHashMap<>();
    private static final class Transition { int pose; float blend; double time; }
    private FamilyBehaviorAnimation() {}
    static void apply(Entity entity, String key, Map<String, ModelPart> parts, List<String> roots, float partial) {
        int requested = entity.getData(ModAttachments.FAMILY_POSE);
        if (entity.getData(ModAttachments.SLEEPING) || entity.isOnFire()
                || entity instanceof net.minecraft.world.entity.LivingEntity living && living.hurtTime > 0) {
            TRANSITIONS.remove(entity); return;
        }
        if (requested == 0 && !TRANSITIONS.containsKey(entity)) return;
        Transition state = TRANSITIONS.computeIfAbsent(entity, ignored -> new Transition());
        double now = entity.level().getGameTime() + partial;
        float dt = (float) Math.max(0, Math.min(2, now - state.time)); state.time = now;
        if (requested != 0 && state.pose != requested) { state.pose = requested; state.blend = 0; }
        state.blend = Mth.clamp(state.blend + (requested == 0 ? -dt : dt) / 8, 0, 1);
        float blend = state.blend;
        if (requested == 0 && blend == 0) { TRANSITIONS.remove(entity); return; }
        float t = entity.tickCount + partial;
        ModelPart head = first(parts, "HeadNode", "head_base", "headBase", "HeadBase", "Head", "head", "Head1", "pug_head", "Neck", "Neck1", "neck_base", "neck1", "hamsterHead");
        float oldHeadX = head == null ? 0 : head.xRot;
        float oldHeadZ = head == null ? 0 : head.zRot;
        boolean bird = entity instanceof net.minecraft.world.entity.animal.Animal animal
                && com.animania.common.entity.FamilyLifecycle.bird(animal);
        switch (state.pose) {
            case FamilyAnimationState.COURT -> {
                if (head != null) { head.xRot += (-.12F + .07F * Mth.sin(t * .12F)) * blend; head.zRot += .04F * Mth.sin(t * .08F) * blend; }
                if (bird) wings(parts, (.12F + .035F * Mth.sin(t * .16F)) * blend);
                ModelPart tail = first(parts, "tail", "Tail", "Tail1");
                if (!bird && tail != null) tail.yRot += .10F * Mth.sin(t * .12F) * blend;
            }
            case FamilyAnimationState.NURSE, FamilyAnimationState.NUZZLE -> {
                if (head != null) { head.xRot += (.22F + .025F * Mth.sin(t * .18F)) * blend; head.zRot += .08F * blend; }
                if (state.pose == FamilyAnimationState.NURSE && entity instanceof net.minecraft.world.entity.animal.Animal a
                        && (com.animania.common.entity.FamilyLifecycle.nestYoung(a) || a instanceof com.animania.farm.livestock.AnimaniaPig)) {
                    lower(parts, roots, .7F * blend);
                    if (a instanceof com.animania.farm.livestock.AnimaniaPig) {
                        for (String name : new String[]{"Leg1", "Leg2", "Leg3", "Leg4"}) {
                            ModelPart leg = parts.get(name); if (leg != null) leg.xRot += .12F * blend;
                        }
                    }
                    for (String name : new String[]{"leg_l1", "leg_r1", "front_left", "front_right", "hamsterLegFrontLeft", "hamsterLegFrontRight", "PawLF", "PawRF", "LegFrontLeft", "LegFrontRight", "left_front_leg", "right_front_leg"}) {
                        ModelPart leg = parts.get(name); if (leg != null) leg.xRot -= .2F * blend;
                    }
                }
            }
            case FamilyAnimationState.SUCKLE -> {
                if (head != null) { head.xRot = Mth.lerp(blend, head.xRot, head.getInitialPose().xRot - .22F + .025F * Mth.sin(t * .3F)); head.yRot *= 1 - blend; }
                ModelPart nose = first(parts, "Snout", "snout", "Muzzle", "Nose", "nose", "hamsterNose");
                if (nose != null) nose.z += .035F * Mth.sin(t * .3F) * blend;
            }
            case FamilyAnimationState.BROOD -> {
                lower(parts, roots, 1.5F * blend);
                for (String name : new String[]{"leg1Pivot", "leg2Pivot"}) {
                    ModelPart leg = parts.get(name); if (leg != null) leg.xRot = Mth.lerp(blend, leg.xRot, .6F);
                }
                if (!parts.containsKey("leg1Pivot")) {
                    for (String name : new String[]{"leg1Top", "leg2Top"}) {
                        ModelPart leg = parts.get(name); if (leg != null) leg.xRot += .3F * blend;
                    }
                }
                wings(parts, -.08F * blend);
                if (head != null) head.xRot += (.12F + .015F * Mth.sin(t * .08F)) * blend;
                if (key.endsWith("/modelpeacock")) PeacockSleepingFan.apply(parts, blend);
            }
            default -> { }
        }
        // The original hamster face consists of independent roots, not head children.
        if (head != null && parts.containsKey("hamsterHead")) {
            org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotationXYZ(head.xRot - oldHeadX, 0, head.zRot - oldHeadZ);
            for (String name : new String[]{"hamsterNose", "hamsterEarRight", "hamsterEarLeft"}) {
                ModelPart face = parts.get(name);
                if (face == null) continue;
                org.joml.Vector3f offset = new org.joml.Vector3f(face.x - head.x, face.y - head.y, face.z - head.z).rotate(rotation);
                face.setPos(head.x + offset.x, head.y + offset.y, head.z + offset.z);
                face.xRot += head.xRot - oldHeadX; face.zRot += head.zRot - oldHeadZ;
            }
        }
    }
    static void applyNative(Entity entity, ModelPart root, float partial) {
        Map<String, ModelPart> parts = new HashMap<>();
        List<String> names = List.of("head", "body", "left_front_leg", "right_front_leg", "left_hind_leg", "right_hind_leg");
        for (String name : names) if (root.hasChild(name)) parts.put(name, root.getChild(name));
        ModelPart body = parts.get("body"), head = parts.get("head");
        if (body != null && body.hasChild("tail")) parts.put("tail", body.getChild("tail"));
        if (head != null && head.hasChild("nose")) parts.put("nose", head.getChild("nose"));
        apply(entity, "modern", parts, names, partial);
    }
    private static void lower(Map<String, ModelPart> parts, List<String> roots, float y) {
        for (String name : roots) { ModelPart part = parts.get(name); if (part != null) part.y += y; }
    }
    private static void wings(Map<String, ModelPart> parts, float angle) {
        ModelPart left = first(parts, "Wing1", "WingL"), right = first(parts, "Wing2", "WingR");
        if (left != null) left.zRot += angle;
        if (right != null) right.zRot -= angle;
    }
    private static ModelPart first(Map<String, ModelPart> parts, String... names) {
        for (String name : names) if (parts.containsKey(name)) return parts.get(name);
        return null;
    }
}
