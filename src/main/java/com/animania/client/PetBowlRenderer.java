package com.animania.client;

import com.animania.Animania;
import com.animania.catsdogs.block.entity.PetBowlBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Renders the original Java ModelPetBowl geometry instead of the placeholder block model. */
public final class PetBowlRenderer implements BlockEntityRenderer<PetBowlBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Animania.MOD_ID, "textures/entity/tileentities/pet_bowl.png");
    private final LegacyAnimalModel<Entity> model = LegacyAnimalModel.load(
            "catsdogs/client/models/blocks/modelpetbowl");

    public PetBowlRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PetBowlBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0.5F, -1.5F, -0.5F);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
