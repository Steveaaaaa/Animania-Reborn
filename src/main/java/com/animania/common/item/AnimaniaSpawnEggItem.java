package com.animania.common.item;

import com.animania.client.FancySpawnEggRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Spawn egg which can switch to the original live-entity inventory preview. */
public final class AnimaniaSpawnEggItem extends DeferredSpawnEggItem {
    public AnimaniaSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor,
                                int highlightColor, Properties properties) {
        super(type, backgroundColor, highlightColor, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FancySpawnEggRenderer.instance();
            }
        });
    }
}
