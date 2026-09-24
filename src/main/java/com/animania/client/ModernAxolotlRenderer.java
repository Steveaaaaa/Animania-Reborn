package com.animania.client;

import com.animania.modern.ModernAxolotl;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public final class ModernAxolotlRenderer extends AxolotlRenderer {
    public ModernAxolotlRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new AxolotlModel<>(ModernAxolotlModel.createBodyLayer().bakeRoot());
    }
    @Override public ResourceLocation getTextureLocation(Axolotl entity) {
        if (!(entity instanceof ModernAxolotl axolotl)) return super.getTextureLocation(entity);
        return ResourceLocation.tryParse("animania:textures/entity/modern/axolotl_" + axolotl.breed() + ".png");
    }
}
