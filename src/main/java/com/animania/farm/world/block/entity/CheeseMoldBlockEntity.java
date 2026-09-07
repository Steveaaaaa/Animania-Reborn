package com.animania.farm.world.block.entity;

import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModFluids;
import com.animania.common.registry.ModItems;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.farm.dairy.DairyStage;
import com.animania.farm.dairy.MilkType;
import com.animania.farm.world.block.CheeseMoldBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public final class CheeseMoldBlockEntity extends BlockEntity {
    /** Default retained for binary compatibility with optional integration plugins. */
    public static final int MATURITY_TIME = 24_000;
    private MilkType milk;
    private boolean water;
    private int fluidAmount;
    private int progress;
    private int outputCount;
    private final IFluidHandler fluids = new MoldFluidHandler();
    private final IItemHandler items = new MoldItemHandler();

    public CheeseMoldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEESE_MOLD.get(), pos, state);
    }

    public boolean fill(MilkType type) {
        if (milk != null || water || getBlockState().getValue(CheeseMoldBlock.STAGE) != DairyStage.EMPTY) return false;
        milk = type;
        fluidAmount = 1_000;
        progress = 0;
        updateStage(DairyStage.milk(type));
        return true;
    }

    public boolean fillWater() {
        if (LegacyConfig.DISABLE_SALT_CREATION.get() || milk != null || water
                || getBlockState().getValue(CheeseMoldBlock.STAGE) != DairyStage.EMPTY) return false;
        water = true;
        fluidAmount = 1_000;
        progress = 0;
        updateStage(DairyStage.WATER);
        return true;
    }

    public boolean isReady() {
        return outputCount > 0;
    }

    public MilkType milk() {
        return milk;
    }

    public boolean containsWater() {
        return water;
    }

    public static int maturityTime() {
        return LegacyConfig.CHEESE_MATURITY_TIME.get();
    }

    public int progress() {
        return progress;
    }

    public int fluidAmount() {
        return fluidAmount;
    }

    public IFluidHandler fluids() {
        return fluids;
    }

    public IItemHandler items() {
        return items;
    }

    public ItemStack outputStack() {
        if (!isReady()) return ItemStack.EMPTY;
        return water ? new ItemStack(ModItems.SALT.get(), outputCount)
                : new ItemStack(ModItems.cheeseWheel(milk).get(), outputCount);
    }

    public void clear() {
        milk = null;
        water = false;
        fluidAmount = 0;
        progress = 0;
        outputCount = 0;
        updateStage(DairyStage.EMPTY);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CheeseMoldBlockEntity mold) {
        if (mold.milk == null && !mold.water || mold.isReady() || mold.fluidAmount < 1_000) return;
        mold.progress++;
        if (mold.progress >= maturityTime()) {
            mold.fluidAmount = 0;
            mold.outputCount = mold.water ? LegacyConfig.SALT_CREATION_AMOUNT.get() : 1;
            mold.updateStage(mold.water ? DairyStage.SALT : DairyStage.cheese(mold.milk));
        }
        else if (mold.progress % 200 == 0) mold.setChanged();
    }

    private void updateStage(DairyStage stage) {
        setChanged();
        if (level != null) level.setBlock(worldPosition, getBlockState().setValue(CheeseMoldBlock.STAGE, stage), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (milk != null) tag.putString("MilkType", milk.getSerializedName());
        tag.putBoolean("Water", water);
        tag.putInt("FluidAmount", fluidAmount);
        tag.putInt("progress", progress);
        tag.putInt("OutputCount", outputCount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("progress");
        water = tag.getBoolean("Water");
        milk = null;
        if (tag.contains("MilkType")) {
            String id = tag.getString("MilkType");
            for (MilkType type : MilkType.values()) if (type.getSerializedName().equals(id)) milk = type;
        }
        fluidAmount = tag.contains("FluidAmount") ? Math.max(0, Math.min(1_000, tag.getInt("FluidAmount")))
                : milk != null || water ? 1_000 : 0;
        outputCount = Math.max(0, tag.getInt("OutputCount"));
    }

    private FluidStack storedFluid() {
        if (fluidAmount <= 0) return FluidStack.EMPTY;
        return water ? new FluidStack(Fluids.WATER, fluidAmount)
                : new FluidStack(ModFluids.milk(milk).source(), fluidAmount);
    }

    private boolean accepts(FluidStack stack) {
        if (stack.isEmpty() || isReady()) return false;
        if ((stack.getFluid() == Fluids.WATER)) return !LegacyConfig.DISABLE_SALT_CREATION.get() && milk == null;
        for (MilkType type : MilkType.values()) {
            if ((stack.getFluid() == ModFluids.milk(type).source())) return !water && (milk == null || milk == type);
        }
        return false;
    }

    private void syncFluidStage() {
        if (fluidAmount <= 0) {
            milk = null;
            water = false;
            progress = 0;
            updateStage(DairyStage.EMPTY);
        } else {
            updateStage(water ? DairyStage.WATER : DairyStage.milk(milk));
        }
    }

    private final class MoldFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { checkTank(tank); return storedFluid(); }
        @Override public int getTankCapacity(int tank) { checkTank(tank); return 1_000; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { checkTank(tank); return accepts(stack); }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!accepts(resource)) return 0;
            int accepted = Math.min(resource.getAmount(), 1_000 - fluidAmount);
            if (accepted <= 0) return 0;
            if (action.execute()) {
                if (fluidAmount == 0) {
                    water = (resource.getFluid() == Fluids.WATER);
                    milk = null;
                    if (!water) for (MilkType type : MilkType.values()) {
                        if ((resource.getFluid() == ModFluids.milk(type).source())) { milk = type; break; }
                    }
                    progress = 0;
                }
                fluidAmount += accepted;
                syncFluidStage();
            }
            return accepted;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack stored = storedFluid();
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack stored = storedFluid();
            if (stored.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
            int drained = Math.min(maxDrain, fluidAmount);
            FluidStack result = new FluidStack(stored, drained);
            if (action.execute()) {
                fluidAmount -= drained;
                if (fluidAmount < 1_000) progress = 0;
                syncFluidStage();
            }
            return result;
        }
        private void checkTank(int tank) {
            if (tank != 0) throw new RuntimeException("Cheese mold only has tank 0");
        }
    }

    private final class MoldItemHandler implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { checkSlot(slot); return outputStack(); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { checkSlot(slot); return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            checkSlot(slot);
            if (!isReady() || amount <= 0) return ItemStack.EMPTY;
            ItemStack result = outputStack();
            int extracted = Math.min(amount, outputCount);
            result.setCount(extracted);
            if (!simulate) {
                outputCount -= extracted;
                if (outputCount == 0) clear();
                else setChanged();
            }
            return result;
        }
        @Override public int getSlotLimit(int slot) { checkSlot(slot); return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { checkSlot(slot); return false; }
        private void checkSlot(int slot) {
            if (slot != 0) throw new RuntimeException("Cheese mold only has slot 0");
        }
    }
}
