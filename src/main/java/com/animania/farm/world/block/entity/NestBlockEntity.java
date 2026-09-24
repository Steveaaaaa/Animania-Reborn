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
    private final java.util.List<String> mothers = new java.util.ArrayList<>();
    private final java.util.List<String> fathers = new java.util.ArrayList<>();
    private final java.util.List<String> fatherBreeds = new java.util.ArrayList<>();
    private void alignParents() {
        int eggs = getBlockState().getValue(NestBlock.EGGS);
        while (mothers.size() > eggs) mothers.remove(0);
        while (mothers.size() < eggs) mothers.add("");
        while (fathers.size() > eggs) fathers.remove(0);
        while (fathers.size() < eggs) fathers.add("");
        while (fatherBreeds.size() > eggs) fatherBreeds.remove(0);
        while (fatherBreeds.size() < eggs) fatherBreeds.add("");
    }
    @Override public void setBlockState(BlockState state) {
        super.setBlockState(state);
        if (mothers != null) { alignParents(); setChanged(); }
    }
    public void rememberMother(net.minecraft.world.entity.animal.Animal mother) {
        com.animania.common.entity.HusbandryMood.caredFor(mother);
        alignParents();
        if (!mothers.isEmpty()) mothers.set(mothers.size() - 1, mother.getUUID().toString());
        if (!mothers.isEmpty() && com.animania.common.registry.ModAttachments.getData(mother, com.animania.common.registry.ModAttachments.FERTILIZED_TIMER) > 0) {
            String father = com.animania.common.registry.ModAttachments.getData(mother, com.animania.common.registry.ModAttachments.LAST_SIRE);
            fathers.set(fathers.size() - 1, father);
            com.animania.farm.world.block.NestBreed breed = getBlockState().getValue(NestBlock.BREED);
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                try {
                    var sire = server.getEntity(java.util.UUID.fromString(father));
                    if (sire instanceof com.animania.farm.chicken.AnimaniaChicken bird) breed = com.animania.farm.world.block.NestBreed.of(bird.breed());
                    if (sire instanceof com.animania.extra.peafowl.AnimaniaPeafowl bird) breed = com.animania.farm.world.block.NestBreed.of(bird.breed());
                } catch (IllegalArgumentException ignored) { }
            }
            fatherBreeds.set(fatherBreeds.size() - 1, breed.name());
        }
        setChanged();
    }
    public com.animania.farm.world.block.NestBreed fatherBreed() {
        alignParents();
        if (fathers.isEmpty() || fathers.get(0).isEmpty()) return com.animania.farm.world.block.NestBreed.EMPTY;
        try { return com.animania.farm.world.block.NestBreed.valueOf(fatherBreeds.get(0)); }
        catch (IllegalArgumentException ignored) { return com.animania.farm.world.block.NestBreed.EMPTY; }
    }
    public boolean belongsTo(net.minecraft.world.entity.animal.Animal mother) {
        alignParents();
        return mothers.contains(mother.getUUID().toString());
    }
    public void recordHatchling(net.minecraft.world.entity.animal.Animal chick) {
        com.animania.common.entity.HusbandryMood.caredFor(chick);
        alignParents();
        if (!mothers.isEmpty() && !mothers.get(0).isEmpty())
            com.animania.common.registry.ModAttachments.setData(chick, com.animania.common.registry.ModAttachments.PARENT, mothers.get(0));
        if (!fathers.isEmpty()) com.animania.common.registry.ModAttachments.setData(chick, com.animania.common.registry.ModAttachments.FATHER, fathers.get(0));
    }
    @Override protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        alignParents();
        net.minecraft.nbt.ListTag parents = new net.minecraft.nbt.ListTag();
        for (String mother : mothers) parents.add(net.minecraft.nbt.StringTag.valueOf(mother));
        tag.put("EggMothers", parents);
        net.minecraft.nbt.ListTag sires = new net.minecraft.nbt.ListTag(), breeds = new net.minecraft.nbt.ListTag();
        for (String father : fathers) sires.add(net.minecraft.nbt.StringTag.valueOf(father));
        for (String breed : fatherBreeds) breeds.add(net.minecraft.nbt.StringTag.valueOf(breed));
        tag.put("EggFathers", sires); tag.put("EggFatherBreeds", breeds);
    }
    @Override public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        mothers.clear(); fathers.clear(); fatherBreeds.clear();
        var parents = tag.getList("EggMothers", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < Math.min(3, parents.size()); i++) mothers.add(parents.getString(i));
        var sires = tag.getList("EggFathers", net.minecraft.nbt.Tag.TAG_STRING);
        var breeds = tag.getList("EggFatherBreeds", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < Math.min(3, sires.size()); i++) fathers.add(sires.getString(i));
        for (int i = 0; i < Math.min(3, breeds.size()); i++) fatherBreeds.add(breeds.getString(i));
        alignParents();
    }
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
