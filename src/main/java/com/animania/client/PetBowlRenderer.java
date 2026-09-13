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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Animania.MOD_ID, "textures/entity/tileentities/pet_bowl.png");
    private static final ResourceLocation WATER_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
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
        if (blockEntity.food().isEmpty() && blockEntity.water() <= 0) return;
        net.minecraft.client.renderer.texture.TextureAtlasSprite sprite;
        int color = 0xFFFFFFFF;
        boolean water = blockEntity.water() > 0;
        if (water) {
            sprite = net.minecraft.client.Minecraft.getInstance().getTextureAtlas(
                    net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(WATER_TEXTURE);
            if (blockEntity.getLevel() != null) color = 0xD0000000 | net.minecraft.client.renderer.BiomeColors
                    .getAverageWaterColor(blockEntity.getLevel(), blockEntity.getBlockPos());
        } else {
            sprite = net.minecraft.client.Minecraft.getInstance().getItemRenderer()
                    .getModel(blockEntity.food(), blockEntity.getLevel(), null, 0).getParticleIcon();
        }
        VertexConsumer surface = buffers.getBuffer(water ? RenderType.translucent() : RenderType.cutout());
        float a = 5F / 16F, b = 11F / 16F, y = 3.01F / 16F;
        TroughRenderer.vertex(poseStack, surface, a, y, a, color, sprite.getU0(), sprite.getV0(), packedLight, packedOverlay);
        TroughRenderer.vertex(poseStack, surface, a, y, b, color, sprite.getU0(), sprite.getV1(), packedLight, packedOverlay);
        TroughRenderer.vertex(poseStack, surface, b, y, b, color, sprite.getU1(), sprite.getV1(), packedLight, packedOverlay);
        TroughRenderer.vertex(poseStack, surface, b, y, a, color, sprite.getU1(), sprite.getV0(), packedLight, packedOverlay);
    }
}
