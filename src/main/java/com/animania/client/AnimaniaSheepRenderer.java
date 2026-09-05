package com.animania.client;

import com.animania.Animania;
import com.animania.farm.livestock.AnimaniaSheep;
import com.animania.farm.livestock.FarmAnimalRole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public final class AnimaniaSheepRenderer extends MobRenderer<AnimaniaSheep, LegacyAnimalModel<AnimaniaSheep>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaSheep>> models = new java.util.HashMap<>();
    public AnimaniaSheepRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("farm/client/model/sheep/modeldorpersheep"), 0.7F);
        models.put("modeldorpersheep", model);
        addLayer(new LegacyBlinkLayer<>(this, sheep -> "sheep/sheep_blink", AnimaniaSheepRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaSheep sheep, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        String name = switch (sheep.breed()) {
            case DORPER -> "modeldorpersheep";
            case DORSET -> sheep.role() == FarmAnimalRole.MALE ? "modeldorsetram" : "modeldorsetewe";
            case FRIESIAN -> "modelfriesiansheep";
            case JACOB -> "modeljacobsheep";
            case MERINO -> sheep.role() == FarmAnimalRole.MALE ? "modelmerinoram" : "modelmerinoewe";
            case SUFFOLK -> sheep.role() == FarmAnimalRole.MALE ? "modelsuffolkram" : "modelsuffolkewe";
        };
        model = models.computeIfAbsent(name,
                key -> LegacyAnimalModel.load("farm/client/model/sheep/" + key));
        super.render(sheep, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaSheep sheep, PoseStack poseStack, float partialTick) {
        float scale = switch (sheep.role()) {
            case FEMALE -> switch (sheep.breed()) {
                case DORPER -> 0.60F; case DORSET -> 0.58F; case FRIESIAN -> 0.61F;
                case JACOB -> 0.48F; case MERINO -> 0.53F; case SUFFOLK -> 0.64F;
            };
            case MALE -> switch (sheep.breed()) {
                case DORPER -> 0.68F; case DORSET -> 0.62F; case FRIESIAN -> 0.65F;
                case JACOB -> 0.52F; case MERINO -> 0.56F; case SUFFOLK -> 0.68F;
            };
            case YOUNG -> switch (sheep.breed()) {
                case DORPER, DORSET -> 0.30F; case FRIESIAN -> 0.33F;
                case JACOB -> 0.22F; case MERINO -> 0.24F; case SUFFOLK -> 0.32F;
            };
        };
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaSheep sheep) {
        String color = sheep.getColor() == DyeColor.BLACK ? "black"
                : sheep.getColor() == DyeColor.BROWN ? "brown" : "white";
        String texture = switch (sheep.breed()) {
            case DORPER -> "sheep_dorper";
            case DORSET -> "sheep_dorset_" + color + "_" + (sheep.role() == FarmAnimalRole.MALE ? "ram" : "ewe");
            case FRIESIAN -> "sheep_friesian_" + color + (sheep.role() == FarmAnimalRole.MALE ? "_ram" : "");
            case JACOB -> sheep.role() == FarmAnimalRole.YOUNG ? "sheep_jacob_lamb" : "sheep_jacob";
            case MERINO -> "sheep_merino_" + color + "_" + (sheep.role() == FarmAnimalRole.MALE ? "ram" : "ewe");
            case SUFFOLK -> "sheep_suffolk_" + color + "_" + (sheep.role() == FarmAnimalRole.MALE ? "ram" : "ewe");
        };
        if (sheep.isSheared()) {
            if (texture.equals("sheep_jacob_lamb")) texture = "sheep_jacob_sheared";
            else texture += "_sheared";
        }
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/sheep/" + texture + ".png");
    }

    private static int eyelidColor(AnimaniaSheep sheep) {
        return switch (sheep.breed()) {
            case DORPER -> 0x222222; case JACOB -> 0x353535; case SUFFOLK -> 0x1D1D1D;
            case DORSET, FRIESIAN, MERINO -> sheep.getColor() == DyeColor.BLACK ? 0x202020
                    : sheep.getColor() == DyeColor.BROWN ? 0x5A463A : 0xD8D8D8;
        };
    }
}
