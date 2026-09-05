package com.animania.extra.world.block;

import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModItems;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.extra.world.block.entity.HamsterWheelBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public final class HamsterWheelBlock extends BaseEntityBlock {
    public static final MapCodec<HamsterWheelBlock> CODEC = simpleCodec(HamsterWheelBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");
    private static final VoxelShape SHAPE = Block.box(1, 0, 2, 15, 15, 14);

    public HamsterWheelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(RUNNING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, RUNNING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.HAMSTER_FOOD.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity wheel) || !wheel.addFood()) {
            return ItemInteractionResult.FAIL;
        }
        if (!level.isClientSide() && !player.getAbilities().instabuild) stack.shrink(1);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity wheel)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown() && wheel.hasHamster()) {
                wheel.releaseHamster();
            } else if (!wheel.hasHamster()) {
                AnimaniaRodent carried = null;
                for (Entity passenger : player.getPassengers()) {
                    if (passenger instanceof AnimaniaRodent rodent && rodent.kind() == AnimaniaRodent.Kind.HAMSTER) {
                        carried = rodent;
                        break;
                    }
                }
                if (carried == null || !carried.isTame() || !carried.isOwnedBy(player) || !wheel.insertHamster(carried)) {
                    player.displayClientMessage(Component.translatable("message.animania.wheel_need_hamster"), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("message.animania.wheel_status",
                        wheel.energy().getEnergyStored(), wheel.capacity(), wheel.food()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity wheel && wheel.hasHamster() ? 15 : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HamsterWheelBlockEntity wheel) {
            if (!level.isClientSide()) {
                wheel.releaseHamster();
                if (wheel.food() > 0) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, new ItemStack(ModItems.HAMSTER_FOOD.get(), wheel.food()));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HamsterWheelBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.HAMSTER_WHEEL.get(), HamsterWheelBlockEntity::serverTick);
    }
}
