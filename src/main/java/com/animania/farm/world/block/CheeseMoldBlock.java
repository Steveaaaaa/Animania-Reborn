package com.animania.farm.world.block;

import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModItems;
import com.animania.farm.dairy.DairyStage;
import com.animania.farm.dairy.MilkType;
import com.animania.farm.world.block.entity.CheeseMoldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public final class CheeseMoldBlock extends BaseEntityBlock {
    public static final EnumProperty<DairyStage> STAGE = EnumProperty.create("stage", DairyStage.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);

    public CheeseMoldBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, DairyStage.EMPTY));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        MilkType type = milkType(stack);
        boolean water = stack.is(Items.WATER_BUCKET);
        if (type == null && !water) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof CheeseMoldBlockEntity mold)
                || state.getValue(STAGE) != DairyStage.EMPTY) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            if (!(water ? mold.fillWater() : mold.fill(type))) return InteractionResult.FAIL;
            if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CheeseMoldBlockEntity mold)) return InteractionResult.PASS;
        if (mold.isReady()) {
            if (!level.isClientSide()) {
                ItemStack result = mold.outputStack();
                if (!player.addItem(result)) player.drop(result, false);
                mold.clear();
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide()) {
            int percent = Math.min(100, mold.progress() * 100 / CheeseMoldBlockEntity.maturityTime());
            player.displayClientMessage(Component.translatable("message.animania.cheese_progress", percent), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static MilkType milkType(ItemStack stack) {
        for (MilkType type : MilkType.values()) if (stack.is(ModItems.milkBucket(type).get())) return type;
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof CheeseMoldBlockEntity mold && mold.isReady()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    mold.outputStack());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CheeseMoldBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.CHEESE_MOLD.get(), CheeseMoldBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        InteractionResult result = useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
        if (result != InteractionResult.PASS) return result;
        return useWithoutItem(state, level, pos, player, hit);
    }
}
