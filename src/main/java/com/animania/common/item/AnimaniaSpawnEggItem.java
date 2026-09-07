package com.animania.common.item;

import com.animania.client.FancySpawnEggRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Spawn egg which can switch to the original live-entity inventory preview. */
public final class AnimaniaSpawnEggItem extends ForgeSpawnEggItem {
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
