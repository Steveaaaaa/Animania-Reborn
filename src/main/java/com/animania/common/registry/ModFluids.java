package com.animania.common.registry;

import com.animania.Animania;
import com.animania.farm.dairy.MilkType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModFluids {
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Animania.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, Animania.MOD_ID);
    private static final Map<MilkType, MilkFluidSet> MILKS = new EnumMap<>(MilkType.class);
    public static final SimpleFluidSet HONEY = new SimpleFluidSet("animania_honey", 3000, 7000,
            () -> ModItems.HONEY_BUCKET.get(), () -> ModBlocks.HONEY.get());
    public static final SimpleFluidSet SLOP = new SimpleFluidSet("slop", 3000, 7000,
            () -> ModItems.SLOP_BUCKET.get(), () -> ModBlocks.SLOP.get());

    static {
        for (MilkType type : MilkType.values()) MILKS.put(type, new MilkFluidSet(type));
    }

    private ModFluids() {
    }

    public static MilkFluidSet milk(MilkType type) {
        return MILKS.get(type);
    }

    public static Collection<MilkFluidSet> allMilk() {
        return MILKS.values();
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    public static final class MilkFluidSet {
        private final MilkType milkType;
        private final RegistryObject<FluidType> type;
        private final RegistryObject<ForgeFlowingFluid.Source> source;
        private final RegistryObject<ForgeFlowingFluid.Flowing> flowing;

        private MilkFluidSet(MilkType milkType) {
            this.milkType = milkType;
            String id = "milk_" + milkType.getSerializedName();
            this.type = FLUID_TYPES.register(id, () -> new FluidType(
                    FluidType.Properties.create().descriptionId("fluid.animania." + id)
                            .density(500).viscosity(1000)) {
                @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                    consumer.accept(com.animania.client.ClientModEvents.fluidRendering("fluid/" + id + "_still", "fluid/" + id + "_flow", 0xFFFFFFFF));
                }
            });
            this.source = FLUIDS.register(id, () -> new ForgeFlowingFluid.Source(properties()));
            this.flowing = FLUIDS.register("flowing_" + id, () -> new ForgeFlowingFluid.Flowing(properties()));
        }

        private ForgeFlowingFluid.Properties properties() {
            return new ForgeFlowingFluid.Properties(type, source, flowing)
                    .bucket(bucketSupplier())
                    .block(blockSupplier())
                    .tickRate(8)
                    .slopeFindDistance(3);
        }

        private Supplier<? extends Item> bucketSupplier() {
            return () -> ModItems.milkBucket(milkType).get();
        }

        private Supplier<? extends LiquidBlock> blockSupplier() {
            return () -> ModBlocks.milkBlock(milkType).get();
        }

        public FluidType type() {
            return type.get();
        }

        public ForgeFlowingFluid.Source source() {
            return source.get();
        }

        public ForgeFlowingFluid.Flowing flowing() {
            return flowing.get();
        }
    }

    public static final class SimpleFluidSet {
        private final String id;
        private final RegistryObject<FluidType> type;
        private final RegistryObject<ForgeFlowingFluid.Source> source;
        private final RegistryObject<ForgeFlowingFluid.Flowing> flowing;
        private final Supplier<? extends Item> bucket;
        private final Supplier<? extends LiquidBlock> block;

        private SimpleFluidSet(String id, int density, int viscosity, Supplier<? extends Item> bucket,
                               Supplier<? extends LiquidBlock> block) {
            this.id = id;
            this.bucket = bucket;
            this.block = block;
            this.type = FLUID_TYPES.register(id, () -> new FluidType(
                    FluidType.Properties.create().descriptionId("fluid.animania." + id)
                            .density(density).viscosity(viscosity)) {
                @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                    consumer.accept(com.animania.client.ClientModEvents.fluidRendering("fluid/" + id + "_still", "fluid/" + id + "_flow", 0xFFFFFFFF));
                }
            });
            this.source = FLUIDS.register(id, () -> new ForgeFlowingFluid.Source(properties()));
            this.flowing = FLUIDS.register("flowing_" + id, () -> new ForgeFlowingFluid.Flowing(properties()));
        }

        private ForgeFlowingFluid.Properties properties() {
            return new ForgeFlowingFluid.Properties(type, source, flowing)
                    .bucket(bucket).block(block).tickRate(20).slopeFindDistance(2);
        }

        public String id() {
            return id;
        }

        public FluidType type() {
            return type.get();
        }

        public ForgeFlowingFluid.Source source() {
            return source.get();
        }

        public ForgeFlowingFluid.Flowing flowing() {
            return flowing.get();
        }
    }
}
