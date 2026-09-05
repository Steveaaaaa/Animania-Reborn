package com.animania.client;

import com.animania.Animania;
import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.cat.CatBreed;
import com.animania.common.registry.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Replays the original two-mask, breed-tinted cat eyelid layer. */
public final class CatBlinkLayer extends RenderLayer<AnimaniaCat, LegacyAnimalModel<AnimaniaCat>> {
    public CatBlinkLayer(RenderLayerParent<AnimaniaCat, LegacyAnimalModel<AnimaniaCat>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AnimaniaCat cat,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!cat.getData(ModAttachments.SLEEPING) && !cat.isBlinking()) return;
        String mask = cat.breed() == CatBreed.RAGDOLL || cat.breed() == CatBreed.NORWEGIAN
                ? "blink_2" : "blink_1";
        int color = 0xFF000000 | eyelidColor(cat.breed());
        renderMask(poseStack, buffers, packedLight, mask + "_left.png", color);
        renderMask(poseStack, buffers, packedLight, mask + "_right.png", color);
    }

    private void renderMask(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                            String file, int color) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/cats/" + file);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }

    private static int eyelidColor(CatBreed breed) {
        return switch (breed) {
            case RAGDOLL -> 0x83786D;
            case AMERICAN_SHORTHAIR -> 0x7D7D7D;
            case ASIATIC -> 0x836951;
            case EXOTIC -> 0xA75823;
            case NORWEGIAN -> 0x4E3C30;
            case OCELOT -> 0xA47947;
            case SIAMESE -> 0x271D1B;
            case TABBY -> 0x594336;
        };
    }
}
