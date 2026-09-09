package com.animania.catsdogs.block.entity;

import com.animania.catsdogs.block.PetBowlBlock;
import com.animania.catsdogs.block.PetBowlContent;
import com.animania.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public final class PetBowlBlockEntity extends BlockEntity {
    private ItemStack food = ItemStack.EMPTY;
    private int water;
    private final IItemHandler automationItems = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? food.copy() : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || water > 0 || !PetBowlBlock.isBowlFood(stack)
                    || !food.isEmpty() && !ItemStack.isSameItemSameTags(food, stack)) return stack;
            int accepted = Math.min(stack.getCount(), 3 - food.getCount());
            if (accepted <= 0) return stack;
            if (!simulate) {
                if (food.isEmpty()) food = stack.copyWithCount(accepted); else food.grow(accepted);
                syncState();
            }
            return stack.copyWithCount(stack.getCount() - accepted);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || food.isEmpty()) return ItemStack.EMPTY;
            int removed = Math.min(amount, food.getCount());
            ItemStack result = food.copyWithCount(removed);
            if (!simulate) {
                food.shrink(removed);
                if (food.isEmpty()) food = ItemStack.EMPTY;
                syncState();
            }
            return result;
        }
        @Override public int getSlotLimit(int slot) { return slot == 0 ? 3 : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && PetBowlBlock.isBowlFood(stack); }
    };
    private final IFluidHandler automationFluids = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 && water > 0
                    ? new FluidStack(net.minecraft.world.level.material.Fluids.WATER, water) : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? 1000 : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER);
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !food.isEmpty() || !isFluidValid(0, resource)) return 0;
            int accepted = Math.min(resource.getAmount(), 1000 - water);
            if (action.execute() && accepted > 0) { water += accepted; syncState(); }
            return accepted;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            if (water <= 0 || maxDrain <= 0) return FluidStack.EMPTY;
            int drained = Math.min(maxDrain, water);
            FluidStack result = new FluidStack(net.minecraft.world.level.material.Fluids.WATER, drained);
            if (action.execute()) { water -= drained; syncState(); }
            return result;
        }
    };

    public PetBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PET_BOWL.get(), pos, state);
    }

    public ItemStack food() {
        return food;
    }

    public int water() {
        return water;
    }

    public IItemHandler automationItems() { return automationItems; }
    public IFluidHandler automationFluids() { return automationFluids; }

    public boolean addFood(ItemStack stack) {
        if (water > 0 || !PetBowlBlock.isBowlFood(stack) || !food.isEmpty() && !ItemStack.isSameItemSameTags(food, stack)
                || food.getCount() >= 3) return false;
        if (food.isEmpty()) food = stack.copyWithCount(1);
        else food.grow(1);
        syncState();
        return true;
    }

    public ItemStack removeFood() {
        if (food.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = food.copyWithCount(1);
        food.shrink(1);
        if (food.isEmpty()) food = ItemStack.EMPTY;
        syncState();
        return result;
    }

    public boolean fillWater() {
        if (!food.isEmpty() || water >= 1000) return false;
        water = 1000;
        syncState();
        return true;
    }

    public boolean drainWaterBucket() {
        if (water != 1000) return false;
        water = 0;
        syncState();
        return true;
    }

    public boolean consumeFood(Animal animal) {
        if (food.isEmpty() || !animal.isFood(food)) return false;
        if (level != null && !level.isClientSide()
                && com.animania.common.config.LegacyItemMatcher.isFarmersDogFood(food)) {
            net.minecraft.world.level.block.Block.popResource(level, worldPosition,
                    new ItemStack(net.minecraft.world.item.Items.BOWL));
        }
        food.shrink(1);
        if (food.isEmpty()) food = ItemStack.EMPTY;
        syncState();
        return true;
    }

    public boolean consumeWater() {
        return consumeWater(100);
    }

    public boolean consumeWater(int amount) {
        if (amount <= 0 || water < amount) return false;
        water -= amount;
        syncState();
        return true;
    }

    private void syncState() {
        setChanged();
        if (level == null || !level.getBlockState(worldPosition).is(getBlockState().getBlock())) return;
        PetBowlContent content = !food.isEmpty() ? PetBowlContent.FOOD : water > 0 ? PetBowlContent.WATER : PetBowlContent.EMPTY;
        int amount = !food.isEmpty() ? food.getCount()
                : water > 0 ? Math.max(1, (water + 333) / 334) : 0;
        level.setBlock(worldPosition, getBlockState().setValue(PetBowlBlock.CONTENT, content)
                .setValue(PetBowlBlock.LEVEL, amount), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!food.isEmpty()) tag.put("Food", food.save(new CompoundTag()));
        tag.putInt("StorageVersion", 2);
        tag.putInt("Water", water);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        food = tag.contains("Food") ? ItemStack.of(tag.getCompound("Food")) : ItemStack.EMPTY;
        int storedWater = tag.getInt("Water");
        water = !tag.contains("StorageVersion") && storedWater <= 3 ? storedWater * 334 : storedWater;
        water = Math.max(0, Math.min(1000, water));
    }
}
