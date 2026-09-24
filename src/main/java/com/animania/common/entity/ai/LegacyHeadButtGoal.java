package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaGoat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

/** Short reciprocal sparring bouts, separate from combat targets and damaging attacks. */
public final class LegacyHeadButtGoal extends Goal {
    private static final Map<Animal, Integer> COOLDOWNS = new WeakHashMap<>();
    private final Animal animal;
    private Animal rival;
    private int nextSearch, elapsed;
    public LegacyHeadButtGoal(Animal animal) {
        this.animal = animal;
        nextSearch = animal.tickCount + 400 + animal.getRandom().nextInt(400);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }
    private static boolean ready(Animal animal) {
        return animal.isAlive() && !animal.isBaby() && animal.onGround()
                && AnimalInformation.gender(animal) == AnimalInformation.Gender.MALE
                && !AnimalInformation.isSterilized(animal) && animal.hurtTime == 0 && animal.getTarget() == null
                && !animal.isLeashed() && !animal.isPassenger() && !animal.isVehicle() && !animal.isInWaterOrBubble()
                && !ModAttachments.getData(animal, ModAttachments.SLEEPING) && !LegacySleepGoal.shouldSleepNow(animal)
                && (!(animal instanceof AnimaniaGoat goat) || !goat.isSpooked())
                && LegacyAnimalNeeds.isFed(animal) && LegacyAnimalNeeds.isWatered(animal)
                && !FarmHerdGoal.hasFoodLure(animal);
    }
    @Override public boolean canUse() {
        if (!LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get() || !ready(animal)) return false;
        String assigned = ModAttachments.getData(animal, ModAttachments.RIVAL);
        if (assigned.isEmpty()) {
            if (animal.tickCount < nextSearch || animal.tickCount < COOLDOWNS.getOrDefault(animal, 0)) return false;
            nextSearch = animal.tickCount + 200 + animal.getRandom().nextInt(200);
        }
        rival = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(8), other ->
                other != animal && other.getClass() == animal.getClass() && ready(other) && animal.hasLineOfSight(other)
                && (assigned.equals(other.getStringUUID()) || assigned.isEmpty()
                    && ModAttachments.getData(other, ModAttachments.RIVAL).isEmpty()
                    && other.tickCount >= COOLDOWNS.getOrDefault(other, 0)))
                .stream().min(Comparator.comparingDouble(animal::distanceToSqr)).orElse(null);
        return rival != null;
    }
    @Override public void start() {
        elapsed = 0;
        ModAttachments.setData(animal, ModAttachments.RIVAL, rival.getStringUUID());
        ModAttachments.setData(rival, ModAttachments.RIVAL, animal.getStringUUID());
        ModAttachments.setData(animal, ModAttachments.FARM_ACTIVITY, FarmActivityGoal.SPAR);
        ModAttachments.setData(animal, ModAttachments.FARM_ACTIVITY_START, (int) animal.level().getGameTime());
    }
    @Override public boolean canContinueToUse() {
        return elapsed < 120 && rival != null && ready(animal) && ready(rival)
                && animal.distanceToSqr(rival) < 100
                && ModAttachments.getData(animal, ModAttachments.RIVAL).equals(rival.getStringUUID())
                && ModAttachments.getData(rival, ModAttachments.RIVAL).equals(animal.getStringUUID());
    }
    @Override public void tick() {
        elapsed++;
        animal.getLookControl().setLookAt(rival, 6, 20);
        if (elapsed < 80 && animal.distanceToSqr(rival) > 3.0) {
            if (elapsed % 10 == 1) animal.getNavigation().moveTo(rival, 0.9);
        } else animal.getNavigation().stop();
    }
    @Override public void stop() {
        animal.getNavigation().stop();
        release(animal);
        if (rival != null && ModAttachments.getData(rival, ModAttachments.RIVAL).equals(animal.getStringUUID())) release(rival);
        rival = null;
    }
    private static void release(Animal animal) {
        ModAttachments.setData(animal, ModAttachments.RIVAL, "");
        if (ModAttachments.getData(animal, ModAttachments.FARM_ACTIVITY) == FarmActivityGoal.SPAR)
            ModAttachments.setData(animal, ModAttachments.FARM_ACTIVITY, 0);
        COOLDOWNS.put(animal, animal.tickCount + 1200 + animal.getRandom().nextInt(1200));
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
