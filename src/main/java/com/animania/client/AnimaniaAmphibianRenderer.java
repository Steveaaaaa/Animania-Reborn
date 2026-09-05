package com.animania.client;

import com.animania.Animania;
import com.animania.extra.amphibian.AnimaniaAmphibian;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaAmphibianRenderer
        extends MobRenderer<AnimaniaAmphibian, LegacyAnimalModel<AnimaniaAmphibian>> {
    private final LegacyAnimalModel<AnimaniaAmphibian> frog;
    private final LegacyAnimalModel<AnimaniaAmphibian> toad;
    public AnimaniaAmphibianRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("extra/client/model/amphibians/modelfrog"), 0.25F);
        frog = model;
        toad = LegacyAnimalModel.load("extra/client/model/amphibians/modeltoad");
    }

    @Override
    public void render(AnimaniaAmphibian amphibian, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        model = amphibian.kind() == AnimaniaAmphibian.Kind.TOAD ? toad : frog;
        super.render(amphibian, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaAmphibian amphibian, PoseStack poseStack, float partialTick) {
        float scale = amphibian.kind() == AnimaniaAmphibian.Kind.TOAD ? 0.32F
                : amphibian.kind() == AnimaniaAmphibian.Kind.DART_FROG ? 0.20F : 0.30F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaAmphibian amphibian) {
        if (amphibian.hasCustomName() && amphibian.getName().getString().equals("Pepe")) {
            return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                    "textures/entity/amphibians/frogs/pepe_frog.png");
        }
        if (amphibian.hasCustomName() && amphibian.getName().getString().equalsIgnoreCase("me_irl")
                && java.time.LocalDate.now().getDayOfWeek() == java.time.DayOfWeek.WEDNESDAY) {
            return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                    "textures/entity/amphibians/frogs/frog_white.png");
        }
        String path = switch (amphibian.kind()) {
            case TOAD -> "amphibians/toads/toad";
            case FROG -> "amphibians/frogs/" + (amphibian.skin() == 0 ? "default_frog" : "green_frog");
            case DART_FROG -> "amphibians/dartfrogs/" + switch (amphibian.skin()) {
                case 1 -> "red_dart_frog";
                case 2 -> "yellow_dart_frog";
                default -> "blue_dart_frog";
            };
        };
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID, "textures/entity/" + path + ".png");
    }
}
