package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;
import java.util.function.ToIntFunction;

/** Shared modern equivalent of the original two-mask LayerBlinking renderer. */
public final class LegacyBlinkLayer<T extends Entity> extends RenderLayer<T, LegacyAnimalModel<T>> {
    private final Function<T, String> mask;
    private final ToIntFunction<T> leftColor;
    private final ToIntFunction<T> rightColor;
    private final boolean oneTexture;

    public LegacyBlinkLayer(RenderLayerParent<T, LegacyAnimalModel<T>> parent, Function<T, String> mask,
                            ToIntFunction<T> color) {
        this(parent, mask, color, color, false);
    }

    public LegacyBlinkLayer(RenderLayerParent<T, LegacyAnimalModel<T>> parent, Function<T, String> mask,
                            ToIntFunction<T> leftColor, ToIntFunction<T> rightColor, boolean oneTexture) {
        super(parent);
        this.mask = mask;
        this.leftColor = leftColor;
        this.rightColor = rightColor;
        this.oneTexture = oneTexture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        boolean sleeping = ModAttachments.getData(entity, ModAttachments.SLEEPING);
        // The old timer blinked for seven ticks at randomized roughly five-second intervals.
        // Entity id offsets keep nearby animals from blinking in lockstep without networking a cosmetic timer.
        boolean blinking = Math.floorMod(entity.tickCount + entity.getId() * 31, 100) < 7;
        if (!sleeping && !blinking) return;
        String base = mask.apply(entity);
        if (oneTexture) {
            renderMask(poseStack, buffers, packedLight, base + ".png", leftColor.applyAsInt(entity));
        } else {
            renderMask(poseStack, buffers, packedLight, base + "_left.png", leftColor.applyAsInt(entity));
            renderMask(poseStack, buffers, packedLight, base + "_right.png", rightColor.applyAsInt(entity));
        }
    }

    private void renderMask(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                            String file, int rgb) {
        ResourceLocation texture = new ResourceLocation(Animania.MOD_ID,
                "textures/entity/" + file);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                0xFF000000 | rgb & 0x00FFFFFF);
    }
}
