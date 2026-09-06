package com.animania.client;

import com.animania.Animania;
import com.animania.farm.livestock.AnimaniaCow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaCowRenderer extends MobRenderer<AnimaniaCow, LegacyAnimalModel<AnimaniaCow>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaCow>> models = new java.util.HashMap<>();

    public AnimaniaCowRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("farm/client/model/cow/modelcow"), 0.7F);
        models.put("modelcow", model);
        addLayer(new CowMushroomLayer(this, context.getBlockRenderDispatcher()));
        addLayer(new LegacyBlinkLayer<>(this, cow -> "cows/" + switch (cow.role()) {
            case YOUNG -> "calf_blink"; case FEMALE -> "cow_blink"; case MALE -> "bull_blink";
        }, AnimaniaCowRenderer::leftEyelidColor, AnimaniaCowRenderer::rightEyelidColor, false));
    }

    @Override
    public void render(AnimaniaCow cow, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        String role = switch (cow.role()) { case YOUNG -> "calf"; case FEMALE -> "cow"; case MALE -> "bull"; };
        String variant = switch (cow.breed()) {
            case ANGUS -> "angus";
            case HEREFORD, JERSEY -> "hereford";
            case LONGHORN, HIGHLAND -> "longhorn";
            default -> "";
        };
        if (cow.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG && variant.equals("hereford")) variant = "";
        String name = "model" + role + variant;
        model = models.computeIfAbsent(name,
                key -> LegacyAnimalModel.load("farm/client/model/cow/" + key));
        shadowRadius = cow.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? 0.45F : 0.7F;
        super.render(cow, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(AnimaniaCow cow, PoseStack poseStack, float partialTick) {
        float scale;
        if (cow.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG) {
            scale = 1.0F;
        } else {
            scale = switch (cow.breed()) {
                case FRIESIAN, HOLSTEIN -> cow.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.30F : 1.24F;
                case LONGHORN, HIGHLAND -> cow.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.50F : 1.44F;
                case ANGUS, HEREFORD, JERSEY -> cow.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.40F : 1.34F;
                case MOOSHROOM -> cow.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 1.30F : 1.34F;
            };
        }
        poseStack.scale(scale, scale, scale);
        LegacySleepAnimation.transform(cow, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaCow cow) {
        String role = switch (cow.role()) {
            case YOUNG -> "calf";
            case FEMALE -> "cow";
            case MALE -> "bull";
        };
        if ((cow.breed() == com.animania.farm.livestock.CowBreed.FRIESIAN
                || cow.breed() == com.animania.farm.livestock.CowBreed.HOLSTEIN)
                && cow.hasCustomName() && cow.getName().getString().trim().equalsIgnoreCase("purp")) {
            return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                    "textures/entity/cows/" + role + "_purplicious.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/cows/" + role + "_" + cow.breed().getSerializedName() + ".png");
    }

    private static int leftEyelidColor(AnimaniaCow cow) {
        return switch (cow.breed()) {
            case ANGUS -> 0x333333; case FRIESIAN -> 0x463930; case HEREFORD, LONGHORN -> 0xDEDEDE;
            case HOLSTEIN -> 0x1C242B; case HIGHLAND -> cow.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG
                    ? 0x5B2F1B : 0x130D0A;
            case JERSEY -> cow.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? 0x7C632D : 0x3B2603;
            case MOOSHROOM -> 0xAB0F0F;
        };
    }

    private static int rightEyelidColor(AnimaniaCow cow) {
        return cow.breed() == com.animania.farm.livestock.CowBreed.FRIESIAN
                || cow.breed() == com.animania.farm.livestock.CowBreed.HOLSTEIN
                ? 0xDEDEDE : leftEyelidColor(cow);
    }
}
