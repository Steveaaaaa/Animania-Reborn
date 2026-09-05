package com.animania.common.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import com.animania.common.registry.ModItems;
import com.animania.common.world.block.entity.TroughBlockEntity;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public final class TroughBlock extends BaseEntityBlock {
    public static final MapCodec<TroughBlock> CODEC = simpleCodec(TroughBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<TroughPart> PART = EnumProperty.create("part", TroughPart.class);
    public static final EnumProperty<TroughContent> CONTENT = EnumProperty.create("content", TroughContent.class);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);
    private static final VoxelShape EAST_WEST_HALF = Block.box(0, 0, 4, 16, 5, 12);
    private static final VoxelShape NORTH_SOUTH_HALF = Block.box(4, 0, 0, 12, 5, 16);

    public TroughBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, TroughPart.MAIN)
                .setValue(CONTENT, TroughContent.EMPTY)
                .setValue(LEVEL, 0));
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
        builder.add(FACING, PART, CONTENT, LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return extensionDirection(state).getAxis() == Direction.Axis.X
                ? EAST_WEST_HALF : NORTH_SOUTH_HALF;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos extensionPos = context.getClickedPos().relative(facing.getClockWise());
        Level level = context.getLevel();
        return level.getBlockState(extensionPos).canBeReplaced(context)
                && level.getWorldBorder().isWithinBounds(extensionPos)
                ? defaultBlockState().setValue(FACING, facing)
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockPos extensionPos = pos.relative(extensionDirection(state));
            level.setBlock(extensionPos, state.setValue(PART, TroughPart.EXTENSION), 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == companionDirection(state)) {
            return neighborState.is(this)
                    && neighborState.getValue(FACING) == state.getValue(FACING)
                    && neighborState.getValue(PART) != state.getValue(PART)
                    ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(PART) == TroughPart.EXTENSION) {
            BlockPos mainPos = mainPos(state, pos);
            BlockState mainState = level.getBlockState(mainPos);
            if (isMatchingPart(mainState, state, TroughPart.MAIN)) {
                level.destroyBlock(mainPos, !player.isCreative(), player);
            }
        } else if (!level.isClientSide() && player.isCreative()) {
            BlockPos extensionPos = pos.relative(extensionDirection(state));
            BlockState extensionState = level.getBlockState(extensionPos);
            if (isMatchingPart(extensionState, state, TroughPart.EXTENSION)) {
                level.setBlock(extensionPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, extensionPos, Block.getId(extensionState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                Player player, InteractionHand hand, BlockHitResult hitResult) {
        pos = mainPos(state, pos);
        state = level.getBlockState(pos);
        if (!state.is(this) || state.getValue(PART) != TroughPart.MAIN
                || !(level.getBlockEntity(pos) instanceof TroughBlockEntity trough)) {
            return ItemInteractionResult.FAIL;
        }
        if (stack.is(Items.WATER_BUCKET)) {
            if (state.getValue(CONTENT) == TroughContent.FEED
                    || trough.water() >= 1000) {
                return ItemInteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                if (trough.fillWater() && !player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (stack.is(ModItems.SLOP_BUCKET.get())) {
            if (state.getValue(CONTENT) == TroughContent.WATER
                    || !trough.feed().isEmpty() || trough.slop() >= 1000) {
                return ItemInteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                if (trough.fillSlop() && !player.hasInfiniteMaterials()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (com.animania.common.config.LegacyItemMatcher.matches(stack, "trough")) {
            if (state.getValue(CONTENT) == TroughContent.WATER) {
                return ItemInteractionResult.FAIL;
            }
            int currentLevel = state.getValue(CONTENT) == TroughContent.FEED ? state.getValue(LEVEL) : 0;
            if (currentLevel >= 3) {
                return ItemInteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                if (trough.addFeed(stack, 1)) stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        pos = mainPos(state, pos);
        state = level.getBlockState(pos);
        if (!state.is(this) || state.getValue(PART) != TroughPart.MAIN) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            TroughBlockEntity trough = level.getBlockEntity(pos) instanceof TroughBlockEntity found ? found : null;
            String amount = trough == null ? "0"
                    : !trough.feed().isEmpty() ? trough.feed().getCount() + "/3"
                    : trough.slop() > 0 ? trough.slop() + "/1000 mB"
                    : trough.water() > 0 ? trough.water() + "/1000 mB" : "0";
            player.displayClientMessage(Component.translatable(
                    "message.animania.trough_status",
                    Component.translatable("trough.animania.content." + state.getValue(CONTENT).getSerializedName()),
                    amount), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static boolean consume(Level level, BlockPos pos, TroughContent expectedContent) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TroughBlock)) return false;
        BlockPos mainPos = mainPos(state, pos);
        return level.getBlockEntity(mainPos) instanceof TroughBlockEntity trough && trough.consume(expectedContent);
    }

    public static boolean hasContent(Level level, BlockPos pos, TroughContent expectedContent) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TroughBlock)) return false;
        BlockPos mainPos = mainPos(state, pos);
        BlockState mainState = level.getBlockState(mainPos);
        return mainState.is(state.getBlock())
                && mainState.getValue(PART) == TroughPart.MAIN
                && mainState.getValue(CONTENT) == expectedContent
                && mainState.getValue(LEVEL) > 0;
    }

    @Nullable
    private static TroughBlockEntity entity(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TroughBlock)) return null;
        return level.getBlockEntity(mainPos(state, pos)) instanceof TroughBlockEntity trough ? trough : null;
    }

    public static boolean hasFoodFor(Level level, BlockPos pos, net.minecraft.world.entity.animal.Animal animal) {
        TroughBlockEntity trough = entity(level, pos);
        return trough != null && trough.canFeed(animal);
    }

    public static boolean consumeFood(Level level, BlockPos pos, net.minecraft.world.entity.animal.Animal animal) {
        TroughBlockEntity trough = entity(level, pos);
        return trough != null && trough.consumeFood(animal);
    }

    public static boolean containsSlop(Level level, BlockPos pos) {
        TroughBlockEntity trough = entity(level, pos);
        return trough != null && trough.containsSlop();
    }

    public static boolean hasWater(Level level, BlockPos pos, int amount) {
        TroughBlockEntity trough = entity(level, pos);
        return trough != null && trough.water() >= amount;
    }

    public static boolean consumeWater(Level level, BlockPos pos, int amount) {
        TroughBlockEntity trough = entity(level, pos);
        return trough != null && trough.consumeWater(amount);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        TroughBlockEntity trough = entity(level, pos);
        if (trough == null) return 0;
        if (!trough.feed().isEmpty()) {
            return (int) Math.floor((double) trough.feed().getCount() / 3.0D * 14.0D) + 1;
        }
        int fluid = Math.max(trough.water(), trough.slop());
        return Math.min(15, fluid / 66);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getValue(PART) == TroughPart.MAIN && !state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof TroughBlockEntity trough
                && !trough.feed().isEmpty() && !trough.containsSlop()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                    trough.feed().copy());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == TroughPart.MAIN ? new TroughBlockEntity(pos, state) : null;
    }

    public static Direction extensionDirection(BlockState state) {
        return state.getValue(FACING).getClockWise();
    }

    private static Direction companionDirection(BlockState state) {
        Direction extension = extensionDirection(state);
        return state.getValue(PART) == TroughPart.MAIN ? extension : extension.getOpposite();
    }

    public static BlockPos mainPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == TroughPart.MAIN ? pos : pos.relative(companionDirection(state));
    }

    private static boolean isMatchingPart(BlockState candidate, BlockState reference, TroughPart expectedPart) {
        return candidate.is(reference.getBlock())
                && candidate.getValue(FACING) == reference.getValue(FACING)
                && candidate.getValue(PART) == expectedPart;
    }
}
