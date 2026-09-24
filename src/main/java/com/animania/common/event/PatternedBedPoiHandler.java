package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class PatternedBedPoiHandler {
    private PatternedBedPoiHandler() { }

    @SubscribeEvent
    public static void beforeServerStarts(ServerAboutToStartEvent event) {
        // Install after the loader's registry snapshot has been restored, before chunks load.
        var home = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(PoiTypes.HOME);
        var states = net.neoforged.neoforge.registries.GameData.getBlockStatePointOfInterestTypeMap();
        ModBlocks.patternedBeds().values().forEach(bed -> bed.get().getStateDefinition()
                .getPossibleStates().stream().filter(state -> state.getValue(BedBlock.PART) == BedPart.HEAD)
                .forEach(state -> states.put(state, home)));
    }
}
