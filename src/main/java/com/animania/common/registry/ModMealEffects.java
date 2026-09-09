package com.animania.common.registry;

import com.animania.Animania;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModMealEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Animania.MOD_ID);
    public static final RegistryObject<MobEffect> STEADY_STEPS = EFFECTS.register("steady_steps", () -> new MealEffect(0xBC713E));
    public static final RegistryObject<MobEffect> COMPOSURE = EFFECTS.register("composure", () -> new MealEffect(0x629E96));
    public static final RegistryObject<MobEffect> COMPOSURE_READY = EFFECTS.register("composure_ready", () -> new MealEffect(0xE8C65A));

    private static final class MealEffect extends MobEffect {
        private MealEffect(int color) { super(MobEffectCategory.BENEFICIAL, color); }
    }

    public static void register(IEventBus bus) { EFFECTS.register(bus); }
    private ModMealEffects() { }
}
