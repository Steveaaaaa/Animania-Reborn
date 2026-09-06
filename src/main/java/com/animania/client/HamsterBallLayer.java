package com.animania.client;

import com.animania.Animania;
import com.animania.extra.rodent.AnimaniaRodent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public final class HamsterBallLayer extends RenderLayer<AnimaniaRodent, LegacyAnimalModel<AnimaniaRodent>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID, "hamster_ball"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Animania.MOD_ID, "textures/entity/rodents/hamster_ball.png");
    private final ModelPart ball;

    public HamsterBallLayer(RenderLayerParent<AnimaniaRodent, LegacyAnimalModel<AnimaniaRodent>> parent, ModelPart root) {
        super(parent);
        ball = root.getChild("ball");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var ball = mesh.getRoot().addOrReplaceChild("ball", CubeListBuilder.create(), PartPose.ZERO);
        shell(ball, "bottom_outer", -5.001F, 4.001F, -5.002F, 10, 1, 10, 0, 17, 0);
        shell(ball, "top_outer", -5.002F, -6.002F, -5.003F, 10, 1, 10, 0, 18, 0);
        shell(ball, "left_outer", -5.003F, -5.003F, -5.004F, 1, 8, 10, 0, 18, 0);
        shell(ball, "right_outer", 4.01F, -5.004F, -5.005F, 1, 8, 10, 0, 18, 0);
        shell(ball, "front_mid", -4.02F, -5.005F, -6.006F, 8, 8, 2, 0, 18, 0);
        shell(ball, "back_inner", -3.01F, -4.002F, 6.007F, 6, 6, 1, 0, 18, 0);
        shell(ball, "right_mid", 5.01F, -5.006F, -4.003F, 1, 8, 8, 0, 18, 0);
        shell(ball, "left_mid", -6.01F, -5.007F, -4.005F, 1, 8, 8, 0, 18, 0);
        shell(ball, "top_mid", -4.01F, -7.004F, -4.009F, 8, 1, 8, 0, 18.008F, 0);
        shell(ball, "bottom_mid", -4.01F, 5.009F, -4.010F, 8, 1, 8, 0, 17, 0);
        shell(ball, "front_inner", -3.01F, -4.01F, -7.011F, 6, 6, 1, 0, 18, 0);
        shell(ball, "left_inner", -7.01F, -4.002F, -3.012F, 1, 6, 6, 0, 18, 0);
        shell(ball, "back_mid", -4.01F, -5.011F, 4.013F, 8, 8, 2, 0, 18, 0);
        shell(ball, "right_inner", 6.01F, -4.012F, -3.014F, 1, 6, 6, 0, 18, 0);
        shell(ball, "top_inner", -3.02F, -8.002F, -3.015F, 6, 1, 6, 0, 18, 0);
        shell(ball, "bottom_inner", -3.03F, 6.002F, -3.016F, 6, 1, 6, 0, 17, 0);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void shell(net.minecraft.client.model.geom.builders.PartDefinition parent, String name,
                              float x, float y, float z, float width, float height, float depth,
                              float pivotX, float pivotY, float pivotZ) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(x, y, z, width, height, depth), PartPose.offset(pivotX, pivotY, pivotZ));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AnimaniaRodent rodent,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!rodent.isInBall()) return;
        int color = rodent.ballColor() < 0 ? 0x66FFFFFF
                : (0x88000000 | (DyeColor.byId(rodent.ballColor()).getTextureDiffuseColor() & 0xFFFFFF));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees((int) limbSwing * 20.0F));
        poseStack.translate(-0.1D, -1.9D, 0.0D);
        poseStack.scale(1.7F, 1.7F, 1.7F);
        ball.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
    }
}
