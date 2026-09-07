package com.animania.client;

import com.animania.Animania;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.FarmAnimalRole;
import com.animania.farm.livestock.PigBreed;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class PigMudLayer extends RenderLayer<AnimaniaPig, LegacyAnimalModel<AnimaniaPig>> {
    public PigMudLayer(RenderLayerParent<AnimaniaPig, LegacyAnimalModel<AnimaniaPig>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AnimaniaPig pig,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (pig.mudAmount() <= 0.0F || pig.isMuddy() && pig.splashTimer() > 0) return;
        String texture = pig.role() == FarmAnimalRole.YOUNG ? "piglet_muddy"
                : pig.breed() == PigBreed.HAMPSHIRE ? "pig_muddy_hampshire" : "pig_muddy";
        ResourceLocation location = new ResourceLocation(Animania.MOD_ID,
                "textures/entity/pigs/" + texture + ".png");
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(location));
        int alpha = (int) (255 * pig.mudAmount());
        poseStack.pushPose();
        poseStack.scale(1.01F, 1.01F, 1.01F);
        getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, alpha << 24 | 0xFFFFFF);
        poseStack.popPose();
    }
}
