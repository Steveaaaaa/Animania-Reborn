package com.animania.farm.world.block;

import com.animania.farm.world.block.entity.HiveBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModItems;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HiveBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape HIVE_SHAPE = Block.box(1, 0, 1, 15, 16, 15);
    private static final VoxelShape WILD_SHAPE = Block.box(3, 1, 3, 13, 15, 13);
    private final boolean wild;

    public HiveBlock(Properties properties, boolean wild) {
        super(properties);
        this.wild = wild;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean isWild() {
        return wild;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return wild ? WILD_SHAPE : HIVE_SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HiveBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, net.minecraft.world.level.Level level,
                                               BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HiveBlockEntity hive)) return ItemInteractionResult.FAIL;
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (hive.honeyAmount() < 1000) return ItemInteractionResult.FAIL;
            if (!level.isClientSide()) {
                hive.tank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                ItemStack filled = new ItemStack(ModItems.HONEY_BOTTLE.get());
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                    if (stack.isEmpty()) player.setItemInHand(hand, filled);
                    else if (!player.getInventory().add(filled)) player.drop(filled, false);
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            if (!level.isClientSide() && !FluidUtil.interactWithFluidHandler(player, hand, hive.tank())) {
                return ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof HiveBlockEntity hive) {
            player.displayClientMessage(Component.translatable("message.animania.hive_status",
                    hive.honeyAmount(), HiveBlockEntity.CAPACITY, hive.nextHoney()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,
                                                                  BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.HIVE.get(), HiveBlockEntity::serverTick);
    }
}
