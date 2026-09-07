package com.animania.common.world.block.entity;

import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModItems;
import com.animania.common.world.block.TroughBlock;
import com.animania.common.world.block.TroughContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/** Stores the exact feed in a trough so overlays and saved worlds retain its identity. */
public final class TroughBlockEntity extends BlockEntity {
    private ItemStack feed = ItemStack.EMPTY;
    private int water;
    private int slop;
    private final IItemHandler automationItems = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? feed.copy() : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || water > 0 || slop > 0
                    || !com.animania.common.config.LegacyItemMatcher.matches(stack, "trough")
                    || !feed.isEmpty() && !ItemStack.isSameItemSameTags(feed, stack)) return stack;
            int accepted = Math.min(stack.getCount(), 3 - feed.getCount());
            if (accepted <= 0) return stack;
            if (!simulate) addFeed(stack, accepted);
            return stack.copyWithCount(stack.getCount() - accepted);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || feed.isEmpty()) return ItemStack.EMPTY;
            int removed = Math.min(amount, feed.getCount());
            ItemStack result = feed.copyWithCount(removed);
            if (!simulate) {
                feed.shrink(removed);
                if (feed.isEmpty()) feed = ItemStack.EMPTY;
                syncState();
            }
            return result;
        }
        @Override public int getSlotLimit(int slot) { return slot == 0 ? 3 : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && com.animania.common.config.LegacyItemMatcher.matches(stack, "trough");
        }
    };
    private final IFluidHandler automationFluids = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) {
            if (tank != 0) return FluidStack.EMPTY;
            if (water > 0) return new FluidStack(net.minecraft.world.level.material.Fluids.WATER, water);
            if (slop > 0) return new FluidStack(com.animania.common.registry.ModFluids.SLOP.source(), slop);
            return FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? 1000 : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && (stack.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)
                    || stack.getFluid().isSame(com.animania.common.registry.ModFluids.SLOP.source()));
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !feed.isEmpty() || !isFluidValid(0, resource)) return 0;
            boolean fillingWater = resource.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER);
            if (fillingWater && slop > 0 || !fillingWater && water > 0) return 0;
            int stored = fillingWater ? water : slop;
            int accepted = Math.min(resource.getAmount(), 1000 - stored);
            if (action.execute() && accepted > 0) {
                if (fillingWater) water += accepted; else slop += accepted;
                syncState();
            }
            return accepted;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack stored = getFluidInTank(0);
            if (resource.isEmpty() || stored.isEmpty()
                    || !resource.isFluidEqual(stored)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack stored = getFluidInTank(0);
            if (stored.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
            int drained = Math.min(maxDrain, stored.getAmount());
            FluidStack result = new FluidStack(stored, drained);
            if (action.execute()) {
                if (water > 0) water -= drained; else slop -= drained;
                syncState();
            }
            return result;
        }
    };

    public TroughBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TROUGH.get(), pos, state);
    }

    public ItemStack feed() {
        return feed;
    }

    public int water() {
        return water;
    }

    public int slop() {
        return slop;
    }

    public IItemHandler automationItems() { return automationItems; }
    public IFluidHandler automationFluids() { return automationFluids; }

    public boolean addFeed(ItemStack stack, int portions) {
        if (water > 0 || slop > 0 || portions <= 0
                || !feed.isEmpty() && !ItemStack.isSameItemSameTags(feed, stack)
                || feed.getCount() >= 3) return false;
        int added = Math.min(portions, 3 - feed.getCount());
        if (feed.isEmpty()) feed = stack.copyWithCount(added);
        else feed.grow(added);
        syncState();
        return true;
    }

    public boolean fillWater() {
        if (!feed.isEmpty() || slop > 0 || water >= 1000) return false;
        water = 1000;
        syncState();
        return true;
    }

    public boolean fillSlop() {
        if (!feed.isEmpty() || water > 0 || slop >= 1000) return false;
        slop = 1000;
        syncState();
        return true;
    }

    public boolean consume(TroughContent expected) {
        if (expected == TroughContent.FEED) {
            if (!feed.isEmpty()) {
                feed.shrink(1);
                if (feed.isEmpty()) feed = ItemStack.EMPTY;
            } else if (slop >= 100) {
                slop -= 100;
            } else return false;
        } else if (expected == TroughContent.WATER) {
            if (water < 100) return false;
            water -= 100;
        } else {
            return false;
        }
        syncState();
        return true;
    }

    public boolean containsSlop() {
        return slop > 0;
    }

    public boolean canFeed(Animal animal) {
        return !feed.isEmpty() && animal.isFood(feed)
                || animal instanceof com.animania.farm.livestock.AnimaniaPig && slop >= 100;
    }

    public boolean consumeFood(Animal animal) {
        if (!canFeed(animal)) return false;
        if (!feed.isEmpty()) {
            feed.shrink(1);
            if (feed.isEmpty()) feed = ItemStack.EMPTY;
        } else {
            slop -= 100;
        }
        syncState();
        return true;
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
        TroughContent content = !feed.isEmpty() || slop > 0 ? TroughContent.FEED
                : water > 0 ? TroughContent.WATER : TroughContent.EMPTY;
        int amount = !feed.isEmpty() ? feed.getCount()
                : slop > 0 ? Math.max(1, (slop + 249) / 250)
                : water > 0 ? Math.max(1, (water + 249) / 250) : 0;
        BlockState updated = getBlockState().setValue(TroughBlock.CONTENT, content)
                .setValue(TroughBlock.LEVEL, amount);
        level.setBlock(worldPosition, updated, 3);
        if (!level.isClientSide()) level.sendBlockUpdated(worldPosition, updated, updated, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!feed.isEmpty()) tag.put("Feed", feed.save(new CompoundTag()));
        tag.putInt("StorageVersion", 2);
        tag.putInt("Water", water);
        tag.putInt("Slop", slop);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        feed = tag.contains("Feed") ? ItemStack.of(tag.getCompound("Feed")) : ItemStack.EMPTY;
        if (feed.is(ModItems.SLOP_BUCKET.get())) {
            slop = Math.min(1000, feed.getCount() * 250);
            feed = ItemStack.EMPTY;
        } else {
            if (feed.getCount() > 3) feed.setCount(3);
            slop = Math.max(0, Math.min(1000, tag.getInt("Slop")));
        }
        int storedWater = tag.getInt("Water");
        // Migrate the earlier 0-4 portion representation without affecting
        // native 1.12 semantics for newly written data.
        water = !tag.contains("StorageVersion") && storedWater <= 4 ? storedWater * 250 : storedWater;
        water = Math.max(0, Math.min(1000, water));
        if (!feed.isEmpty() || slop > 0) water = 0;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        var direction = com.animania.common.world.block.TroughBlock.extensionDirection(getBlockState());
        return new net.minecraft.world.phys.AABB(getBlockPos()).expandTowards(direction.getStepX(), 0, direction.getStepZ()).inflate(0.01);
    }
}
