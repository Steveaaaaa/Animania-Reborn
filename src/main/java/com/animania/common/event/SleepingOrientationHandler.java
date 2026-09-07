package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Preserve the resting orientation through server AI and client body interpolation. */
@EventBusSubscriber(modid = Animania.MOD_ID)
public final class SleepingOrientationHandler {
    private static final Map<Animal, Orientation> RESTING = Collections.synchronizedMap(new WeakHashMap<>());

    private SleepingOrientationHandler() { }

    public static void holdOrientation(Animal animal) {
        if (!AnimalInformation.isAnimaniaAnimal(animal)) return;
        if (!ModAttachments.getData(animal, ModAttachments.SLEEPING)) {
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
