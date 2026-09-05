package com.animania.farm.world.block.entity;

import com.animania.Animania;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.common.config.LegacyBiomeMatcher;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

/** Persistent 5000 mB honey producer used by both crafted and wild hives. */
public final class HiveBlockEntity extends BlockEntity {
    public static final int CAPACITY = 5_000;
    private final FluidTank tank;
    private int nextHoney = 400;

    public HiveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HIVE.get(), pos, state);
        tank = new FluidTank(CAPACITY, stack -> stack.is(ModFluids.HONEY.source())) {
            @Override protected void onContentsChanged() { setChangedAndSync(); }
        };
    }

    public FluidTank tank() { return tank; }
    public int honeyAmount() { return tank.getFluidAmount(); }
    public int nextHoney() { return nextHoney; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HiveBlockEntity hive) {
        if (!(level instanceof ServerLevel server)) return;
        if (state.is(ModBlocks.WILD_HIVE.get()) && !LegacyConfig.HIVE_SPAWNING.get()) {
            level.destroyBlock(pos, false);
            return;
        }
        if (--hive.nextHoney <= 0) {
            boolean wild = state.is(ModBlocks.WILD_HIVE.get());
            hive.nextHoney = (wild ? LegacyConfig.HIVE_WILD_HONEY_RATE.get()
                    : LegacyConfig.HIVE_PLAYERMADE_HONEY_RATE.get()) + level.random.nextInt(100);
            if (level.dimension() == Level.OVERWORLD && LegacyBiomeMatcher.matches(level.getBiome(pos),
                    LegacyConfig.BIOME_TYPES.get("hive").get())) {
                hive.tank.fill(new FluidStack(ModFluids.HONEY.source(),
                        AnimaniaConfig.HIVE_HONEY_PER_CYCLE.get()), IFluidHandler.FluidAction.EXECUTE);
            }
            hive.setChangedAndSync();
        }

        if (state.is(ModBlocks.WILD_HIVE.get()) && level.random.nextInt(10) == 0) {
            for (Player player : server.getEntitiesOfClass(Player.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(2.0))) {
                if (!player.isCreative() && level.random.nextInt(3) == 0) {
                    player.hurt(level.damageSources().generic(), AnimaniaConfig.WILD_HIVE_DAMAGE.get().floatValue());
                }
            }
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("NextHoney", nextHoney);
        tag.put("HoneyTank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        nextHoney = tag.contains("NextHoney") ? Math.max(1, tag.getInt("NextHoney")) : 400;
        if (tag.contains("HoneyTank")) tank.readFromNBT(registries, tag.getCompound("HoneyTank"));
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) loadAdditional(tag, registries);
    }
}
