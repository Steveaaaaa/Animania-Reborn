package com.animania.client;

import com.animania.modern.ModernFox;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Fox;

public final class ModernFoxRenderer extends FoxRenderer {
    public ModernFoxRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new Model(ModernWildlifeModels.fox().bakeRoot());
    }

    private static final class Model extends FoxModel<Fox> {
        private final net.minecraft.client.model.geom.ModelPart root;
        Model(net.minecraft.client.model.geom.ModelPart root) { super(root); this.root = root; }
        @Override public void setupAnim(Fox fox, float swing, float amount, float age, float yaw, float pitch) {
            super.setupAnim(fox, swing, amount, age, yaw, pitch);
            FamilyBehaviorAnimation.applyNative(fox, root, age - fox.tickCount);
        }
        @Override public void prepareMobModel(Fox fox, float swing, float amount, float partial) {
            root.getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);
            super.prepareMobModel(fox, swing, amount, partial);
        }
    }

    @Override public ResourceLocation getTextureLocation(Fox entity) {
        if (!(entity instanceof ModernFox fox))
            return super.getTextureLocation(entity);
        return ResourceLocation.tryParse("animania:textures/entity/modern/fox_" + fox.coatName()
                + (fox.isSleeping() ? "_sleep" : "") + ".png");
    }
}
