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
            com.animania.common.registry.ModItems.AXOLOTL_BUCKETS.values().forEach(bucket ->
                    DispenserBlock.registerBehavior(bucket.get(), new DefaultDispenseItemBehavior() {
                        @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
                            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                            var item = (net.minecraft.world.item.MobBucketItem) stack.getItem();
                            if (item.emptyContents(null, source.getLevel(), pos, null)) {
                                item.checkExtraContent(null, source.getLevel(), stack, pos);
                                return new ItemStack(Items.BUCKET);
                            }
                            return super.execute(source, stack);
                        }
                    }));
            DispenserBlock.registerBehavior(com.animania.common.registry.ModItems.BLUE_EGG.get(),
                    new DefaultDispenseItemBehavior() {
                        @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
                            if (!LegacyConfig.ALLOW_EGG_THROWING.get()) return super.execute(source, stack);
                            var egg = com.animania.common.registry.ModEntities.BLUE_EGG.get().create(source.getLevel());
                            if (egg == null) return stack;
                            var direction = source.getBlockState().getValue(DispenserBlock.FACING);
                            var pos = DispenserBlock.getDispensePosition(source);
                            egg.setPos(pos.x(), pos.y(), pos.z());
                            egg.setItem(stack);
                            egg.shoot(direction.getStepX(), direction.getStepY() + 0.1, direction.getStepZ(), 1.1F, 6.0F);
                            if (source.getLevel().addFreshEntity(egg)) stack.shrink(1);
                            return stack;
                        }
                    });
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
