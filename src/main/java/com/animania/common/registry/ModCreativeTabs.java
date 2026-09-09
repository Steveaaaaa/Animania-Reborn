package com.animania.common.registry;

import com.animania.Animania;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Animania.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.animania.main"))
                    .icon(() -> ModItems.TROUGH.get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> {
                                if (item.get() != ModItems.CHEESE_SANDWICH.get()
                                        && item.get() != ModItems.TRUFFLE_RISOTTO.get()
                                        && item.get() != ModItems.CHEVON_STEW.get()
                                        && item.get() != ModItems.PEACOCK_PILAF.get()
                                        && item.get() != ModItems.THREE_CHEESE_PASTA.get()
                                        || net.minecraftforge.fml.ModList.get().isLoaded("farmersdelight")) output.accept(item.get());
                            }))
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
