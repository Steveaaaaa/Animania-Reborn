package com.animania.client;

import com.animania.farm.world.block.NestBlock;
import com.animania.farm.world.block.NestBreed;
import com.animania.farm.world.block.entity.NestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class NestRenderer implements BlockEntityRenderer<NestBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("animania", "textures/entity/tileentities/block_nest_white.png");
    public NestRenderer(BlockEntityRendererProvider.Context context) {}
    @Override
    public void render(NestBlockEntity nest, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        int count = nest.getBlockState().getValue(NestBlock.EGGS);
        if (count == 0) return;
        NestBreed breed = nest.getBlockState().getValue(NestBlock.BREED);
        String color = breed == NestBreed.PEACOCK_WHITE ? "peacock_white"
                : breed.peafowlBreed() != null ? "blue" : breed.chickenBreed() == null ? "white" : breed.chickenBreed().eggColor();
        boolean blueChicken = breed.chickenBreed() == com.animania.farm.chicken.ChickenBreed.COLD;
        var texture = blueChicken ? ResourceLocation.tryParse("animania:textures/entity/tileentities/block_nest_chicken_blue.png") : TEXTURE;
        LegacyAnimalModel<Entity> eggs = LegacyAnimalModel.load("nest_" + (blueChicken ? "white" : color) + "_" + count);
        poses.pushPose();
        poses.translate(0.5D, 1.5D, 0.5D);
        poses.scale(-1, -1, 1);
        eggs.renderToBuffer(poses, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, overlay, 0xFFFFFFFF);
        poses.popPose();
    }
}
