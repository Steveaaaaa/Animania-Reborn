package com.animania.common.registry;

import com.animania.Animania;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class ModVillagers {
    private static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Animania.MOD_ID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Animania.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> PET_SELLER_POI = POI_TYPES.register("pet_seller",
            () -> new PoiType(Set.copyOf(ModBlocks.PET_BOWL.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> PET_SELLER =
            PROFESSIONS.register("pet_seller", () -> new VillagerProfession("pet_seller",
                    holder -> holder.is(PET_SELLER_POI.getKey()), holder -> holder.is(PET_SELLER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LEATHERWORKER));

    private ModVillagers() {
    }

    public static void register(IEventBus modBus) {
        POI_TYPES.register(modBus);
        PROFESSIONS.register(modBus);
    }
}
