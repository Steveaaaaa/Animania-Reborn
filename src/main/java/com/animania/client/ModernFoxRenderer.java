package com.animania.client;

import com.animania.modern.ModernFox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public final class ModernFoxRenderer extends MobRenderer<ModernFox, LegacyAnimalModel<ModernFox>> {
    public ModernFoxRenderer(EntityRendererProvider.Context context) {
        super(context, LegacyAnimalModel.load("catsdogs/client/models/dogs/modelfox"), 0.4F);
        addLayer(new MouthLayer(this, context.getItemInHandRenderer()));
    }
    @Override protected void scale(ModernFox fox, PoseStack poses, float partial) {
        float size = fox.isBaby() ? 0.5F : 0.9F;
        poses.scale(size, size, size);
    }
    @Override public ResourceLocation getTextureLocation(ModernFox fox) {
        boolean closed = fox.isSleeping() || Math.floorMod(fox.tickCount + fox.getId() * 31, 100) < 7;
        return ResourceLocation.tryParse("animania:textures/entity/modern/legacy_fox_" + fox.coatName()
                + (closed ? "_sleep" : "") + ".png");
    }
    private static final class MouthLayer extends RenderLayer<ModernFox, LegacyAnimalModel<ModernFox>> {
        private final ItemInHandRenderer items;
        MouthLayer(ModernFoxRenderer renderer, ItemInHandRenderer items) { super(renderer); this.items = items; }
        @Override public void render(PoseStack poses, MultiBufferSource buffers, int light, ModernFox fox,
                                     float swing, float amount, float partial, float age, float yaw, float pitch) {
            if (fox.getMainHandItem().isEmpty()) return;
            poses.pushPose();
            getParentModel().translateToParts(poses, "body", "neck1", "head_base", "head_front");
            poses.translate(0, 0.06, -0.18);
            poses.mulPose(Axis.XP.rotationDegrees(90));
            poses.scale(0.5F, 0.5F, 0.5F);
            items.renderItem(fox, fox.getMainHandItem(), ItemDisplayContext.GROUND, false, poses, buffers, light);
            poses.popPose();
        }
    }
}
