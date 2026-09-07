package com.animania.common.entity;

import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.AnimaniaSheep;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;

/**
 * Authoritative Forge representation of Animania 1.12's IFoodEating state.
 * The old mod used fed/watered booleans and countdown timers, not a generic
 * continuously decaying hunger bar.  HUNGER and THIRST are retained only as
 * derived 0/100 compatibility values for existing renderers and integrations.
 */
public final class LegacyAnimalNeeds {
    private LegacyAnimalNeeds() {
    }

    public static Profile profile(Animal animal) {
        if (animal instanceof AnimaniaCow) return new Profile(3, 2, 1.0D, false, FoodBlockMode.DEFAULT, true, 1, 1, 100);
        if (animal instanceof AnimaniaGoat) return new Profile(3, 2, 1.0D, false, FoodBlockMode.DEFAULT, true, 1, 1, 100);
        if (animal instanceof AnimaniaHorse) return new Profile(1, 1, 1.0D, false, FoodBlockMode.DEFAULT, true, 1, 1, 100);
        if (animal instanceof AnimaniaPig) return new Profile(3, 3, 1.0D, false, FoodBlockMode.DEFAULT, true, 1, 1, 100);
        if (animal instanceof AnimaniaSheep) return new Profile(3, 2, 1.0D, false, FoodBlockMode.DEFAULT, true, 1, 1, 100);
        if (animal instanceof AnimaniaChicken) return new Profile(3, 2, 1.0D, true, FoodBlockMode.SEEDS, false, 1, 1, 100);
        if (animal instanceof AnimaniaRabbit) return new Profile(3, 2, 1.4D, true, FoodBlockMode.RABBIT, true, 1, 1, 100);
        if (animal instanceof AnimaniaPeafowl) return new Profile(1, 1, 1.0D, true, FoodBlockMode.SEEDS, false, 2, 2, 100);
        if (animal instanceof AnimaniaCat) return new Profile(3, 1, 1.0D, true, FoodBlockMode.NONE, true, 1, 1, 100);
        if (animal instanceof AnimaniaDog) return new Profile(3, 1, 1.0D, true, FoodBlockMode.NONE, true, 1, 1, 100);
        if (animal instanceof AnimaniaRodent rodent) {
            if (rodent.kind() == AnimaniaRodent.Kind.HAMSTER) {
                return new Profile(3, 3, 1.0D, true, FoodBlockMode.NONE, false, 1, 4, 200);
            }
            if (rodent.kind().isFerret()) {
                return new Profile(3, 1, 1.0D, true, FoodBlockMode.NONE, true, 1, 2, 200);
            }
            return new Profile(4, 2, 1.0D, true, FoodBlockMode.NONE, true, 1, 2, 200);
        }
        return null;
    }

