package com.animania.client;

import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.CowBreed;
import com.animania.farm.livestock.FarmAnimalRole;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Renders the three legacy red mushrooms on adult Animania mooshrooms. */
public final class CowMushroomLayer extends RenderLayer<AnimaniaCow, LegacyAnimalModel<AnimaniaCow>> {
    private final BlockRenderDispatcher blockRenderer;

    public CowMushroomLayer(RenderLayerParent<AnimaniaCow, LegacyAnimalModel<AnimaniaCow>> parent,
                            BlockRenderDispatcher blockRenderer) {
        super(parent);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AnimaniaCow cow,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (cow.breed() != CowBreed.MOOSHROOM || cow.role() == FarmAnimalRole.YOUNG) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean outline = minecraft.shouldEntityAppearGlowing(cow) && cow.isInvisible();
        if (cow.isInvisible() && !outline) return;

        BlockState state = Blocks.RED_MUSHROOM.defaultBlockState();
        BakedModel model = blockRenderer.getBlockModel(state);
        int overlay = LivingEntityRenderer.getOverlayCoords(cow, 0.0F);

        poseStack.pushPose();
        poseStack.translate(0.2F, -0.35F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-48));
        renderMushroom(poseStack, buffer, packedLight, overlay, outline, state, model);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.2F, -0.35F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(42));
        poseStack.translate(0.1F, 0, -0.6F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-48));
        renderMushroom(poseStack, buffer, packedLight, overlay, outline, state, model);
        poseStack.popPose();

        poseStack.pushPose();
        getParentModel().head().translateAndRotate(poseStack);
        poseStack.translate(0, -0.7F, -0.2F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-78));
        renderMushroom(poseStack, buffer, packedLight, overlay, outline, state, model);
        poseStack.popPose();
    }

    private void renderMushroom(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
                                boolean outline, BlockState state, BakedModel model) {
        poseStack.scale(-1, -1, 1);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        if (outline) {
            blockRenderer.getModelRenderer().renderModel(poseStack.last(),
                    buffer.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), state, model,
                    0, 0, 0, light, overlay);
        } else {
            blockRenderer.renderSingleBlock(state, poseStack, buffer, light, overlay);
        }
    }
}
