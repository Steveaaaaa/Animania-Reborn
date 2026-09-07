package com.animania.farm.world.block;

import com.animania.common.registry.ModEntities;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModItems;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.chicken.ChickenBreed;
import com.animania.farm.chicken.ChickenRole;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.peafowl.PeafowlBreed;
import com.animania.extra.peafowl.PeafowlRole;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.animania.farm.world.block.entity.NestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class NestBlock extends BaseEntityBlock {
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 0, 3);
    public static final EnumProperty<NestBreed> BREED = EnumProperty.create("breed", NestBreed.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 5, 16);

    public NestBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(EGGS, 0).setValue(BREED, NestBreed.EMPTY));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NestBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EGGS, BREED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static boolean canAccept(BlockState state, ChickenBreed breed) {
        return state.getValue(EGGS) < 3
                && (state.getValue(BREED) == NestBreed.EMPTY || state.getValue(BREED) == NestBreed.of(breed));
    }

    public static boolean tryInsert(Level level, BlockPos pos, ChickenBreed breed) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NestBlock) || !canAccept(state, breed)) {
            return false;
        }
        level.setBlock(pos, state.setValue(EGGS, state.getValue(EGGS) + 1)
                .setValue(BREED, NestBreed.of(breed)), UPDATE_ALL);
        return true;
    }

    public static boolean canAcceptPeafowl(BlockState state, PeafowlBreed breed) {
        return state.getValue(EGGS) < 3
                && (state.getValue(BREED) == NestBreed.EMPTY || state.getValue(BREED) == NestBreed.of(breed));
    }

    public static boolean tryInsertPeafowl(Level level, BlockPos pos, PeafowlBreed breed) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NestBlock) || !canAcceptPeafowl(state, breed)) return false;
        level.setBlock(pos, state.setValue(EGGS, state.getValue(EGGS) + 1)
                .setValue(BREED, NestBreed.of(breed)), UPDATE_ALL);
        return true;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        int eggs = state.getValue(EGGS);
        if (eggs == 0) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.animania.nest_empty"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide()) {
            NestBreed nestBreed = state.getValue(BREED);
            ChickenBreed breed = nestBreed.chickenBreed();
            PeafowlBreed peafowl = nestBreed.peafowlBreed();
            ItemStack egg = eggStack(state);
            egg.setCount(1);
            if (!player.addItem(egg)) {
                player.drop(egg, false);
            }
            level.setBlock(pos, state.setValue(EGGS, eggs - 1)
                    .setValue(BREED, eggs == 1 ? NestBreed.EMPTY : state.getValue(BREED)), UPDATE_ALL);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(EGGS) > 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            NestBreed nestBreed = state.getValue(BREED);
            ChickenBreed breed = nestBreed.chickenBreed();
            PeafowlBreed peafowl = nestBreed.peafowlBreed();
            if ((breed != null || peafowl != null) && state.getValue(EGGS) > 0) {
                ItemStack eggs = new ItemStack(peafowl != null
                                ? peafowl == PeafowlBreed.BLUE ? ModItems.PEACOCK_EGG_BLUE.get() : ModItems.PEACOCK_EGG_WHITE.get()
                                : breed.laysBrownEggs() ? ModItems.BROWN_EGG.get() : Items.EGG,
                        state.getValue(EGGS));
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, eggs);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        NestBreed nestBreed = state.getValue(BREED);
        ChickenBreed breed = nestBreed.chickenBreed();
        PeafowlBreed peafowlBreed = nestBreed.peafowlBreed();
        if ((breed == null && peafowlBreed == null)
                || random.nextInt(LegacyConfig.EGG_HATCH_CHANCE.get()) != 0) return;

        if (peafowlBreed != null) {
            List<AnimaniaPeafowl> males = level.getEntitiesOfClass(AnimaniaPeafowl.class,
                    SHAPE.bounds().move(pos).inflate(4.0), bird -> bird.role() == PeafowlRole.PEACOCK);
            if (males.isEmpty()) return;
            PeafowlBreed childBreed = random.nextBoolean() ? peafowlBreed : males.get(0).breed();
            AnimaniaPeafowl chick = ModEntities.peafowl(PeafowlRole.PEACHICK, childBreed).create(level);
            if (chick != null) {
                chick.moveTo(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                        random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(chick);
                consumeEgg(level, pos, state);
            }
            return;
        }

        List<AnimaniaChicken> roosters = level.getEntitiesOfClass(AnimaniaChicken.class,
                SHAPE.bounds().move(pos).inflate(3.0),
                chicken -> chicken.role() == ChickenRole.ROOSTER);
        if (roosters.isEmpty()) {
            return;
        }

        ChickenBreed childBreed = random.nextBoolean() ? breed : roosters.get(0).breed();
        AnimaniaChicken chick = ModEntities.chicken(ChickenRole.CHICK, childBreed).create(level);
        if (chick != null) {
            chick.moveTo(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(chick);
            consumeEgg(level, pos, state);
        }
    }

    private static void consumeEgg(Level level, BlockPos pos, BlockState state) {
        int eggs = state.getValue(EGGS) - 1;
        level.setBlock(pos, state.setValue(EGGS, eggs)
                .setValue(BREED, eggs == 0 ? NestBreed.EMPTY : state.getValue(BREED)), UPDATE_ALL);
    }

    public static ItemStack eggStack(BlockState state) {
        if (!(state.getBlock() instanceof NestBlock) || state.getValue(EGGS) <= 0) return ItemStack.EMPTY;
        NestBreed nestBreed = state.getValue(BREED);
        ChickenBreed breed = nestBreed.chickenBreed();
        PeafowlBreed peafowl = nestBreed.peafowlBreed();
        if (breed == null && peafowl == null) return ItemStack.EMPTY;
        return new ItemStack(peafowl != null
                ? peafowl == PeafowlBreed.BLUE ? ModItems.PEACOCK_EGG_BLUE.get() : ModItems.PEACOCK_EGG_WHITE.get()
                : breed.laysBrownEggs() ? ModItems.BROWN_EGG.get() : Items.EGG, state.getValue(EGGS));
    }

    public static void consumeEggs(Level level, BlockPos pos, int amount) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NestBlock)) return;
        int eggs = Math.max(0, state.getValue(EGGS) - Math.max(0, amount));
        level.setBlock(pos, state.setValue(EGGS, eggs)
                .setValue(BREED, eggs == 0 ? NestBreed.EMPTY : state.getValue(BREED)), UPDATE_ALL);
    }

    /** Ferrets and hedgehogs in 1.12 ate chicken eggs from nests, but ignored peafowl eggs. */
    public static boolean takeChickenEggForPredator(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NestBlock) || state.getValue(EGGS) <= 0
                || state.getValue(BREED).chickenBreed() == null) return false;
        consumeEgg(level, pos, state);
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        return useWithoutItem(state, level, pos, player, hit);
    }
}
