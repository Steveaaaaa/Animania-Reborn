package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.config.LegacyConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Animania.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.HAMSTER_WHEEL.get(),
                (wheel, side) -> wheel.energy());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HIVE.get(),
                (hive, side) -> hive.tank());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.NEST.get(),
                (nest, side) -> nest.items());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.HAMSTER_WHEEL.get(),
                (wheel, side) -> wheel.items());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CHEESE_MOLD.get(),
                (mold, side) -> mold.items());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CHEESE_MOLD.get(),
                (mold, side) -> mold.isReady() ? null : mold.fluids());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TROUGH.get(),
                (trough, side) -> LegacyConfig.ALLOW_TROUGH_AUTOMATION.get()
                        && trough.water() == 0 && trough.slop() == 0 ? trough.automationItems() : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.TROUGH.get(),
                (trough, side) -> LegacyConfig.ALLOW_TROUGH_AUTOMATION.get()
                        && trough.feed().isEmpty() ? trough.automationFluids() : null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PET_BOWL.get(),
                (bowl, side) -> LegacyConfig.ALLOW_TROUGH_AUTOMATION.get()
                        && bowl.water() == 0 ? bowl.automationItems() : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.PET_BOWL.get(),
                (bowl, side) -> LegacyConfig.ALLOW_TROUGH_AUTOMATION.get()
                        && bowl.food().isEmpty() ? bowl.automationFluids() : null);
    }
}
