package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Interaction switches that were global event handlers in the 1.12 release. */
@EventBusSubscriber(modid = Animania.MOD_ID)
public final class LegacyInteractionHandler {
    private LegacyInteractionHandler() {}

    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!LegacyConfig.ALLOW_EGG_THROWING.get() && stack.is(Items.EGG)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (LegacyConfig.EAT_FOOD_ANYTIME.get() && stack.has(DataComponents.FOOD)
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(Animania.MOD_ID)
                && !event.getEntity().canEat(false)) {
            event.getEntity().startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        boolean seed = stack.is(Items.WHEAT_SEEDS) || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Items.MELON_SEEDS) || stack.is(Items.BEETROOT_SEEDS);
        if (!seed || event.getFace() != Direction.UP
                || LegacyConfig.SHIFT_SEED_PLACEMENT.get() && !event.getEntity().isShiftKeyDown()) return;
        var level = event.getLevel();
        BlockPos support = event.getPos();
        BlockPos place = support.above();
        var seedState = ModBlocks.SEEDS.get().defaultBlockState();
        if (level.getBlockState(support).is(Blocks.FARMLAND) || !level.getBlockState(place).canBeReplaced()
                || !seedState.canSurvive(level, place)) return;
        if (!level.isClientSide()) {
            level.setBlock(place, seedState, 3);
            if (!event.getEntity().hasInfiniteMaterials()) stack.shrink(1);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        event.setCanceled(true);
    }
}
