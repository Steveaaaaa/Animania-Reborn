package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.server.ServerAboutToStartEvent;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class PatternedBedPoiHandler {
    private PatternedBedPoiHandler() { }

    @SubscribeEvent
    public static void beforeServerStarts(ServerAboutToStartEvent event) {
        // Install after the loader's registry snapshot has been restored, before chunks load.
        var home = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(PoiTypes.HOME).value();
        var states = net.minecraftforge.registries.GameData.getBlockStatePointOfInterestTypeMap();
        ModBlocks.patternedBeds().values().forEach(bed -> bed.get().getStateDefinition()
                .getPossibleStates().stream().filter(state -> state.getValue(BedBlock.PART) == BedPart.HEAD)
                .forEach(state -> states.put(state, home)));
    }
}
