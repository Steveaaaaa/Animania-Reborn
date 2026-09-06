package com.animania.client;

import com.animania.Animania;
import com.animania.catsdogs.block.PetPropBlock;
import com.animania.catsdogs.block.entity.PetPropBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Renders the pet furniture models used by TileEntityPropRenderer in 1.12. */
public final class PetPropRenderer implements BlockEntityRenderer<PetPropBlockEntity> {
    private static final Map<String, CraftStudioModel> MODELS = new ConcurrentHashMap<>();
    private static final Map<String, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();

    public PetPropRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PetPropBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String name = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).getPath();
        CraftStudioModel model = MODELS.computeIfAbsent(name,
                key -> CraftStudioModel.load("catsdogs/blocks/model_" + key));
        ResourceLocation texture = TEXTURES.computeIfAbsent(name, key -> ResourceLocation.fromNamespaceAndPath(
                Animania.MOD_ID, "textures/entity/tileentities/" + key + ".png"));

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                blockEntity.getBlockState().getValue(PetPropBlock.FACING).toYRot()));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
