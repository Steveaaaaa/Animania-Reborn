package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;

final class HusbandryActivityGuard {
    private HusbandryActivityGuard() {}
    static boolean free(Animal a) {
        if (!a.isAlive() || a.isBaby() || !a.onGround() || a.hurtTime > 0 || a.getTarget() != null
                || a.isOnFire() || a.isInWaterOrBubble() || a.isPassenger() || a.isVehicle() || a.isLeashed()
                || ModAttachments.getData(a, ModAttachments.FIGHTING) || a.isNoAi() || a.isInLove() || ModAttachments.getData(a, ModAttachments.SLEEPING) || com.animania.common.config.LegacyConfig.ANIMALS_SLEEP.get() && LegacySleepGoal.shouldSleepNow(a)
                || ModAttachments.getData(a, ModAttachments.EATING_TICKS) > 0 || ModAttachments.getData(a, ModAttachments.FAMILY_POSE) != 0
                || !LegacyAnimalNeeds.isFed(a) || !LegacyAnimalNeeds.isWatered(a)
                || FarmHerdGoal.hasFoodLure(a)) return false;
        if (a instanceof AnimaniaHorse horse && horse.isPullingVehicle()) return false;
        if (a instanceof AnimaniaRodent r && (r.isInBall() || r.isHamsterStanding())) return false;
        return !(a instanceof TamableAnimal pet) || !pet.isOrderedToSit() && !pet.isInSittingPose()
                && (pet.getOwner() == null || pet.distanceToSqr(pet.getOwner()) < 64);
    }
}
