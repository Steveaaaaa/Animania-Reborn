package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.rabbit.RabbitRole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaRabbitRenderer extends MobRenderer<AnimaniaRabbit, LegacyAnimalModel<AnimaniaRabbit>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaRabbit>> models = new java.util.HashMap<>();
    public AnimaniaRabbitRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("extra/client/model/rabbits/modelcottontail"), 0.3F);
        models.put("modelcottontail", model);
        addLayer(new LegacyBlinkLayer<>(this, rabbit -> "rabbits/rabbit_blink", AnimaniaRabbitRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaRabbit rabbit, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        String name = switch (rabbit.breed()) {
            case NEW_ZEALAND -> "newzealand";
            default -> rabbit.breed().getSerializedName();
        };
        String key = "model" + name;
        model = models.computeIfAbsent(key,
                value -> LegacyAnimalModel.load("extra/client/model/rabbits/" + value));
        super.render(rabbit, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaRabbit rabbit, PoseStack poseStack, float partialTick) {
        float breed = switch (rabbit.role()) {
            case BUCK -> switch (rabbit.breed()) {
                case CHINCHILLA, NEW_ZEALAND -> 0.57F; case COTTONTAIL, DUTCH, HAVANA -> 0.52F;
                case JACK -> 0.56F; case LOP -> 0.47F; case REX -> 0.54F;
            };
            case DOE -> switch (rabbit.breed()) {
                case CHINCHILLA -> 0.59F; case COTTONTAIL, HAVANA -> 0.56F;
                case DUTCH -> 0.53F; case JACK -> 0.57F; case LOP -> 0.51F;
                case NEW_ZEALAND, REX -> 0.58F;
            };
            case KIT -> switch (rabbit.breed()) {
                case CHINCHILLA -> 0.33F; case COTTONTAIL, DUTCH, HAVANA -> 0.26F;
                case JACK, NEW_ZEALAND -> 0.32F; case LOP -> 0.23F; case REX -> 0.28F;
            };
        };
        poseStack.scale(breed, breed, breed);
        poseStack.translate(0, 0, -0.5F);
        if (rabbit.getData(ModAttachments.SLEEPING)) poseStack.translate(-0.25D, 0.25D, -0.25D);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaRabbit rabbit) {
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/rabbits/rabbit_" + rabbit.textureName() + ".png");
    }

    private static int eyelidColor(AnimaniaRabbit rabbit) {
        return switch (rabbit.breed()) {
            case CHINCHILLA -> 0x9E9E9E; case COTTONTAIL -> 0x896E58; case DUTCH, HAVANA -> 0x404040;
            case JACK -> 0x938375; case NEW_ZEALAND -> 0xF4F2F2; case REX -> 0x574133;
            case LOP -> switch (rabbit.textureName()) {
                case "black" -> 0x202020; case "white" -> 0xEFEFEF; case "brown" -> 0x76523B;
                default -> 0x777777;
            };
        };
    }
}
