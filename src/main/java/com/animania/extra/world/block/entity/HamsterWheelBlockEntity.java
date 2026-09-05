package com.animania.extra.world.block.entity;

import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.extra.world.block.HamsterWheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.item.ItemStack;
import com.animania.common.registry.ModItems;

import javax.annotation.Nullable;
import java.util.UUID;

public final class HamsterWheelBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    private final WheelEnergyStorage energy = new WheelEnergyStorage();
    @Nullable private UUID hamsterId;
    private int food;
    private int runTicks;
    private final IItemHandler items = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) {
            checkSlot(slot);
            return food <= 0 ? ItemStack.EMPTY : new ItemStack(ModItems.HAMSTER_FOOD.get(), food);
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            checkSlot(slot);
            if (stack.isEmpty() || !stack.is(ModItems.HAMSTER_FOOD.get())) return stack;
            int accepted = Math.min(stack.getCount(), 16 - food);
            if (accepted <= 0) return stack;
            if (!simulate) {
                food += accepted;
                setChanged();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(accepted);
            return remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            checkSlot(slot);
            int extracted = Math.min(Math.max(0, amount), food);
            if (extracted <= 0) return ItemStack.EMPTY;
            if (!simulate) {
                food -= extracted;
                setChanged();
            }
            return new ItemStack(ModItems.HAMSTER_FOOD.get(), extracted);
        }
        @Override public int getSlotLimit(int slot) { checkSlot(slot); return 16; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            checkSlot(slot);
            return stack.is(ModItems.HAMSTER_FOOD.get());
        }
        private void checkSlot(int slot) {
            if (slot != 0) throw new RuntimeException("Hamster wheel only has slot 0");
        }
    };

    public HamsterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAMSTER_WHEEL.get(), pos, state);
    }

    public EnergyStorage energy() {
        return energy;
    }

    public IItemHandler items() {
        return items;
    }

    public int capacity() { return energy.getMaxEnergyStored(); }

    public int food() {
        return food;
    }

    public boolean hasHamster() {
        return hamsterId != null;
    }

    public boolean addFood() {
        if (food >= 16) return false;
        food++;
        setChanged();
        return true;
    }

    public boolean insertHamster(AnimaniaRodent hamster) {
        if (hamsterId != null || hamster.kind() != AnimaniaRodent.Kind.HAMSTER || hamster.isInBall()) return false;
        hamster.stopRiding();
        hamster.setOrderedToSit(false);
        hamster.setNoAi(true);
        hamster.setDeltaMovement(0, 0, 0);
        hamsterId = hamster.getUUID();
        runTicks = 0;
        positionHamster(hamster);
        updateRunning(true);
        setChanged();
        return true;
    }

    public void releaseHamster() {
        AnimaniaRodent hamster = findHamster();
        if (hamster != null) {
            hamster.setNoAi(false);
            com.animania.common.entity.LegacyAnimalNeeds.setFed(hamster, false);
            hamster.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.1,
                    worldPosition.getZ() + 1.25);
        }
        hamsterId = null;
        runTicks = 0;
        updateRunning(false);
        setChanged();
    }

    @Nullable
    private AnimaniaRodent findHamster() {
        if (!(level instanceof ServerLevel server) || hamsterId == null) return null;
        Entity entity = server.getEntity(hamsterId);
        return entity instanceof AnimaniaRodent rodent && rodent.kind() == AnimaniaRodent.Kind.HAMSTER
                ? rodent : null;
    }

    private void positionHamster(AnimaniaRodent hamster) {
        Direction facing = getBlockState().getValue(HamsterWheelBlock.FACING);
        double sidewaysX = facing.getStepZ() * 0.08;
        double sidewaysZ = -facing.getStepX() * 0.08;
        hamster.setPos(worldPosition.getX() + 0.5 + sidewaysX,
                worldPosition.getY() + 0.28, worldPosition.getZ() + 0.5 + sidewaysZ);
        hamster.setYRot(facing.toYRot());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HamsterWheelBlockEntity wheel) {
        if (wheel.hamsterId == null) return;
        AnimaniaRodent hamster = wheel.findHamster();
        if (hamster == null || !hamster.isAlive()) {
            wheel.hamsterId = null;
            wheel.updateRunning(false);
            wheel.setChanged();
            return;
        }

        wheel.positionHamster(hamster);
        hamster.setDeltaMovement(0, 0, 0);
        int generation = LegacyConfig.HAMSTER_WHEEL_RF_GENERATION.get();
        wheel.energy.generate(generation);
        wheel.runTicks++;

        for (Direction direction : Direction.values()) {
            var receiver = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    pos.relative(direction), direction.getOpposite());
            if (receiver != null && receiver.canReceive()) {
                int accepted = receiver.receiveEnergy(Math.min(generation, wheel.energy.getEnergyStored()), false);
                wheel.energy.extractEnergy(accepted, false);
            }
        }

        if (wheel.runTicks >= LegacyConfig.HAMSTER_WHEEL_USE_TIME.get()) {
            wheel.runTicks = 0;
            if (wheel.food > 0) {
                wheel.food--;
                com.animania.common.entity.LegacyAnimalNeeds.feed(hamster, false, false);
            } else {
                wheel.releaseHamster();
                return;
            }
        }
        if (wheel.runTicks % 20 == 0) wheel.setChanged();
    }

    private void updateRunning(boolean running) {
        if (level != null && level.getBlockState(worldPosition).is(getBlockState().getBlock())
                && getBlockState().getValue(HamsterWheelBlock.RUNNING) != running) {
            level.setBlock(worldPosition, getBlockState().setValue(HamsterWheelBlock.RUNNING, running), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (hamsterId != null) tag.putUUID("Hamster", hamsterId);
        tag.putInt("Food", food);
        tag.putInt("RunTicks", runTicks);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hamsterId = tag.hasUUID("Hamster") ? tag.getUUID("Hamster") : null;
        food = Math.max(0, Math.min(16, tag.getInt("Food")));
        runTicks = Math.max(0, tag.getInt("RunTicks"));
        energy.setEnergy(tag.getInt("Energy"));
    }

    private static final class WheelEnergyStorage extends EnergyStorage {
        private WheelEnergyStorage() {
            super(LegacyConfig.HAMSTER_WHEEL_CAPACITY.get(), 0,
                    LegacyConfig.HAMSTER_WHEEL_RF_GENERATION.get() * 6);
        }

        private void generate(int amount) {
            energy = Math.min(capacity, energy + amount);
        }

        private void setEnergy(int amount) {
            energy = Math.max(0, Math.min(capacity, amount));
        }
    }
}
