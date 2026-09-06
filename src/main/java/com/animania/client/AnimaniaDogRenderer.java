package com.animania.client;

import com.animania.Animania;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaDogRenderer extends MobRenderer<AnimaniaDog, LegacyAnimalModel<AnimaniaDog>> {
    private final java.util.Map<String, LegacyAnimalModel<AnimaniaDog>> models = new java.util.HashMap<>();

    public AnimaniaDogRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("catsdogs/client/models/dogs/modelbloodhound"), 0.4F);
        models.put("modelbloodhound", model);
        addLayer(new LegacyBlinkLayer<>(this, AnimaniaDogRenderer::blinkMask,
                AnimaniaDogRenderer::eyelidColor, AnimaniaDogRenderer::eyelidColor, true));
    }

    @Override
    public void render(AnimaniaDog dog, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        String breed = dog.breed() == DogBreed.BLOOD_HOUND ? "bloodhound"
                : dog.breed().getSerializedName().replace("_", "");
        String name = "model" + breed;
        model = models.computeIfAbsent(name,
                key -> LegacyAnimalModel.load("catsdogs/client/models/dogs/" + key));
        super.render(dog, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(AnimaniaDog dog, PoseStack poseStack, float partialTick) {
        double y = switch (dog.breed()) {
            case BLOOD_HOUND, GREAT_DANE -> -0.1;
            case CHIHUAHUA -> 0.1;
            case CORGI -> -0.05;
            default -> 0;
        };
        double z = switch (dog.breed()) {
            case CHIHUAHUA, POMERANIAN, PUG -> dog.role() == DogRole.PUPPY ? -0.25 : -0.5;
            default -> 0;
        };
        poseStack.translate(0, y, z);
        float scale = switch (dog.breed()) {
            case CHIHUAHUA, POMERANIAN -> switch (dog.role()) {
                case PUPPY -> 0.3F; case FEMALE -> 0.5F; case MALE -> 0.6F;
            };
            case DACHSHUND -> switch (dog.role()) {
                case PUPPY -> 0.6F; case FEMALE -> 1.1F; case MALE -> 1.2F;
            };
            case GREYHOUND -> switch (dog.role()) {
                case PUPPY -> 0.4F; case FEMALE -> 0.7F; case MALE -> 0.8F;
            };
            case PUG -> switch (dog.role()) {
                case PUPPY -> 0.4F; case FEMALE -> 0.7F; case MALE -> 0.8F;
            };
            case FOX -> dog.role() == DogRole.PUPPY ? 0.5F : 0.9F;
            default -> switch (dog.role()) {
                case PUPPY -> 0.5F; case FEMALE -> 0.9F; case MALE -> 1.0F;
            };
        };
        poseStack.scale(scale, scale, scale);
        LegacySleepAnimation.transform(dog, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaDog dog) {
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/dogs/" + dog.textureName() + ".png");
    }

    private static String blinkMask(AnimaniaDog dog) {
        String mask = switch (dog.breed()) {
            case BLOOD_HOUND -> "blood_hound"; case CHIHUAHUA -> "chihuahua"; case CORGI -> "corgi";
            case DACHSHUND -> "dachshund"; case FOX -> "fox"; case GREYHOUND -> "greyhound";
            case POMERANIAN -> "pomeranian"; case POODLE -> "poodle"; case PUG -> "pug";
            case COLLIE, GERMAN_SHEPHERD, GREAT_DANE, HUSKY, LABRADOR, WOLF -> "collie";
        };
        return "dogs/blink_" + mask;
    }

    private static int eyelidColor(AnimaniaDog dog) {
        return switch (dog.breed()) {
            case BLOOD_HOUND -> 0xA56234; case CHIHUAHUA -> 0xF6F1EC; case COLLIE -> 0x403025;
            case CORGI -> 0xFBFBFB; case DACHSHUND -> 0; case FOX -> 0xAD5D3C;
            case GERMAN_SHEPHERD, GREAT_DANE -> 0x815940; case GREYHOUND -> 0x8C5C34;
            case HUSKY -> 0xC4C4C4; case LABRADOR -> 0xC09D77; case POMERANIAN -> 0xFCFCFC;
            case POODLE -> 0xF5F2ED; case PUG -> 0xE8E3DF; case WOLF -> 0xBCB6B0;
        };
    }
}
