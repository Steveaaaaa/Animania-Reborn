package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.Map;
import java.util.WeakHashMap;

/** One cosmetic update per client tick, shared by normal, blink and Iris shadow passes. */
@EventBusSubscriber(modid = Animania.MOD_ID, value = Dist.CLIENT)
public final class LegacySleepAnimation {
    private static final Map<Entity, State> STATES = new WeakHashMap<>();
    private static final class State {
        boolean sleeping;
        float timer, previousTimer, pet, previousPet;
    }
    private static State state(Entity entity) {
        State state = STATES.computeIfAbsent(entity, ignored -> new State());
        boolean sleeping = entity.getData(ModAttachments.SLEEPING);
        if (sleeping != state.sleeping) {
            state.sleeping = sleeping;
            state.timer = state.previousTimer = 0;
            state.pet = state.previousPet = sleeping ? 0 : 10;
        }
        return state;
    }
    @SubscribeEvent public static void tick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide() || !entity.getType().builtInRegistryHolder().key().location().getNamespace().equals("animania")) return;
        State state = state(entity);
        state.previousTimer = state.timer;
        state.previousPet = state.pet;
        if (state.sleeping) {
            float step = entity instanceof com.animania.farm.livestock.AnimaniaSheep sheep
                    && sheep.breed() == com.animania.farm.livestock.SheepBreed.FRIESIAN
                    && sheep.role() == com.animania.farm.livestock.FarmAnimalRole.FEMALE ? 0.0125F : 0.01F;
            if (state.timer > -0.55F && (!(entity instanceof com.animania.farm.livestock.AnimaniaPig)
                    || entity.level().random.nextInt(2) < 1)) state.timer -= step;
            state.pet = Math.min(10, state.pet + 1);
        } else state.pet = Math.max(0, state.pet - 1);
    }
    static float timer(Entity entity, float partial) {
        State state = state(entity);
        return Mth.lerp(partial, state.previousTimer, state.timer);
    }
    static float petBlend(Entity entity, float partial) {
        State state = state(entity);
        return Mth.lerp(partial, state.previousPet, state.pet) / 10;
    }

    /** Original preRenderScale transforms, applied after each renderer's scale. */
    static void transform(net.minecraft.world.entity.animal.Animal animal,
                          com.mojang.blaze3d.vertex.PoseStack pose, float partial) {
        if (!animal.getData(ModAttachments.SLEEPING)) return;
        float base;
        if (animal instanceof com.animania.farm.livestock.AnimaniaCow cow) {
            base = cow.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? 1.15F : 1.85F;
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaHorse horse) {
            base = horse.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? 1.25F : 1.95F;
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaGoat goat) {
            base = goat.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? 0.5F
                    : switch (goat.breed()) { case FAINTING, KIKO, PYGMY -> 1.10F; default -> 1.45F; };
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaSheep sheep) {
            base = sheep.role() == com.animania.farm.livestock.FarmAnimalRole.YOUNG ? 0.45F
                    : sheep.role() == com.animania.farm.livestock.FarmAnimalRole.FEMALE
                    && sheep.breed() == com.animania.farm.livestock.SheepBreed.DORPER ? 0.85F : 1.05F;
        } else if (animal instanceof com.animania.catsdogs.cat.AnimaniaCat) {
            base = 2;
        } else if (animal instanceof com.animania.catsdogs.dog.AnimaniaDog dog) {
            if (dog.breed() != com.animania.catsdogs.dog.DogBreed.FOX) {
                pose.translate(0, -0.1, 0);
                return;
            }
            base = 1.45F;
        } else return;
        pose.translate(-0.25F, animal.getBbHeight() - base - timer(animal, partial), -0.25F);
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(6));
        if (animal instanceof com.animania.catsdogs.cat.AnimaniaCat) pose.translate(0, animal.isBaby() ? 1 : 0.6, 0);
        if (animal instanceof com.animania.catsdogs.dog.AnimaniaDog) pose.translate(0, -0.3, 0);
    }
}
