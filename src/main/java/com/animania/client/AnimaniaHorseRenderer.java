package com.animania.client;

import com.animania.Animania;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.FarmAnimalRole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaHorseRenderer extends MobRenderer<AnimaniaHorse, LegacyAnimalModel<AnimaniaHorse>> {
    private final LegacyAnimalModel<AnimaniaHorse> foal;
    private final LegacyAnimalModel<AnimaniaHorse> mare;
    private final LegacyAnimalModel<AnimaniaHorse> stallion;
    private static final String[] COATS = {"black", "bw1", "bw2", "grey", "red", "white"};

    public AnimaniaHorseRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("farm/client/model/horse/modeldrafthorsemare"), 0.9F);
        mare = model;
        foal = LegacyAnimalModel.load("farm/client/model/horse/modeldrafthorsefoal");
        stallion = LegacyAnimalModel.load("farm/client/model/horse/modeldrafthorsestallion");
        addLayer(new LegacyBlinkLayer<>(this, horse -> "horses/horse_blink",
                horse -> switch (Math.floorMod(horse.coat(), COATS.length)) {
                    case 0 -> 0x17120F; case 3 -> 0x777777; case 4 -> 0x75412B; case 5 -> 0xDDDDDD;
                    default -> 0x303030;
                }));
    }

    @Override
    public void render(AnimaniaHorse horse, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        model = switch (horse.role()) {
            case YOUNG -> foal;
            case FEMALE -> mare;
            case MALE -> stallion;
        };
        super.render(horse, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaHorse horse, PoseStack poseStack, float partialTick) {
        float scale = horse.role() == FarmAnimalRole.YOUNG ? 0.40F
                : horse.role() == FarmAnimalRole.MALE ? 0.85F : 0.72F;
        poseStack.scale(scale, scale, scale);
        LegacySleepAnimation.transform(horse, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaHorse horse) {
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/horses/draft_horse_" + COATS[Math.floorMod(horse.coat(), COATS.length)] + ".png");
    }
}
