package com.animania.client;

import com.animania.Animania;
import com.animania.extra.world.block.HamsterWheelBlock;
import com.animania.extra.world.block.entity.HamsterWheelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Renders the CraftStudio wheel and hamster with their original animations. */
public final class HamsterWheelRenderer implements BlockEntityRenderer<HamsterWheelBlockEntity> {
    private static final ResourceLocation WHEEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Animania.MOD_ID, "textures/entity/tileentities/hamster_wheel.png");
    private static final ResourceLocation HAMSTER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Animania.MOD_ID, "textures/entity/rodents/hamster_tarou.png");

    private final CraftStudioModel wheel = CraftStudioModel.load("extra/blocks/model_hamster_wheel");
    private final CraftStudioModel hamster = CraftStudioModel.load("extra/entity/hamster");
    private final CraftStudioAnimation wheelAnimation = CraftStudioAnimation.load("extra/blocks/anim_hamster_wheel");
    private final CraftStudioAnimation hamsterAnimation = CraftStudioAnimation.load("extra/entity/hamster_run");

    public HamsterWheelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HamsterWheelBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        boolean running = blockEntity.getBlockState().getValue(HamsterWheelBlock.RUNNING);
        float frame = ((blockEntity.getLevel() == null ? 0 : blockEntity.getLevel().getGameTime())
                + partialTick) * 3.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                blockEntity.getBlockState().getValue(HamsterWheelBlock.FACING).toYRot()));

        VertexConsumer wheelConsumer = buffers.getBuffer(RenderType.entityTranslucent(WHEEL_TEXTURE));
        if (running) {
            wheel.renderAnimated(poseStack, wheelConsumer, packedLight, packedOverlay, 0xFFFFFFFF,
                    wheelAnimation, frame, false);
        } else {
            wheel.render(poseStack, wheelConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
        }

        if (running) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0, 0.9F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            VertexConsumer hamsterConsumer = buffers.getBuffer(RenderType.entityCutoutNoCull(HAMSTER_TEXTURE));
            hamster.renderAnimated(poseStack, hamsterConsumer, packedLight, packedOverlay, 0xFFFFFFFF,
                    hamsterAnimation, frame, false);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
