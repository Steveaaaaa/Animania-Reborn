package com.animania.common.world.block.entity;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.world.block.SaltLickBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SaltLickBlockEntity extends BlockEntity {
    public static final int MAX_USES = 200;
    private int usesLeft = maxUses();

    public SaltLickBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SALT_LICK.get(), pos, state);
    }

    public int usesLeft() { return usesLeft; }
    public static int maxUses() { return LegacyConfig.SALT_LICK_MAX_USES.get(); }

    public void setDamage(int damage) {
        usesLeft = Math.max(1, maxUses() - damage * maxUses() / MAX_USES);
        syncWear();
    }

    public void use(LivingEntity entity) {
        if (level == null || level.isClientSide() || usesLeft <= 0) return;
        entity.heal(AnimaniaConfig.SALT_LICK_HEALING.get().floatValue());
        usesLeft--;
        if (usesLeft <= 0) level.destroyBlock(worldPosition, false);
        else {
            syncWear();
            setChanged();
        }
    }

    private void syncWear() {
        if (level == null) return;
        int maximum = maxUses();
        int wear = Math.min(7, (maximum - usesLeft) * 8 / maximum);
        BlockState state = getBlockState();
        if (state.hasProperty(SaltLickBlock.WEAR) && state.getValue(SaltLickBlock.WEAR) != wear) {
            level.setBlock(worldPosition, state.setValue(SaltLickBlock.WEAR, wear), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("UsesLeft", usesLeft);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        usesLeft = tag.contains("UsesLeft") ? Math.max(0, Math.min(maxUses(), tag.getInt("UsesLeft"))) : maxUses();
    }
}
