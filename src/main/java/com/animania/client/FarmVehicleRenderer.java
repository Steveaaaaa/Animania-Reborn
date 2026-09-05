package com.animania.client;

import com.animania.Animania;
import com.animania.farm.vehicle.FarmVehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.world.entity.LivingEntity;

public final class FarmVehicleRenderer extends EntityRenderer<FarmVehicleEntity> {
    private final CraftStudioModel cart;
    private final CraftStudioModel cartChest;
    private final CraftStudioModel wagon;
    private final CraftStudioModel tiller;
    private final CraftStudioAnimation cartAnimation;
    private final CraftStudioAnimation cartChestAnimation;
    private final CraftStudioAnimation wagonAnimation;
    private final CraftStudioAnimation tillerAnimation;

    public FarmVehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        cart = CraftStudioModel.load("farm/entity/model_cart");
        cartChest = CraftStudioModel.load("farm/entity/model_cart_chest");
        wagon = CraftStudioModel.load("farm/entity/model_wagon");
        tiller = CraftStudioModel.load("farm/entity/model_tiller");
        cartAnimation = CraftStudioAnimation.load("farm/entity/anim_cart");
        cartChestAnimation = CraftStudioAnimation.load("farm/entity/anim_cart_chest");
        wagonAnimation = CraftStudioAnimation.load("farm/entity/anim_wagon");
        tillerAnimation = CraftStudioAnimation.load("farm/entity/anim_tiller");
        shadowRadius = 0.9F;
    }

    @Override
    public void render(FarmVehicleEntity vehicle, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        Entity puller = vehicle.pullerForRendering();
        applyVehicleTransform(vehicle, entityYaw, partialTick, puller, poseStack);
        CraftStudioModel model = switch (vehicle.kind()) {
            case CART -> vehicle.hasChest() ? cartChest : cart;
            case WAGON -> wagon;
            case TILLER -> tiller;
        };
        CraftStudioAnimation animation = switch (vehicle.kind()) {
            case CART -> vehicle.hasChest() ? cartChestAnimation : cartAnimation;
            case WAGON -> wagonAnimation;
            case TILLER -> tillerAnimation;
        };
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(vehicle)));
        double dx = vehicle.getX() - vehicle.xOld;
        double dz = vehicle.getZ() - vehicle.zOld;
        if (dx * dx + dz * dz > 1.0E-6D) {
            double yaw = Math.toRadians(vehicle.getYRot());
            boolean reverse = dx * Math.sin(yaw) - dz * Math.cos(yaw) < 0;
            model.renderAnimated(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF,
                    animation, (vehicle.tickCount + partialTick) * 3.0F, reverse);
        } else {
            model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
        poseStack.popPose();
        if (puller != null) renderHarness(vehicle, puller, entityYaw, partialTick, poseStack, buffers, packedLight);
        super.render(vehicle, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void applyVehicleTransform(FarmVehicleEntity vehicle, float entityYaw, float partialTick,
                                              Entity puller, PoseStack poseStack) {
        poseStack.translate(0.0F, vehicle.kind() == FarmVehicleEntity.Kind.WAGON ? 1.6F : 1.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        if (puller != null) {
            double vehicleY = Mth.lerp(partialTick, vehicle.yOld, vehicle.getY());
            double pullerY = Mth.lerp(partialTick, puller.yOld, puller.getY());
            float pitchFactor = vehicle.kind() == FarmVehicleEntity.Kind.WAGON ? 15.0F : 25.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    Mth.clamp((float) (pullerY - vehicleY) * pitchFactor, -35.0F, 35.0F)));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
    }

    /**
     * The 1.12 models contain the wooden shafts but no separate horse harness.
     * Two short leather traces join their tips to the animal's chest so the
     * visual connection remains explicit without replacing the original model.
     */
    private static void renderHarness(FarmVehicleEntity vehicle, Entity puller, float entityYaw, float partialTick,
                                      PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        if (vehicle.kind() == FarmVehicleEntity.Kind.WAGON) {
            renderWagonHarness(vehicle, puller, entityYaw, partialTick, poseStack, buffers, packedLight);
            return;
        }
        Vec3 vehiclePos = interpolatedPosition(vehicle, partialTick);
        Vec3 pullerPos = interpolatedPosition(puller, partialTick);
        Vec3 forward = new Vec3(pullerPos.x - vehiclePos.x, 0.0D, pullerPos.z - vehiclePos.z);
        if (forward.lengthSqr() < 1.0E-6D) return;
        forward = forward.normalize();
        Vec3 side = new Vec3(forward.z, 0.0D, -forward.x);

        double sideOffset = Math.min(0.58D, puller.getBbWidth() * 0.40D);
        double shaftForward = vehicle.kind().centerDistance() - 0.55D;
        double shaftHeight = vehicle.kind() == FarmVehicleEntity.Kind.WAGON ? 0.82D : 0.88D;
        double chestHeight = Math.max(0.55D, puller.getBbHeight() * 0.56D);
        Vec3 shaftCenter = vehiclePos.add(forward.scale(shaftForward)).add(0.0D, shaftHeight, 0.0D);
        Vec3 chestCenter = pullerPos.add(forward.scale(0.18D)).add(0.0D, chestHeight, 0.0D);

        VertexConsumer leather = buffers.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();
        for (double sign : new double[]{-1.0D, 1.0D}) {
            Vec3 start = shaftCenter.add(side.scale(sideOffset * sign)).subtract(vehiclePos);
            Vec3 end = chestCenter.add(side.scale(sideOffset * sign)).subtract(vehiclePos);
            renderLeatherTrace(leather, matrix, start, end, packedLight);
        }
    }

    private static void renderWagonHarness(FarmVehicleEntity vehicle, Entity puller, float yaw, float partialTick,
                                           PoseStack poseStack, MultiBufferSource buffers, int light) {
        PoseStack modelSpace = new PoseStack();
        applyVehicleTransform(vehicle, yaw, partialTick, puller, modelSpace);
        Vec3 animalPos = interpolatedPosition(puller, partialTick).subtract(interpolatedPosition(vehicle, partialTick));
        float bodyYaw = puller instanceof LivingEntity living
                ? Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot)
                : Mth.rotLerp(partialTick, puller.yRotO, puller.getYRot());
        Vec3 forward = new Vec3(-Math.sin(bodyYaw * Mth.DEG_TO_RAD), 0, Math.cos(bodyYaw * Mth.DEG_TO_RAD));
        Vec3 side = new Vec3(forward.z, 0, -forward.x);
        double width = puller.getBbWidth() * 0.40D;
        double height = puller.getBbHeight() * 0.56D;
        if (puller instanceof com.animania.farm.livestock.AnimaniaHorse horse) {
            double scale = horse.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.85D : 0.72D;
            // Original Body cube: x = +/-7, y = -10..4 after its pivot.
            width = 7.0D / 16.0D * scale + 0.035D;
            height = (1.501D + 3.0D / 16.0D) * scale;
        }
        Vec3 chest = animalPos.add(forward.scale(0.45D)).add(0, height, 0);
        Vec3 left = chest.add(side.scale(width));
        Vec3 right = chest.subtract(side.scale(width));
        VertexConsumer leather = buffers.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();
        for (int sign : new int[]{-1, 1}) {
            // Tow6 crossbar endpoints, composed from the unchanged source hierarchy.
            Vector3f tip = modelSpace.last().pose().transformPosition(
                    new Vector3f(sign * 0.780469F, 0.649259F, -4.172812F));
            renderLeatherTrace(leather, matrix, new Vec3(tip.x, tip.y, tip.z),
                    sign > 0 ? left : right, light);
        }
        // A visible breast strap joins both traces on the front of the horse.
        Vec3 frontLeft = left.add(forward.scale(0.35D));
        Vec3 frontRight = right.add(forward.scale(0.35D));
        renderLeatherTrace(leather, matrix, left, frontLeft, light);
        renderLeatherTrace(leather, matrix, frontLeft, frontRight, light);
        renderLeatherTrace(leather, matrix, frontRight, right, light);
    }

    private static Vec3 interpolatedPosition(Entity entity, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
    }

    private static void renderLeatherTrace(VertexConsumer consumer, Matrix4f matrix, Vec3 start, Vec3 end,
                                           int packedLight) {
        Vec3 delta = end.subtract(start);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (delta.lengthSqr() < 1.0E-6D || horizontal < 1.0E-6D) return;
        float edgeX = (float) (delta.z / horizontal * 0.0125D);
        float edgeZ = (float) (-delta.x / horizontal * 0.0125D);
        for (int i = 0; i <= 16; i++) {
            float t = i / 16.0F;
            float x = (float) Mth.lerp(t, start.x, end.x);
            float y = (float) Mth.lerp(t, start.y, end.y);
            float z = (float) Mth.lerp(t, start.z, end.z);
            float shade = (i & 1) == 0 ? 0.78F : 1.0F;
            consumer.addVertex(matrix, x - edgeX, y, z - edgeZ)
                    .setColor(0.30F * shade, 0.16F * shade, 0.07F * shade, 1.0F).setLight(packedLight);
            consumer.addVertex(matrix, x + edgeX, y + 0.025F, z + edgeZ)
                    .setColor(0.30F * shade, 0.16F * shade, 0.07F * shade, 1.0F).setLight(packedLight);
        }
        for (int i = 16; i >= 0; i--) {
            float t = i / 16.0F;
            float x = (float) Mth.lerp(t, start.x, end.x);
            float y = (float) Mth.lerp(t, start.y, end.y);
            float z = (float) Mth.lerp(t, start.z, end.z);
            float shade = (i & 1) == 0 ? 1.0F : 0.78F;
            consumer.addVertex(matrix, x - edgeX, y + 0.025F, z - edgeZ)
                    .setColor(0.30F * shade, 0.16F * shade, 0.07F * shade, 1.0F).setLight(packedLight);
            consumer.addVertex(matrix, x + edgeX, y, z + edgeZ)
                    .setColor(0.30F * shade, 0.16F * shade, 0.07F * shade, 1.0F).setLight(packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FarmVehicleEntity vehicle) {
        String texture = vehicle.kind() == FarmVehicleEntity.Kind.CART && vehicle.hasChest()
                ? "cart_chest" : vehicle.kind().id();
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/props/" + texture + ".png");
    }
}
