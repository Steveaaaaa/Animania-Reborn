package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModMealEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class MealEffectHandler {
    private static final Map<ServerPlayer, Steps> STEPS = new WeakHashMap<>();
    private static final Map<ServerPlayer, Integer> QUIET_TICKS = new WeakHashMap<>();
    private static final int PREPARE_TICKS = 200;

    public static void onEaten(Item item, LivingEntity eater) {
        if (eater.level().isClientSide() || !LegacyConfig.FOODS_GIVE_BONUS_EFFECTS.get()) return;
        if (item == ModItems.CHEESE_SANDWICH.get()) {
            nourish(eater, 1200);
            eater.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
        } else if (item == ModItems.TRUFFLE_RISOTTO.get()) {
            nourish(eater, 3600);
            eater.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1800, 0));
        } else if (item == ModItems.THREE_CHEESE_PASTA.get()) {
            eater.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
            eater.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1));
            eater.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
        } else if (eater instanceof ServerPlayer player) {
            if (item == ModItems.CHEVON_STEW.get()) {
                player.addEffect(new MobEffectInstance(ModMealEffects.STEADY_STEPS.get(), 3600, 0));
                STEPS.put(player, new Steps(player));
                message(player, "steps_start");
            } else if (item == ModItems.PEACOCK_PILAF.get()) {
                player.addEffect(new MobEffectInstance(ModMealEffects.COMPOSURE.get(), 4800, 0));
                disturb(player);
                message(player, "composure_start");
            }
        }
    }

    private static void nourish(LivingEntity eater, int ticks) {
        BuiltInRegistries.MOB_EFFECT.getOptional(new ResourceLocation("farmersdelight", "nourishment"))
                .ifPresent(effect -> eater.addEffect(new MobEffectInstance(effect, ticks, 0)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) tickPlayer(player);
    }

    private static void tickPlayer(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || !LegacyConfig.FOODS_GIVE_BONUS_EFFECTS.get()) {
            STEPS.remove(player);
            QUIET_TICKS.remove(player);
            player.removeEffect(ModMealEffects.STEADY_STEPS.get());
            player.removeEffect(ModMealEffects.COMPOSURE.get());
            player.removeEffect(ModMealEffects.COMPOSURE_READY.get());
            return;
        }
        if (player.hasEffect(ModMealEffects.STEADY_STEPS.get())) {
            Steps steps = STEPS.computeIfAbsent(player, Steps::new);
            Vec3 now = player.position();
            double dy = now.y - steps.previous.y;
            double horizontal = now.subtract(steps.previous).horizontalDistanceSqr();
            boolean walking = player.onGround() && steps.grounded && !player.isPassenger()
                    && !player.getAbilities().flying && !player.isFallFlying() && !player.isInWaterOrBubble()
                    && !player.onClimbable() && !player.isSprinting();
            // Both samples must be grounded: jumping, swimming and climbing ladders do not charge steps.
            if (walking && dy > 0 && dy <= 0.65 && horizontal > 0.0001 && horizontal < 1.0) {
                steps.ascent += dy;
                if (steps.ascent >= 1 && steps.charges < 3) {
                    steps.ascent -= 1;
                    steps.charges++;
                    message(player, "steps_charge", steps.charges);
                }
            }
            if (steps.charges == 3) steps.ascent = 0;
            steps.previous = now;
            steps.grounded = player.onGround();
        } else {
            STEPS.remove(player);
        }
        if (player.hasEffect(ModMealEffects.COMPOSURE.get())) {
            int quiet = Math.min(PREPARE_TICKS, QUIET_TICKS.getOrDefault(player, 0) + 1);
            QUIET_TICKS.put(player, quiet);
            if (quiet == PREPARE_TICKS && !player.hasEffect(ModMealEffects.COMPOSURE_READY.get())) {
                int remaining = player.getEffect(ModMealEffects.COMPOSURE.get()).getDuration();
                player.addEffect(new MobEffectInstance(ModMealEffects.COMPOSURE_READY.get(), remaining, 0));
                message(player, "composure_ready");
            }
        } else {
            QUIET_TICKS.remove(player);
            player.removeEffect(ModMealEffects.COMPOSURE_READY.get());
        }
    }

    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) disturb(player);
    }

    @SubscribeEvent
    public static void projectile(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && (event.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow
                    || event.getEntity() instanceof net.minecraft.world.entity.projectile.ThrowableProjectile)
                && event.getEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) disturb(player);
    }

    @SubscribeEvent
    public static void damage(LivingDamageEvent event) {
        event.setAmount(reduceDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }

    private static float reduceDamage(LivingEntity target, DamageSource source, float amount) {
        if (target.level().isClientSide() || amount <= 0 || !LegacyConfig.FOODS_GIVE_BONUS_EFFECTS.get()) return amount;
        if (source.getEntity() instanceof ServerPlayer attacker) disturb(attacker);
        if (!(target instanceof ServerPlayer player)) return amount;
        if (source.is(DamageTypes.FALL) && player.hasEffect(ModMealEffects.STEADY_STEPS.get())) {
            Steps steps = STEPS.get(player);
            if (steps != null && steps.charges > 0) {
                amount -= Math.min(amount * 0.5F, steps.charges * 2.0F);
                steps.charges = 0;
                steps.ascent = 0;
                message(player, "steps_used");
            }
        }
        if (player.hasEffect(ModMealEffects.COMPOSURE.get()) && player.hasEffect(ModMealEffects.COMPOSURE_READY.get())
                && source.getEntity() instanceof LivingEntity && source.getEntity() != player
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount -= Math.min(amount * 0.4F, 6.0F);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
            message(player, "composure_used");
        }
        disturb(player);
        return amount;
    }

    private static void disturb(ServerPlayer player) {
        if (player.hasEffect(ModMealEffects.COMPOSURE.get())) QUIET_TICKS.put(player, 0);
        player.removeEffect(ModMealEffects.COMPOSURE_READY.get());
    }

    private static void message(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.animania.meal." + key, args), true);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        String key = item == ModItems.CHEESE_SANDWICH.get() ? "cheese_sandwich"
                : item == ModItems.TRUFFLE_RISOTTO.get() ? "truffle_risotto"
                : item == ModItems.CHEVON_STEW.get() ? "chevon_stew"
                : item == ModItems.PEACOCK_PILAF.get() ? "peacock_pilaf"
                : item == ModItems.THREE_CHEESE_PASTA.get() ? "three_cheese_pasta" : null;
        if (key != null) {
            event.getToolTip().add(Component.translatable("tooltip.animania.meal." + key).withStyle(ChatFormatting.BLUE));
            if (key.equals("chevon_stew") || key.equals("peacock_pilaf")) {
                event.getToolTip().add(Component.translatable("tooltip.animania.meal." + key + ".detail").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static final class Steps {
        private Vec3 previous;
        private boolean grounded;
        private double ascent;
        private int charges;
        private Steps(ServerPlayer player) {
            previous = player.position();
            grounded = player.onGround();
        }
    }

    private MealEffectHandler() { }
}
