package com.animania.client;

import com.animania.common.config.LegacyConfig;
import com.animania.common.item.AnimaniaSpawnEggItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Map;

/** Renders the animal represented by a spawn egg, as Animania 1.12 did. */
public final class FancySpawnEggRenderer extends BlockEntityWithoutLevelRenderer {
    private static FancySpawnEggRenderer instance;
    private final Map<EntityType<?>, Entity> previews = new IdentityHashMap<>();
    private Level cachedLevel;

    private FancySpawnEggRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static FancySpawnEggRenderer instance() {
        if (instance == null) instance = new FancySpawnEggRenderer();
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!LegacyConfig.FANCY_EGGS.get() || !(stack.getItem() instanceof AnimaniaSpawnEggItem egg)) return;
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) return;
        if (cachedLevel != level) {
            previews.clear();
            cachedLevel = level;
        }
        EntityType<?> type = egg.getType(stack);
        Entity entity = previews.computeIfAbsent(type, key -> key.create(level));
        if (entity == null) return;

        float width = Math.max(0.1F, entity.getBbWidth());
        float height = Math.max(0.1F, entity.getBbHeight());
        float scale = (context == ItemDisplayContext.GROUND ? 0.65F : 0.8F) / Math.max(width, height);
        float yaw = LegacyConfig.FANCY_EGGS_ROTATE.get()
                ? (System.currentTimeMillis() % 10_000L) * 0.036F : 20.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, context == ItemDisplayContext.GUI ? 0.05F : 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.scale(scale, scale, scale);
        entity.setPos(0.0D, 0.0D, 0.0D);
        entity.setYRot(0.0F);
        entity.setXRot(0.0F);
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F,
                poseStack, buffers, packedLight);
        dispatcher.setRenderShadow(true);
        poseStack.popPose();
    }
}
