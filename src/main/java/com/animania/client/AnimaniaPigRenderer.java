package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaPig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaPigRenderer extends MobRenderer<AnimaniaPig, LegacyAnimalModel<AnimaniaPig>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaPig>> models = new java.util.HashMap<>();

    public AnimaniaPigRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("farm/client/model/pig/modelsow"), 0.6F);
        models.put("modelsow", model);
        addLayer(new PigMudLayer(this));
        addLayer(new LegacyBlinkLayer<>(this, pig -> "pigs/" +
                (pig.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? "piglet_blink"
                        : pig.breed() == com.animania.farm.livestock.PigBreed.HAMPSHIRE ? "hampshire_blink" : "pig_blink"),
                AnimaniaPigRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaPig pig, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light) {
        String role = switch (pig.role()) { case YOUNG -> "piglet"; case FEMALE -> "sow"; case MALE -> "hog"; };
        String variant = pig.breed() == com.animania.farm.livestock.PigBreed.HAMPSHIRE ? "hampshire"
                : pig.role() != com.animania.farm.livestock.FarmAnimalRole.YOUNG
                && pig.breed() == com.animania.farm.livestock.PigBreed.LARGE_BLACK ? "largeblack" : "";
        String name = "model" + role + variant;
        model = models.computeIfAbsent(name,
                key -> LegacyAnimalModel.load("farm/client/model/pig/" + key));
        super.render(pig, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaPig pig, PoseStack poseStack, float partialTick) {
        float scale = switch (pig.breed()) {
            case DUROC -> pig.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.18F : 1.06F;
            case HAMPSHIRE -> pig.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.12F : 1.02F;
            case LARGE_BLACK -> pig.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.20F : 1.14F;
            case LARGE_WHITE -> pig.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.16F : 1.08F;
            case OLD_SPOT -> pig.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.19F : 1.12F;
            case YORKSHIRE -> pig.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.10F : 1.0F;
        };
        if (pig.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG) {
            scale = switch (pig.breed()) {
                case DUROC, YORKSHIRE -> 1.0F;
                case HAMPSHIRE, LARGE_WHITE -> 1.12F;
                case LARGE_BLACK -> 1.20F;
                case OLD_SPOT -> 1.10F;
            };
        }
        poseStack.scale(scale, scale, scale);
        if (pig.getData(ModAttachments.SLEEPING)) {
            double y = pig.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG
                    ? pig.getBbHeight() - 0.70D : pig.getBbHeight() - 1.25D;
            poseStack.translate(0.0D, y, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(86.0F));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaPig pig) {
        String role = switch (pig.role()) {
            case YOUNG -> "piglet";
            case FEMALE -> "sow";
            case MALE -> "hog";
        };
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/pigs/" + role + "_" + pig.breed().getSerializedName() + ".png");
    }

    private static int eyelidColor(AnimaniaPig pig) {
        return switch (pig.breed()) {
            case DUROC -> 0x421006; case HAMPSHIRE, LARGE_BLACK -> 0x3A3333;
            case LARGE_WHITE -> 0xC4A8A8; case OLD_SPOT -> 0x514B4B; case YORKSHIRE -> 0xE07F7D;
        };
    }
}
