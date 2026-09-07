package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Restores the configurable 1.12 dispenser behavior for loose seed ground cover. */
@EventBusSubscriber(modid = Animania.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class LegacyDispenserHandler {
    private LegacyDispenserHandler() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            register(Items.WHEAT_SEEDS);
            register(Items.PUMPKIN_SEEDS);
            register(Items.MELON_SEEDS);
            register(Items.BEETROOT_SEEDS);
        });
    }

    private static void register(Item item) {
        DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos place = source.getPos().relative(facing);
                var level = source.getLevel();
                var state = ModBlocks.SEEDS.get().defaultBlockState();
                if (LegacyConfig.ALLOW_SEED_DISPENSER_PLACEMENT.get()
                        && level.getBlockState(place).canBeReplaced() && state.canSurvive(level, place)) {
                    level.setBlock(place, state, 3);
                    stack.shrink(1);
                    return stack;
                }
                return super.execute(source, stack);
            }
        });
    }
}
