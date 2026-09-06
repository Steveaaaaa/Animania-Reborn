package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.rodent.AnimaniaRodent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaRodentRenderer extends MobRenderer<AnimaniaRodent, LegacyAnimalModel<AnimaniaRodent>> {
    private final LegacyAnimalModel<AnimaniaRodent> hamster;
    private final LegacyAnimalModel<AnimaniaRodent> hedgehog;
    private final LegacyAnimalModel<AnimaniaRodent> ferret;

    public AnimaniaRodentRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("extra/client/model/rodents/modelhamster"), 0.25F);
        hamster = model;
        hedgehog = LegacyAnimalModel.load("extra/client/model/rodents/modelhedgehog");
        ferret = LegacyAnimalModel.load("extra/client/model/rodents/modelferret");
        addLayer(new HamsterBallLayer(this, context.bakeLayer(HamsterBallLayer.LAYER)));
        addLayer(new LegacyBlinkLayer<>(this, rodent -> "rodents/" +
                (rodent.kind() == AnimaniaRodent.Kind.HAMSTER ? "hamster_blink"
                        : rodent.kind().isFerret() ? "ferret_blink" : "hedgehog_blink"),
                AnimaniaRodentRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaRodent rodent, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        model = rodent.kind().isHedgehog() ? hedgehog : rodent.kind().isFerret() ? ferret : hamster;
        super.render(rodent, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaRodent rodent, PoseStack poseStack, float partialTick) {
        float scale = rodent.kind() == AnimaniaRodent.Kind.HAMSTER ? 0.40F
                : rodent.kind().isFerret() ? 0.50F : 0.60F;
        poseStack.scale(scale, scale, scale);
        if (rodent.getData(ModAttachments.SLEEPING) || rodent.kind().isHedgehog() && rodent.isInSittingPose()) {
            double y = rodent.kind().isFerret() ? 0.20D : 0.15D;
            poseStack.translate(0.0D, y, 0.0D);
            if (rodent.kind() == AnimaniaRodent.Kind.HAMSTER) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
            } else if (rodent.kind().isFerret()) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(rodent.kind() == AnimaniaRodent.Kind.FERRET_WHITE ? -10.0F : 10.0F));
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaRodent rodent) {
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/rodents/" + (rodent.kind() == AnimaniaRodent.Kind.HAMSTER
                        ? "hamster_" : "") + rodent.textureName() + ".png");
    }

    private static int eyelidColor(AnimaniaRodent rodent) {
        if (rodent.kind() == AnimaniaRodent.Kind.HAMSTER) return 0x202020;
        if (rodent.kind().isFerret()) return rodent.kind() == AnimaniaRodent.Kind.FERRET_WHITE ? 0xC9C8B7 : 0x58372D;
        return rodent.kind() == AnimaniaRodent.Kind.HEDGEHOG_ALBINO ? 0xF6F0C7 : 0xD3CDAB;
    }
}
