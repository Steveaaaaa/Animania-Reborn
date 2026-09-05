package com.animania.client;

import com.animania.common.config.LegacyConfig;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

/** Keeps normal egg quads available while selecting the custom renderer at runtime. */
public final class ConfigurableSpawnEggModel extends BakedModelWrapper<BakedModel> {
    public ConfigurableSpawnEggModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer() {
        return LegacyConfig.FANCY_EGGS.get();
    }
}
