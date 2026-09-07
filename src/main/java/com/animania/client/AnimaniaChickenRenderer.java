package com.animania.client;

import com.animania.Animania;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;

public final class AnimaniaChickenRenderer extends MobRenderer<AnimaniaChicken, LegacyAnimalModel<AnimaniaChicken>> {
    private final LegacyAnimalModel<AnimaniaChicken> chickModel;
    private final LegacyAnimalModel<AnimaniaChicken> henModel;
    private final LegacyAnimalModel<AnimaniaChicken> roosterModel;

    public AnimaniaChickenRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("farm/client/model/chicken/modelhen"), 0.3F);
        chickModel = LegacyAnimalModel.load("farm/client/model/chicken/modelchick");
        henModel = model;
        roosterModel = LegacyAnimalModel.load("farm/client/model/chicken/modelrooster");
        addLayer(new LegacyBlinkLayer<>(this,
                chicken -> "chickens/" + (chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK
                        ? "chick_blink" : "chicken_blink"), AnimaniaChickenRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaChicken chicken, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        model = switch (chicken.role()) {
            case CHICK -> chickModel;
            case HEN -> henModel;
            case ROOSTER -> roosterModel;
        };
        super.render(chicken, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaChicken chicken) {
        if (chicken.role() == com.animania.farm.chicken.ChickenRole.ROOSTER && chicken.hasCustomName()
                && chicken.getName().getString().equals("Ducktonio")) {
            return new ResourceLocation(Animania.MOD_ID,
                    "textures/entity/chickens/rooster_antonio.png");
        }
        String role = chicken.role().name().toLowerCase();
        return new ResourceLocation(Animania.MOD_ID,
                "textures/entity/chickens/" + role + "_" + chicken.breed().texture() + ".png");
    }

    @Override
    protected float getBob(AnimaniaChicken chicken, float partialTick) {
        float flap = Mth.lerp(partialTick, chicken.oFlap, chicken.flap);
        float speed = Mth.lerp(partialTick, chicken.oFlapSpeed, chicken.flapSpeed);
        return (Mth.sin(flap) + 1.0F) * speed;
    }

    @Override
    protected void scale(AnimaniaChicken chicken, PoseStack poseStack, float partialTick) {
        float scale = chicken.role() == com.animania.farm.chicken.ChickenRole.HEN ? 0.9F : 1.0F;
        poseStack.scale(scale, scale, scale);
        boolean nesting = chicken.level().getBlockState(chicken.blockPosition()).is(ModBlocks.NEST.get());
        if (nesting || ModAttachments.getData(chicken, ModAttachments.SLEEPING)) {
            double y = chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK ? 0.10D : 0.35D;
            poseStack.translate(-0.25D, y, -0.25D);
        }
    }

    private static int eyelidColor(AnimaniaChicken chicken) {
        return switch (chicken.breed()) {
            case LEGHORN -> chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK ? 0xFACA65 : 0xF2F2F2;
            case PLYMOUTH_ROCK -> chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK ? 0xD2D7E2 : 0xA29497;
            case RHODE_ISLAND_RED, ORPINGTON -> chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK
                    ? 0xF6C132 : chicken.breed() == com.animania.farm.chicken.ChickenBreed.ORPINGTON ? 0xCD902F : 0x9F4931;
            case WYANDOTTE -> chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK ? 0x492A1E : 0x362018;
        };
    }
}
