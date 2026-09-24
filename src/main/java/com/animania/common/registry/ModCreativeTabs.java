package com.animania.common.registry;

import com.animania.Animania;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Animania.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.animania.main"))
                    .icon(() -> ModItems.TROUGH.get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> {
                                String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.get()).getPath();
                                if (path.equals("entity_egg_modern_fox") || path.equals("entity_egg_mountain_goat")) return;
                                if (item.get() != ModItems.CHEESE_SANDWICH.get()
                                        && item.get() != ModItems.TRUFFLE_RISOTTO.get()
                                        && item.get() != ModItems.CHEVON_STEW.get()
                                        && item.get() != ModItems.PEACOCK_PILAF.get()
                                        && item.get() != ModItems.THREE_CHEESE_PASTA.get()
                                        || net.neoforged.fml.ModList.get().isLoaded("farmersdelight")) output.accept(item.get());
                            }))
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
