package com.animania.client;

import com.animania.Animania;
import com.animania.farm.world.block.HiveBlock;
import com.animania.farm.world.block.entity.HiveBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Exact CraftStudio hive geometry and continuously looping bee animations. */
public final class HiveRenderer implements BlockEntityRenderer<HiveBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Animania.MOD_ID, "textures/entity/props/bee_hive.png");
    private final CraftStudioModel beeHive = CraftStudioModel.load("farm/blocks/model_bee_hive");
    private final CraftStudioModel wildHive = CraftStudioModel.load("farm/blocks/model_wild_hive");
    private final CraftStudioAnimation bees = CraftStudioAnimation.load("farm/blocks/anim_bees");
    private final CraftStudioAnimation wildBees = CraftStudioAnimation.load("farm/blocks/anim_bees_wild");

    public HiveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HiveBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        HiveBlock block = (HiveBlock) blockEntity.getBlockState().getBlock();
        boolean wild = block.isWild();
        Direction facing = blockEntity.getBlockState().getValue(HiveBlock.FACING);
        float frame = ((blockEntity.getLevel() == null ? 0 : blockEntity.getLevel().getGameTime())
                + partialTick) * 3.0F;

        poseStack.pushPose();
        if (wild) {
            float x = facing == Direction.EAST ? 0.25F : facing == Direction.WEST ? 0.75F : 0.5F;
            float z = facing == Direction.NORTH ? 0.75F : facing == Direction.SOUTH ? 0.25F : 0.5F;
            poseStack.translate(x, 1.0F, z);
        } else {
            poseStack.translate(0.5F, 1.5F, 0.5F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        if (wild) {
            wildHive.renderAnimated(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF,
                    wildBees, frame, false);
        } else {
            beeHive.renderAnimated(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF,
                    bees, frame, false);
        }
        poseStack.popPose();
    }
}