    public static void tick(Animal animal) {
        Profile profile = profile(animal);
        if (profile == null || animal.level().isClientSide()) return;
        initialize(animal, profile);

        if (animal.isLeashed() && !isInteracted(animal)) setInteracted(animal, true);

        if (LegacyConfig.AMBIANCE_MODE.get()) {
            setFed(animal, true);
            setWatered(animal, true);
        } else {
            int fedTimer = ModAttachments.getData(animal, ModAttachments.FED_TIMER);
            if (fedTimer > -1 && (!LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get() || isInteracted(animal))) {
                fedTimer--;
                ModAttachments.setData(animal, ModAttachments.FED_TIMER, fedTimer);
                if (fedTimer == 0) setFed(animal, false);
            }

            int wateredTimer = ModAttachments.getData(animal, ModAttachments.WATERED_TIMER);
            if (wateredTimer > -1) {
                wateredTimer--;
                ModAttachments.setData(animal, ModAttachments.WATERED_TIMER, wateredTimer);
                if (wateredTimer == 0
                        && (!LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get() || isInteracted(animal))) {
                    setWatered(animal, false);
                }
            }
        }

        boolean fed = isFed(animal);
        boolean watered = isWatered(animal);
        if (!fed && !watered) {
            animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2, 1, false, false));
            if (LegacyConfig.ANIMALS_STARVE.get()) {
                int damageTimer = ModAttachments.getData(animal, ModAttachments.STARVATION_TIMER);
                if (damageTimer >= LegacyConfig.STARVATION_TIMER.get()) {
                    animal.hurt(animal.damageSources().starve(), 4.0F);
                    damageTimer = 0;
                }
                if (!ModAttachments.getData(animal, ModAttachments.SLEEPING)) damageTimer++;
                ModAttachments.setData(animal, ModAttachments.STARVATION_TIMER, damageTimer);
            }
        } else if (!fed || !watered) {
            animal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2, 0, false, false));
        }

        int unhappyTimer = ModAttachments.getData(animal, ModAttachments.UNHAPPY_TIMER);
        if (unhappyTimer > -1 && --unhappyTimer == 0) {
            unhappyTimer = 60;
            if (!fed && !watered && !ModAttachments.getData(animal, ModAttachments.SLEEPING)
                    && LegacyConfig.SHOW_UNHAPPY_PARTICLES.get()
                    && (!LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get() || isInteracted(animal))
                    && animal.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SMOKE,
                        animal.getX() + animal.getRandom().nextFloat() * animal.getBbWidth() - animal.getBbWidth(),
                        animal.getY() + 1.5D + animal.getRandom().nextFloat() * animal.getBbHeight(),
                        animal.getZ() + animal.getRandom().nextFloat() * animal.getBbWidth() - animal.getBbWidth(),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        ModAttachments.setData(animal, ModAttachments.UNHAPPY_TIMER, unhappyTimer);
        syncCompatibilityValues(animal);
    }

    private static void initialize(Animal animal, Profile profile) {
        if (ModAttachments.getData(animal, ModAttachments.NEEDS_INITIALIZED)) return;
        ModAttachments.setData(animal, ModAttachments.NEEDS_INITIALIZED, true);
        ModAttachments.setData(animal, ModAttachments.FED, true);
        ModAttachments.setData(animal, ModAttachments.WATERED, true);
        ModAttachments.setData(animal, ModAttachments.HAND_FED, false);
        ModAttachments.setData(animal, ModAttachments.FED_TIMER,
                LegacyConfig.FEED_TIMER.get() * profile.initialFeedMultiplier()
                        + animal.getRandom().nextInt(100));
        ModAttachments.setData(animal, ModAttachments.WATERED_TIMER,
                LegacyConfig.WATER_TIMER.get() * profile.initialWaterMultiplier()
                        + animal.getRandom().nextInt(profile.initialWaterRandomBound()));
        ModAttachments.setData(animal, ModAttachments.STARVATION_TIMER, 0);
        ModAttachments.setData(animal, ModAttachments.UNHAPPY_TIMER, 60);
        syncCompatibilityValues(animal);
    }

    public static boolean isFed(Animal animal) {
        return ModAttachments.getData(animal, ModAttachments.FED);
    }

    public static boolean isWatered(Animal animal) {
        return ModAttachments.getData(animal, ModAttachments.WATERED);
    }

    public static boolean isInteracted(Animal animal) {
        return ModAttachments.getData(animal, ModAttachments.INTERACTED);
    }

    public static void setInteracted(Animal animal, boolean interacted) {
        ModAttachments.setData(animal, ModAttachments.INTERACTED, interacted);
    }

    public static void setFed(Animal animal, boolean fed) {
        if (fed) {
            ModAttachments.setData(animal, ModAttachments.FED_TIMER,
                    LegacyConfig.FEED_TIMER.get() + animal.getRandom().nextInt(100));
        }
        ModAttachments.setData(animal, ModAttachments.FED, fed);
        ModAttachments.setData(animal, ModAttachments.HUNGER, fed ? ModAttachments.MAX_NEED : 0);
    }

    public static void setWatered(Animal animal, boolean watered) {
        if (watered) {
            ModAttachments.setData(animal, ModAttachments.WATERED_TIMER,
                    LegacyConfig.WATER_TIMER.get() + animal.getRandom().nextInt(100));
        }
        ModAttachments.setData(animal, ModAttachments.WATERED, watered);
        ModAttachments.setData(animal, ModAttachments.THIRST, watered ? ModAttachments.MAX_NEED : 0);
    }

    public static void feed(Animal animal, boolean handFed, boolean slop) {
        setFed(animal, true);
        if (slop && animal instanceof AnimaniaPig) {
            ModAttachments.setData(animal, ModAttachments.FED_TIMER,
                    LegacyConfig.FEED_TIMER.get() * 2 + animal.getRandom().nextInt(100));
        }
        if (handFed && animal instanceof AnimaniaRodent rodent) rodent.storeHamsterFood();
        if (handFed) ModAttachments.setData(animal, ModAttachments.HAND_FED, true);
        if (handFed) setInteracted(animal, true);
    }

    public static void water(Animal animal) {
        setWatered(animal, true);
        setInteracted(animal, true);
    }

    /** Preserve the complete legacy husbandry state when a child entity is replaced by its adult entity. */
    public static void copyState(Animal from, Animal to) {
        ModAttachments.setData(to, ModAttachments.NEEDS_INITIALIZED, ModAttachments.getData(from, ModAttachments.NEEDS_INITIALIZED));
        ModAttachments.setData(to, ModAttachments.FED, ModAttachments.getData(from, ModAttachments.FED));
        ModAttachments.setData(to, ModAttachments.WATERED, ModAttachments.getData(from, ModAttachments.WATERED));
        ModAttachments.setData(to, ModAttachments.HAND_FED, ModAttachments.getData(from, ModAttachments.HAND_FED));
        ModAttachments.setData(to, ModAttachments.INTERACTED, ModAttachments.getData(from, ModAttachments.INTERACTED));
        ModAttachments.setData(to, ModAttachments.FED_TIMER, ModAttachments.getData(from, ModAttachments.FED_TIMER));
        ModAttachments.setData(to, ModAttachments.WATERED_TIMER, ModAttachments.getData(from, ModAttachments.WATERED_TIMER));
        ModAttachments.setData(to, ModAttachments.STARVATION_TIMER, ModAttachments.getData(from, ModAttachments.STARVATION_TIMER));
        ModAttachments.setData(to, ModAttachments.UNHAPPY_TIMER, ModAttachments.getData(from, ModAttachments.UNHAPPY_TIMER));
        ModAttachments.setData(to, ModAttachments.HUNGER, ModAttachments.getData(from, ModAttachments.HUNGER));
        ModAttachments.setData(to, ModAttachments.THIRST, ModAttachments.getData(from, ModAttachments.THIRST));
        ModAttachments.setData(to, ModAttachments.LAST_MATE, ModAttachments.getData(from, ModAttachments.LAST_MATE));
        ModAttachments.setData(to, ModAttachments.PARENT, ModAttachments.getData(from, ModAttachments.PARENT));
    }

    private static void syncCompatibilityValues(Animal animal) {
        ModAttachments.setData(animal, ModAttachments.HUNGER, isFed(animal) ? ModAttachments.MAX_NEED : 0);
        ModAttachments.setData(animal, ModAttachments.THIRST, isWatered(animal) ? ModAttachments.MAX_NEED : 0);
    }

    public enum FoodBlockMode {
        NONE, DEFAULT, SEEDS, RABBIT
    }

    public record Profile(int foodPriority, int waterPriority, double speed, boolean halfWater,
                          FoodBlockMode foodBlockMode, boolean automaticEatAnimation,
                          int initialFeedMultiplier, int initialWaterMultiplier,
                          int initialWaterRandomBound) {
        public boolean eatBlocks() {
            return foodBlockMode != FoodBlockMode.NONE;
        }
    }
}
