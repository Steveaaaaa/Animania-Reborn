package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaGoat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;

/** Paired buck/ram rivalry. Original shouldExecute drives both navigators,
 * without a melee hit. No movement mutex: the buck leap must run concurrently. */
public final class LegacyHeadButtGoal extends Goal {
    private final Animal animal;
    private Animal rival;
    private int delay, remaining;
    public LegacyHeadButtGoal(Animal animal) { this.animal = animal; }
    @Override public boolean canUse() {
        if (!LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get() || AnimalInformation.isSterilized(animal)
                || AnimalInformation.gender(animal) != AnimalInformation.Gender.MALE) return false;
        if (++delay <= (animal instanceof AnimaniaGoat ? LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get() * 20 : 1000)) return false;
        if (!animal.level().isDay() || animal.getData(ModAttachments.SLEEPING)) { delay = 0; return false; }
        String current = animal.getData(ModAttachments.RIVAL);
        rival = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(10),
                other -> other != animal && other.getClass() == animal.getClass() && other.isAlive()
                        && AnimalInformation.gender(other) == AnimalInformation.Gender.MALE
                        && (current.equals(other.getStringUUID()) || current.isEmpty()
                        && other.getData(ModAttachments.RIVAL).isEmpty())).stream().findFirst().orElse(null);
        return rival != null;
    }
    @Override public void start() {
        remaining = 100 + animal.getRandom().nextInt(50);
        animal.setData(ModAttachments.RIVAL, rival.getStringUUID());
        rival.setData(ModAttachments.RIVAL, animal.getStringUUID());
        animal.setData(ModAttachments.FIGHTING, true);
        rival.setData(ModAttachments.FIGHTING, true);
        animal.setTarget(rival); rival.setTarget(animal);
    }
    @Override public boolean canContinueToUse() {
        return remaining > 0 && rival != null && rival.isAlive()
                && animal.getData(ModAttachments.FIGHTING) && !animal.getData(ModAttachments.SLEEPING)
                && !rival.getData(ModAttachments.SLEEPING) && !AnimalInformation.isSterilized(animal);
    }
    @Override public void tick() {
        --remaining;
        animal.getLookControl().setLookAt(rival, 10, animal.getMaxHeadXRot());
        animal.getNavigation().moveTo(rival, 1.3D);
        rival.getLookControl().setLookAt(animal, 10, rival.getMaxHeadXRot());
        rival.getNavigation().moveTo(animal, 1.3D);
    }
    @Override public void stop() {
        clear(animal);
        if (rival != null && rival.getData(ModAttachments.RIVAL).equals(animal.getStringUUID())) clear(rival);
        rival = null; delay = 0;
    }
    private static void clear(Animal animal) {
        animal.setData(ModAttachments.FIGHTING, false);
        animal.setData(ModAttachments.RIVAL, "");
        animal.setTarget(null); animal.getNavigation().stop();
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
