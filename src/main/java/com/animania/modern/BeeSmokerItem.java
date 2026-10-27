package com.animania.modern;

import com.animania.farm.world.block.entity.HiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** One charcoal supplies eight puffs; the durability bar represents spent fuel. */
public final class BeeSmokerItem extends Item {
    public BeeSmokerItem() { super(new Properties().durability(9)); }
    private boolean puff(Level level, Player player, ItemStack stack, Vec3 center) {
        if (player.getCooldowns().isOnCooldown(this)) return false;
        if (level.isClientSide()) return true;
        if (stack.getDamageValue() >= 8 && !player.isCreative()) {
            ItemStack fuel = ItemStack.EMPTY;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack candidate = player.getInventory().getItem(i);
                if (candidate.is(Items.CHARCOAL)) { fuel = candidate; break; }
            }
            if (fuel.isEmpty()) { player.displayClientMessage(Component.translatable("message.animania.smoker_fuel"), true); return false; }
            fuel.shrink(1); stack.setDamageValue(0);
        }
        if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 1);
        player.getCooldowns().addCooldown(this, 40);
        ServerLevel server = (ServerLevel)level;
        BlockPos origin = BlockPos.containing(center);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2,-2,-2), origin.offset(2,2,2))) {
            if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof HiveBlockEntity hive)) continue;
            Vec3 target = Vec3.atCenterOf(pos);
            var hit = level.clip(new ClipContext(player.getEyePosition(), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos)) hive.colony().smoke();
        }
        for (DefensiveBeeSwarm swarm : level.getEntitiesOfClass(DefensiveBeeSwarm.class,
                new net.minecraft.world.phys.AABB(center, center).inflate(3))) {
            var hit = level.clip(new ClipContext(player.getEyePosition(), swarm.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS) swarm.calm();
        }
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y, center.z, 18, .6, .5, .6, .015);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, .35F, 1.6F);
        player.displayClientMessage(Component.translatable("message.animania.smoker_puffs", 8 - stack.getDamageValue()), true);
        return true;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        return puff(context.getLevel(), player, context.getItemInHand(), context.getClickLocation())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide()) : InteractionResult.FAIL;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return puff(level, player, stack, player.getEyePosition().add(player.getLookAngle().scale(2)))
                ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide()) : InteractionResultHolder.fail(stack);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.add(net.minecraft.network.chat.Component.translatable("tooltip.animania.smoker").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
