package com.animania.client;

import com.animania.Animania;
import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.cat.CatRole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaCatRenderer extends MobRenderer<AnimaniaCat, LegacyAnimalModel<AnimaniaCat>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaCat>> models = new java.util.HashMap<>();
    public AnimaniaCatRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("catsdogs/client/models/cats/modelcatragdoll"), 0.35F);
        models.put("modelcatragdoll", model);
        addLayer(new CatBlinkLayer(this));
    }

    @Override
    public void render(AnimaniaCat cat, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        String breed = cat.breed() == com.animania.catsdogs.cat.CatBreed.NORWEGIAN ? "ragdoll"
                : cat.breed().getSerializedName().replace("_", "");
        String name = "modelcat" + breed;
        model = models.computeIfAbsent(name,
                key -> LegacyAnimalModel.load("catsdogs/client/models/cats/" + key));
        super.render(cat, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaCat cat, PoseStack poseStack, float partialTick) {
        float scale = switch (cat.role()) {
            case KITTEN -> switch (cat.breed()) {
                case ASIATIC -> 0.35F;
                default -> 0.30F;
            };
            case QUEEN -> switch (cat.breed()) {
                case RAGDOLL -> 0.655F; case AMERICAN_SHORTHAIR -> 0.635F; case ASIATIC -> 0.765F;
                case EXOTIC -> 0.585F; case NORWEGIAN -> 0.575F; case OCELOT -> 0.82F;
                case SIAMESE -> 0.555F; case TABBY -> 0.68F;
            };
            case TOM -> switch (cat.breed()) {
                case RAGDOLL -> 0.67F; case AMERICAN_SHORTHAIR -> 0.65F; case ASIATIC -> 0.78F;
                case EXOTIC -> 0.60F; case NORWEGIAN -> 0.59F; case OCELOT -> 0.85F;
                case SIAMESE -> 0.57F; case TABBY -> 0.70F;
            };
        };
        poseStack.scale(scale, scale, scale);
        LegacySleepAnimation.transform(cat, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaCat cat) {
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/cats/" + cat.breed().getSerializedName() + ".png");
    }
}
