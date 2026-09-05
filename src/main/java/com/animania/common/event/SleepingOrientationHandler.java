package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Preserve the resting orientation through server AI and client body interpolation. */
@EventBusSubscriber(modid = Animania.MOD_ID)
public final class SleepingOrientationHandler {
    private static final Map<Animal, Orientation> RESTING = Collections.synchronizedMap(new WeakHashMap<>());

    private SleepingOrientationHandler() { }

    @SubscribeEvent
    public static void beforeTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Animal animal) holdOrientation(animal);
    }

    @SubscribeEvent
    public static void afterTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Animal animal) holdOrientation(animal);
    }

    private static void holdOrientation(Animal animal) {
        if (!AnimalInformation.isAnimaniaAnimal(animal)) return;
        if (!animal.getData(ModAttachments.SLEEPING)) {
            RESTING.remove(animal);
            return;
        }
        Orientation resting = RESTING.computeIfAbsent(animal, a ->
                new Orientation(a.getYRot(), a.getXRot(), a.yBodyRot, a.yHeadRot));
        animal.setYRot(resting.yaw());
        animal.setXRot(resting.pitch());
        animal.yRotO = resting.yaw();
        animal.xRotO = resting.pitch();
        animal.yBodyRot = animal.yBodyRotO = resting.body();
        animal.yHeadRot = animal.yHeadRotO = resting.head();
    }

    private record Orientation(float yaw, float pitch, float body, float head) { }
}
