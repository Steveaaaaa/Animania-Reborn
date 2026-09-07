package com.animania.catsdogs.block;

import com.animania.catsdogs.block.entity.PetBowlBlockEntity;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PetBowlBlock extends BaseEntityBlock {
    public static final EnumProperty<PetBowlContent> CONTENT = EnumProperty.create("content", PetBowlContent.class);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 4, 12);

    public PetBowlBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CONTENT, PetBowlContent.EMPTY).setValue(LEVEL, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTENT, LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static boolean isBowlFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "petBowl");
    }

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl)) return InteractionResult.FAIL;
        if (isBowlFood(stack)) {
            if (!level.isClientSide() && bowl.addFood(stack) && !player.getAbilities().instabuild) stack.shrink(1);
            return state.getValue(CONTENT) == PetBowlContent.WATER || state.getValue(LEVEL) >= 3
                    ? InteractionResult.FAIL : InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (stack.is(Items.WATER_BUCKET)) {
            if (!bowl.food().isEmpty() || bowl.water() >= 1000) return InteractionResult.FAIL;
            if (!level.isClientSide() && bowl.fillWater() && !player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (stack.is(Items.BUCKET) && state.getValue(CONTENT) == PetBowlContent.WATER && state.getValue(LEVEL) == 3) {
            if (!level.isClientSide() && bowl.drainWaterBucket() && !player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (!bowl.food().isEmpty()) {
                ItemStack food = bowl.removeFood();
                if (!player.addItem(food)) player.drop(food, false);
            } else {
                String amount = bowl.water() > 0 ? bowl.water() + "/1000 mB" : "0";
                player.displayClientMessage(Component.translatable("message.animania.pet_bowl_status",
                        Component.translatable("pet_bowl.animania.content." + state.getValue(CONTENT).getSerializedName()),
                        amount), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static boolean consume(Level level, BlockPos pos, Animal animal, PetBowlContent expected) {
        if (!(level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl)) return false;
        return expected == PetBowlContent.FOOD ? bowl.consumeFood(animal)
                : expected == PetBowlContent.WATER && bowl.consumeWater();
    }

    public static boolean hasContent(Level level, BlockPos pos, Animal animal, PetBowlContent expected) {
        if (!(level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl)) return false;
        return expected == PetBowlContent.FOOD ? !bowl.food().isEmpty() && animal.isFood(bowl.food())
                : expected == PetBowlContent.WATER && bowl.water() > 0;
    }

    public static boolean hasWater(Level level, BlockPos pos, int amount) {
        return level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl && bowl.water() >= amount;
    }

    public static boolean consumeWater(Level level, BlockPos pos, int amount) {
        return level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl && bowl.consumeWater(amount);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl)) return 0;
        if (!bowl.food().isEmpty()) {
            return (int) Math.floor((double) bowl.food().getCount() / 3.0D * 14.0D) + 1;
        }
        return Math.min(15, bowl.water() / 66);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof PetBowlBlockEntity bowl && !bowl.food().isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, bowl.food().copy());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PetBowlBlockEntity(pos, state);
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
