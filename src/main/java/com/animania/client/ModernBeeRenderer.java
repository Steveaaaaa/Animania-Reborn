package com.animania.client;

import com.animania.modern.ModernBee;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;

public final class ModernBeeRenderer extends BeeRenderer {
    public ModernBeeRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new BeeModel<>(ModernBeeModel.createBodyLayer().bakeRoot());
    }
    @Override public ResourceLocation getTextureLocation(Bee entity) {
        if (!(entity instanceof ModernBee bee)) return super.getTextureLocation(entity);
        return ResourceLocation.tryParse("animania:textures/entity/modern/bee_" + bee.breed()
                + (bee.isAngry() ? "_angry" : "") + (bee.hasNectar() ? "_nectar" : "") + ".png");
    }
}
