package com.animania.common.registry;

import com.animania.Animania;
import com.animania.common.world.block.TroughBlock;
import com.animania.common.world.block.MudBlock;
import com.animania.common.world.block.GroundCoverBlock;
import com.animania.common.world.block.Invisiblock;
import com.animania.common.world.block.SaltLickBlock;
import com.animania.farm.world.block.NestBlock;
import com.animania.farm.dairy.MilkType;
import com.animania.farm.world.block.CheeseMoldBlock;
import com.animania.farm.world.block.CheeseWheelBlock;
import com.animania.farm.world.block.HiveBlock;
import com.animania.extra.world.block.HamsterWheelBlock;
import com.animania.catsdogs.block.PetBowlBlock;
import com.animania.catsdogs.block.PetPropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Animania.MOD_ID);

    public static final DeferredBlock<TroughBlock> TROUGH = BLOCKS.register(
            "trough",
            () -> new TroughBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    public static final DeferredBlock<NestBlock> NEST = BLOCKS.register(
            "block_nest",
            () -> new NestBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)
                    .randomTicks()
                    .noOcclusion())
    );
    public static final DeferredBlock<MudBlock> MUD = BLOCKS.register("block_mud",
            () -> new MudBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
                    .strength(1.0F).sound(SoundType.MUD).randomTicks()));
    public static final DeferredBlock<GroundCoverBlock> STRAW = BLOCKS.register("block_straw",
            () -> new GroundCoverBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.1F).sound(SoundType.GRASS).noCollission().noOcclusion()));
    public static final DeferredBlock<GroundCoverBlock> SEEDS = BLOCKS.register("block_seeds",
            () -> new GroundCoverBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                    .strength(0.0F).sound(SoundType.CROP).noCollission().noOcclusion().noLootTable()));
    public static final DeferredBlock<SaltLickBlock> SALT_LICK = BLOCKS.register("salt_lick",
            () -> new SaltLickBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
                    .strength(1.2F, 1.7F).sound(SoundType.STONE).noOcclusion()));
    public static final DeferredBlock<Invisiblock> INVISIBLOCK = BLOCKS.register("block_invisiblock",
            () -> new Invisiblock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .strength(-1.0F, 3_600_000.0F).noLootTable().noOcclusion()));
    public static final DeferredBlock<HamsterWheelBlock> HAMSTER_WHEEL = BLOCKS.register("block_hamster_wheel",
            () -> new HamsterWheelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(1.4F, 3.4F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<PetBowlBlock> PET_BOWL = BLOCKS.register("pet_bowl",
            () -> new PetBowlBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(1.2F, 1.5F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<HiveBlock> HIVE = BLOCKS.register("block_hive",
            () -> new HiveBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.3F, 0.3F).sound(SoundType.WOOD).noOcclusion(), false));
    public static final DeferredBlock<HiveBlock> WILD_HIVE = BLOCKS.register("block_wild_hive",
            () -> new HiveBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.3F, 0.3F).sound(SoundType.WOOD).noOcclusion(), true));
    public static final DeferredBlock<PetPropBlock> CAT_BED_1 = petProp("cat_bed_1", MapColor.COLOR_RED,
            SoundType.WOOL, 1.0F, Block.box(2, 0, 2, 14, 1, 14));
    public static final DeferredBlock<PetPropBlock> CAT_BED_2 = petProp("cat_bed_2", MapColor.COLOR_RED,
            SoundType.WOOL, 1.0F, Block.box(2, 0, 2, 14, 2, 14));
    public static final DeferredBlock<PetPropBlock> CAT_TOWER = petProp("cat_tower", MapColor.WOOD,
            SoundType.WOOD, 1.4F, Block.box(0, 0, 0, 16, 24, 16));
    public static final DeferredBlock<PetPropBlock> DOG_HOUSE = petProp("dog_house", MapColor.COLOR_RED,
            SoundType.STONE, 1.5F, Block.box(0, 0, 0, 16, 16, 16));
    public static final DeferredBlock<PetPropBlock> DOG_PILLOW = petProp("dog_pillow", MapColor.COLOR_RED,
            SoundType.WOOL, 1.0F, Block.box(1, 0, 1, 15, 1, 15));
    public static final DeferredBlock<PetPropBlock> LITTER_BOX = petProp("litter_box", MapColor.METAL,
            SoundType.STONE, 1.5F, Block.box(1, 0, 1, 15, 3, 15));
    private static final Map<MilkType, DeferredBlock<LiquidBlock>> MILK_BLOCKS = new EnumMap<>(MilkType.class);
    private static final Map<MilkType, DeferredBlock<CheeseWheelBlock>> CHEESE_BLOCKS = new EnumMap<>(MilkType.class);
    private static final Map<String, DeferredBlock<Block>> ANIMANIA_WOOL = new java.util.LinkedHashMap<>();
    public static final DeferredBlock<LiquidBlock> HONEY = BLOCKS.register("animania_honey",
            () -> new LiquidBlock(ModFluids.HONEY.source(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW).replaceable().noCollission().strength(100.0F).noLootTable().liquid()));
    public static final DeferredBlock<LiquidBlock> SLOP = BLOCKS.register("slop",
            () -> new LiquidBlock(ModFluids.SLOP.source(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).replaceable().noCollission().strength(100.0F).noLootTable().liquid()));
    public static final DeferredBlock<CheeseMoldBlock> CHEESE_MOLD = BLOCKS.register("cheese_mold",
            () -> new CheeseMoldBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(0.9F).sound(SoundType.WOOD).noOcclusion()));

    static {
        for (MilkType type : MilkType.values()) {
            MILK_BLOCKS.put(type, BLOCKS.register("milk_" + type.getSerializedName(),
                    () -> new LiquidBlock(ModFluids.milk(type).source(), BlockBehaviour.Properties.of()
                            .mapColor(MapColor.SNOW).replaceable().noCollission().strength(100.0F).noLootTable().liquid())));
            CHEESE_BLOCKS.put(type, BLOCKS.register("cheese_" + type.getSerializedName(),
                    () -> new CheeseWheelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.6F).sound(SoundType.WOOL).noOcclusion())));
        }
        for (String type : new String[]{"dorset_brown", "friesian_black", "friesian_brown", "jacob",
                "merino_brown", "merino_white", "suffolk_brown"}) {
            ANIMANIA_WOOL.put(type, BLOCKS.register("wool_" + type,
                    () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL)
                            .strength(0.8F).sound(SoundType.WOOL))));
        }
    }

    private ModBlocks() {
    }

    public static DeferredBlock<LiquidBlock> milkBlock(MilkType type) {
        return MILK_BLOCKS.get(type);
    }

    public static DeferredBlock<CheeseWheelBlock> cheeseBlock(MilkType type) {
        return CHEESE_BLOCKS.get(type);
    }

    public static Map<String, DeferredBlock<Block>> animaniaWool() {
        return java.util.Collections.unmodifiableMap(ANIMANIA_WOOL);
    }

    private static DeferredBlock<PetPropBlock> petProp(String name, MapColor color, SoundType sound,
                                                        float strength, net.minecraft.world.phys.shapes.VoxelShape shape) {
        return BLOCKS.register(name, () -> new PetPropBlock(BlockBehaviour.Properties.of().mapColor(color)
                .strength(strength).sound(sound).noOcclusion(), shape));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
