package com.animania.client;

import com.animania.Animania;
import com.animania.farm.livestock.AnimaniaGoat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AnimaniaGoatRenderer extends MobRenderer<AnimaniaGoat, LegacyAnimalModel<AnimaniaGoat>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaGoat>> models = new java.util.HashMap<>();

    public AnimaniaGoatRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("farm/client/model/goats/modeldoealpine"), 0.7F);
        models.put("modeldoealpine", model);
        addLayer(new LegacyBlinkLayer<>(this, goat -> "goats/" +
                (goat.breed() == com.animania.farm.livestock.GoatBreed.ANGORA ? "angora_blink" : "goats_blink"),
                AnimaniaGoatRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaGoat goat, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        String role = switch (goat.role()) { case YOUNG -> "kid"; case FEMALE -> "doe"; case MALE -> "buck"; };
        String breed = goat.breed().getSerializedName().replace("_", "");
        String name = "model" + role + breed;
        model = models.computeIfAbsent(name,
                key -> LegacyAnimalModel.load("farm/client/model/goats/" + key));
        super.render(goat, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(AnimaniaGoat goat, PoseStack poseStack, float partialTick) {
        float adult = switch (goat.breed()) {
            case ALPINE -> goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.67F : 0.60F;
            case ANGORA -> goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.64F : 0.58F;
            case FAINTING -> goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.42F : 0.40F;
            case KIKO -> goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.45F : 0.42F;
            case KINDER -> goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.52F : 0.48F;
            case NIGERIAN_DWARF, PYGMY -> goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE ? 0.45F : 0.42F;
        };
        float scale = adult;
        if (goat.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG) {
            float growth = Mth.clamp(1.0F + goat.getAge() / 24000.0F, 0, 1);
            float birth = switch (goat.breed()) {
                case ALPINE -> 0.33F;
                case ANGORA -> 0.30F;
                case FAINTING -> 0.21F;
                case KIKO -> 0.25F;
                case KINDER -> 0.27F;
                case NIGERIAN_DWARF, PYGMY -> 0.24F;
            };
            scale = birth + growth * (adult - birth);
        }
        poseStack.scale(scale, scale, scale);
        LegacySleepAnimation.transform(goat, poseStack, partialTick);
        if (!com.animania.common.registry.ModAttachments.getData(goat, com.animania.common.registry.ModAttachments.SLEEPING)
                && goat.isSpooked() && goat.spookedTimer() < 0.94F && goat.spookedTimer() > 0.06F) {
            poseStack.translate(0.0D, goat.getBbHeight() - 1.5D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(86.0F));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaGoat goat) {
        String role = switch (goat.role()) {
            case YOUNG -> "kid";
            case FEMALE -> "doe";
            case MALE -> "buck";
        };
        String suffix = goat.isAngoraSheared() && role.equals("buck") ? "angora_sheared" : goat.breed().texture();
        return new ResourceLocation(Animania.MOD_ID,
                "textures/entity/goats/" + role + "_" + suffix + ".png");
    }

    private static int eyelidColor(AnimaniaGoat goat) {
        return switch (goat.breed()) {
            case ALPINE -> 0x83786D; case ANGORA -> 0xCAC4B7; case FAINTING -> 0x6B6968;
            case KIKO -> 0x694330; case KINDER -> 0x6F4935; case NIGERIAN_DWARF -> 0x404040;
            case PYGMY -> 0x2B2E2E;
        };
    }
}
