package com.animania.common.entity;

import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Restores the original 1.12 female-led natural family spawning rule. */
public final class LegacyNaturalFamily {
    public enum Result {
        NONE,
        MALE,
        YOUNG
    }

    private LegacyNaturalFamily() {
    }

    public static Result spawn(ServerLevelAccessor level, Animal female, MobSpawnType spawnType,
                               Class<? extends Animal> familyClass, int nearbyLimit,
                               Supplier<? extends Animal> maleFactory,
                               Supplier<? extends Animal> youngFactory,
                               Consumer<Animal> appearanceCopy) {
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return Result.NONE;
        }
        int nearby = level.getLevel().getEntitiesOfClass(familyClass,
                female.getBoundingBox().inflate(64.0D)).size();
        if (nearby > nearbyLimit) return Result.NONE;

        int chooser = female.getRandom().nextInt(3);
        if (chooser == 2) return Result.NONE;
        Animal companion = chooser == 0 ? maleFactory.get() : youngFactory.get();
        if (companion == null) return Result.NONE;

        appearanceCopy.accept(companion);
        companion.moveTo(female.getX(), female.getY(), female.getZ(), female.getYRot(), 0.0F);
        if (chooser == 0) {
            // The old implementation assigned the female to the newly spawned male.
            ModAttachments.setData(companion, ModAttachments.LAST_MATE, female.getUUID().toString());
        } else {
            AnimalInformation.recordParent(companion, female);
        }
        level.addFreshEntity(companion);
        return chooser == 0 ? Result.MALE : Result.YOUNG;
    }
}
