package com.animania.common.entity;
import com.animania.common.event.AnimalNeedsHandler;
import com.animania.common.event.SleepingOrientationHandler;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Preserves the pre/post entity-tick ordering used by the 1.21.1 branch. */
public final class AnimalTickBridge {
    public static void before(Animal animal) { SleepingOrientationHandler.holdOrientation(animal); }
    public static void after(Animal animal) {
        AnimalNeedsHandler.onAnimalTick(animal);
        SleepingOrientationHandler.holdOrientation(animal);
        if (animal.level().isClientSide()) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.animania.client.LegacySleepAnimation.tick(animal));
    }
}
