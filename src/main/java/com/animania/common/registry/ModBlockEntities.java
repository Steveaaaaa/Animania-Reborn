package com.animania.common.registry;

import com.animania.Animania;
import com.animania.farm.world.block.entity.CheeseMoldBlockEntity;
import com.animania.extra.world.block.entity.HamsterWheelBlockEntity;
import com.animania.catsdogs.block.entity.PetBowlBlockEntity;
import com.animania.catsdogs.block.entity.PetPropBlockEntity;
import com.animania.farm.world.block.entity.HiveBlockEntity;
import com.animania.farm.world.block.entity.NestBlockEntity;
import com.animania.common.world.block.entity.SaltLickBlockEntity;
import com.animania.common.world.block.entity.TroughBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Animania.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CheeseMoldBlockEntity>> CHEESE_MOLD =
            TYPES.register("cheese_mold", () -> BlockEntityType.Builder
                    .of(CheeseMoldBlockEntity::new, ModBlocks.CHEESE_MOLD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HamsterWheelBlockEntity>> HAMSTER_WHEEL =
            TYPES.register("hamster_wheel", () -> BlockEntityType.Builder
                    .of(HamsterWheelBlockEntity::new, ModBlocks.HAMSTER_WHEEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PetBowlBlockEntity>> PET_BOWL =
            TYPES.register("pet_bowl", () -> BlockEntityType.Builder
                    .of(PetBowlBlockEntity::new, ModBlocks.PET_BOWL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PetPropBlockEntity>> PET_PROP =
            TYPES.register("pet_prop", () -> BlockEntityType.Builder.of(PetPropBlockEntity::new,
                    ModBlocks.CAT_BED_1.get(), ModBlocks.CAT_BED_2.get(), ModBlocks.CAT_TOWER.get(),
                    ModBlocks.DOG_HOUSE.get(), ModBlocks.DOG_PILLOW.get(), ModBlocks.LITTER_BOX.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HiveBlockEntity>> HIVE =
            TYPES.register("hive", () -> BlockEntityType.Builder
                    .of(HiveBlockEntity::new, ModBlocks.HIVE.get(), ModBlocks.WILD_HIVE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NestBlockEntity>> NEST =
            TYPES.register("nest", () -> BlockEntityType.Builder
                    .of(NestBlockEntity::new, ModBlocks.NEST.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaltLickBlockEntity>> SALT_LICK =
            TYPES.register("salt_lick", () -> BlockEntityType.Builder
                    .of(SaltLickBlockEntity::new, ModBlocks.SALT_LICK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TroughBlockEntity>> TROUGH =
            TYPES.register("trough", () -> BlockEntityType.Builder
                    .of(TroughBlockEntity::new, ModBlocks.TROUGH.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
