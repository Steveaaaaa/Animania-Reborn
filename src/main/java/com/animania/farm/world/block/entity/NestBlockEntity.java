package com.animania.farm.world.block.entity;

import com.animania.common.registry.ModBlockEntities;
import com.animania.farm.world.block.NestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

/** Extraction-only inventory view used by hoppers and pipes, matching 1.12. */
public final class NestBlockEntity extends BlockEntity {
    private final IItemHandler items = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) {
            checkSlot(slot);
            return NestBlock.eggStack(getBlockState());
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            checkSlot(slot);
            return stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            checkSlot(slot);
            if (amount <= 0 || level == null) return ItemStack.EMPTY;
            ItemStack result = NestBlock.eggStack(getBlockState());
            if (result.isEmpty()) return ItemStack.EMPTY;
            result.setCount(Math.min(amount, result.getCount()));
            if (!simulate) NestBlock.consumeEggs(level, worldPosition, result.getCount());
            return result;
        }
        @Override public int getSlotLimit(int slot) { checkSlot(slot); return 3; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { checkSlot(slot); return false; }
        private void checkSlot(int slot) {
            if (slot != 0) throw new RuntimeException("Nest only has slot 0");
        }
    };

    public NestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEST.get(), pos, state);
    }

    public IItemHandler items() {
        return items;
    }
}
