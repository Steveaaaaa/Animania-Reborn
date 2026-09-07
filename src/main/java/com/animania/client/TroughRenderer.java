package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModFluids;
import com.animania.common.world.block.TroughBlock;
import com.animania.common.world.block.TroughContent;
import com.animania.common.world.block.TroughPart;
import com.animania.common.world.block.entity.TroughBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;


/** Renders the original two-block ModelTrough geometry and its contents. */
public final class TroughRenderer implements BlockEntityRenderer<TroughBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Animania.MOD_ID, "textures/entity/tileentities/block_trough.png");
    private static final ResourceLocation WHEAT_TEXTURE = new ResourceLocation(
            Animania.MOD_ID, "textures/entity/tileentities/wheat.png");
    private final LegacyAnimalModel<Entity> model = LegacyAnimalModel.load("base/client/models/modeltrough");
    private final LegacyAnimalModel<Entity> feedBase = LegacyAnimalModel.load(
            "base/client/models/modeltrough_feed_base");
    private final LegacyAnimalModel<Entity> feedFront = LegacyAnimalModel.load(
            "base/client/models/modeltrough_feed_front");
    private final LegacyAnimalModel<Entity> feedBack = LegacyAnimalModel.load(
            "base/client/models/modeltrough_feed_back");

    public TroughRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TroughBlockEntity trough, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (trough.getBlockState().getValue(TroughBlock.PART) != TroughPart.MAIN) return;

        poseStack.pushPose();
        applyLegacyTransform(poseStack, trough.getBlockState().getValue(TroughBlock.FACING));
        VertexConsumer wood = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, wood, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();

        TroughContent content = trough.getBlockState().getValue(TroughBlock.CONTENT);
        int level = trough.getBlockState().getValue(TroughBlock.LEVEL);
        if (content == TroughContent.EMPTY || level <= 0) return;

        if (content == TroughContent.FEED && !trough.containsSlop()) {
            renderFeed(trough, poseStack, buffers, level, packedLight, packedOverlay);
            return;
        }

        TextureAtlasSprite sprite;
        int color = 0xFFFFFFFF;
        if (content == TroughContent.WATER) {
            Fluid fluid = Fluids.WATER;
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(fluid);
            sprite = net.minecraft.client.Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());
            if (trough.getLevel() != null) {
                color = extension.getTintColor(fluid.defaultFluidState(), trough.getLevel(), trough.getBlockPos());
            }
            color = 0xD0000000 | color & 0x00FFFFFF;
        } else {
            Fluid fluid = ModFluids.SLOP.source();
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(fluid);
            sprite = net.minecraft.client.Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());
            color = extension.getTintColor();
        }
        renderSurface(trough, poseStack, buffers.getBuffer(RenderType.translucent()), sprite,
                color, level, packedLight, packedOverlay);
    }

    private void renderFeed(TroughBlockEntity trough, PoseStack poseStack, MultiBufferSource buffers,
                            int level, int packedLight, int packedOverlay) {
        int visualLevel = Math.min(3, level);

        poseStack.pushPose();
        applyLegacyTransform(poseStack, trough.getBlockState().getValue(TroughBlock.FACING));
        poseStack.translate(0, 0.17F * (3 - visualLevel), 0);
        feedBase.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, packedOverlay, 0xFFA07C59);
        poseStack.popPose();

        poseStack.pushPose();
        applyLegacyTransform(poseStack, trough.getBlockState().getValue(TroughBlock.FACING));
        poseStack.translate(0, 0.2F * (3 - visualLevel), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-10));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0, 0.25F, -0.1F);
        VertexConsumer wheat = buffers.getBuffer(RenderType.entityCutoutNoCull(WHEAT_TEXTURE));
        feedFront.renderToBuffer(poseStack, wheat, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(-1.4F, -0.1F, 0);
        feedBack.renderToBuffer(poseStack, wheat, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }

    private static void applyLegacyTransform(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case NORTH -> poseStack.translate(1.5F, 1.5F, 0.5F);
            case SOUTH -> {
                poseStack.translate(-0.5F, 1.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case WEST -> {
                poseStack.translate(0.5F, 1.5F, -0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
            case EAST -> {
                poseStack.translate(0.5F, 1.5F, 1.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
            }
            default -> throw new IllegalStateException("Unexpected trough facing " + facing);
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
    }

    private static void renderSurface(TroughBlockEntity trough, PoseStack poseStack, VertexConsumer consumer,
                                      TextureAtlasSprite sprite, int color, int level,
                                      int packedLight, int packedOverlay) {
        Direction direction = TroughBlock.extensionDirection(trough.getBlockState());
        float dx = direction.getStepX();
        float dz = direction.getStepZ();
        float px = -dz;
        float pz = dx;
        float centerX = 0.5F + dx * 0.5F;
        float centerZ = 0.5F + dz * 0.5F;
        float length = 0.875F;
        float width = 0.25F;
        float y = 0.20F + Math.min(4, level) * 0.075F;

        float x1 = centerX - dx * length - px * width;
        float z1 = centerZ - dz * length - pz * width;
        float x2 = centerX - dx * length + px * width;
        float z2 = centerZ - dz * length + pz * width;
        float x3 = centerX + dx * length + px * width;
        float z3 = centerZ + dz * length + pz * width;
        float x4 = centerX + dx * length - px * width;
        float z4 = centerZ + dz * length - pz * width;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        vertex(poseStack, consumer, x1, y, z1, color, u0, v0, packedLight, packedOverlay);
        vertex(poseStack, consumer, x2, y, z2, color, u0, v1, packedLight, packedOverlay);
        vertex(poseStack, consumer, x3, y, z3, color, u1, v1, packedLight, packedOverlay);
        vertex(poseStack, consumer, x4, y, z4, color, u1, v0, packedLight, packedOverlay);
        vertex(poseStack, consumer, x4, y, z4, color, u1, v0, packedLight, packedOverlay);
        vertex(poseStack, consumer, x3, y, z3, color, u1, v1, packedLight, packedOverlay);
        vertex(poseStack, consumer, x2, y, z2, color, u0, v1, packedLight, packedOverlay);
        vertex(poseStack, consumer, x1, y, z1, color, u0, v0, packedLight, packedOverlay);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, float x, float y, float z,
                               int color, float u, float v, int light, int overlay) {
        consumer.vertex(poseStack.last().pose(), x, y, z)
                .color(color)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(poseStack.last().normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    public AABB getRenderBoundingBox(TroughBlockEntity trough) {
        BlockPos pos = trough.getBlockPos();
        Direction direction = TroughBlock.extensionDirection(trough.getBlockState());
        return new AABB(pos).expandTowards(direction.getStepX(), 0, direction.getStepZ()).inflate(0.01);
    }
}
