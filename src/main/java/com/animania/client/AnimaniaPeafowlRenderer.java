package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.peafowl.PeafowlRole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class AnimaniaPeafowlRenderer extends MobRenderer<AnimaniaPeafowl, LegacyAnimalModel<AnimaniaPeafowl>> {
    private final LegacyAnimalModel<AnimaniaPeafowl> peachick;
    private final LegacyAnimalModel<AnimaniaPeafowl> peahen;
    private final LegacyAnimalModel<AnimaniaPeafowl> peacock;

    public AnimaniaPeafowlRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("extra/client/model/peafowl/modelpeafowl"), 0.45F);
        peahen = model;
        peachick = LegacyAnimalModel.load("extra/client/model/peafowl/modelpeachick");
        peacock = LegacyAnimalModel.load("extra/client/model/peafowl/modelpeacock");
        addLayer(new LegacyBlinkLayer<>(this, bird -> "peacocks/" + switch (bird.role()) {
            case PEACHICK -> "peachick_blink"; case PEAHEN -> "peafowl_blink"; case PEACOCK -> "peacock_blink";
        }, AnimaniaPeafowlRenderer::eyelidColor));
    }

    @Override
    public void render(AnimaniaPeafowl bird, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        model = switch (bird.role()) {
            case PEACHICK -> peachick;
            case PEAHEN -> peahen;
            case PEACOCK -> peacock;
        };
        super.render(bird, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected float getBob(AnimaniaPeafowl bird, float partial) {
        float flap = net.minecraft.util.Mth.lerp(partial, bird.oFlap, bird.flap);
        float speed = net.minecraft.util.Mth.lerp(partial, bird.oFlapSpeed, bird.flapSpeed);
        return (net.minecraft.util.Mth.sin(flap) + 1) * speed;
    }

    @Override
    protected void scale(AnimaniaPeafowl bird, PoseStack poseStack, float partialTick) {
        float scale = bird.role() == PeafowlRole.PEACHICK ? 0.30F
                : bird.role() == PeafowlRole.PEAHEN ? 0.9F : 1.0F;
        poseStack.scale(scale, scale, scale);
        boolean nesting = bird.role() != PeafowlRole.PEACOCK
                && bird.level().getBlockState(bird.blockPosition()).is(ModBlocks.NEST.get());
        if (nesting || bird.getData(ModAttachments.SLEEPING)) {
            if (bird.role() == PeafowlRole.PEACOCK) poseStack.translate(-0.25D, 0.45D, -0.45D);
            else poseStack.translate(-0.25D, bird.role() == PeafowlRole.PEACHICK ? 0.10D : 0.35D, -0.25D);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AnimaniaPeafowl bird) {
        String prefix = switch (bird.role()) {
            case PEACHICK -> "peachick_";
            case PEAHEN -> "peafowl_";
            case PEACOCK -> "peacock_";
        };
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID,
                "textures/entity/peacocks/" + prefix + bird.breed().getSerializedName() + ".png");
    }

    private static int eyelidColor(AnimaniaPeafowl bird) {
        return switch (bird.breed()) {
            case BLUE -> switch (bird.role()) { case PEACHICK -> 0x6F5B2D; case PEAHEN -> 0x846F75; case PEACOCK -> 0x8E8670; };
            case CHARCOAL -> bird.role() == PeafowlRole.PEACHICK ? 0x646464 : 0x9E9792;
            case OPAL -> switch (bird.role()) { case PEACHICK -> 0xA1A1A1; case PEAHEN -> 0xBFB3AC; case PEACOCK -> 0x495163; };
            case PEACH -> bird.role() == PeafowlRole.PEACHICK ? 0xB49B7F : 0x7F5A41;
            case PURPLE -> bird.role() == PeafowlRole.PEACHICK ? 0x8B794D : 0x846F75;
            case TAUPE -> 0xA7988E; case WHITE -> 0xCCCCCC;
        };
    }
}
