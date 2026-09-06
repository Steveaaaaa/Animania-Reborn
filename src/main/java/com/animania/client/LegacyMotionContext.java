package com.animania.client;

import com.animania.common.registry.ModAttachments;
import com.animania.extra.rodent.AnimaniaRodent;
import com.animania.farm.chicken.AnimaniaChicken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

/** State adapter for the original 1.12 model equations. */
final class LegacyMotionContext {
    private final Entity entity;
    final float pitch;
    final int ticks, eatTimer;

    LegacyMotionContext(Entity entity) {
        this.entity = entity;
        eatTimer = entity.getData(ModAttachments.SLEEPING) ? 0 : entity.getData(ModAttachments.EATING_TICKS);
        pitch = entity.getData(ModAttachments.SLEEPING) ? 0 : entity.getXRot();
        ticks = entity.getData(ModAttachments.SLEEPING) ? 1 : entity.tickCount;
    }

    boolean getSleeping() { return entity.getData(ModAttachments.SLEEPING); }
    boolean isSitting() { return entity instanceof TamableAnimal tame && tame.isInSittingPose(); }
    boolean isTamed() { return entity instanceof TamableAnimal tame && tame.isTame(); }
    boolean isBeingRidden() { return entity.isVehicle(); }
    boolean isRiding() { return entity.isPassenger(); }
    boolean getFighting() { return entity.getData(ModAttachments.FIGHTING); }
    String getRivalUniqueId() {
        String rival = entity.getData(ModAttachments.RIVAL);
        return rival.isEmpty() ? null : rival;
    }
    long worldTime() { return entity.level().getDayTime(); }
    int getCrowDuration() { return entity instanceof AnimaniaChicken chicken ? chicken.getCrowDuration() : 0; }
    int getFoodStackCount() { return entity instanceof AnimaniaRodent rodent ? rodent.getFoodStackCount() : 0; }
    boolean isHamsterStanding() { return entity instanceof AnimaniaRodent rodent && !getSleeping() && rodent.isHamsterStanding(); }
    float getInterestedAngle(float partial) {
        return !getSleeping() && entity instanceof AnimaniaRodent rodent ? rodent.getInterestedAngle(partial) : 0;
    }
    boolean isType(String legacyType) {
        if (legacyType.equals("EntityHedgehogBase"))
            return entity instanceof AnimaniaRodent rodent && rodent.kind().isHedgehog();
        return legacyType.substring("Entity".length()).equalsIgnoreCase(
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().replace("_", ""));
    }
    float getHeadAnchorPointY(float partial) {
        if (isBeingRidden()) return 0;
        int timer = entity.getData(ModAttachments.EATING_TICKS);
        if (timer <= 0) return 0;
        if (timer >= 4 && timer <= 76) return 1;
        return timer < 4 ? (timer - partial) / 4 : -(timer - 80 - partial) / 4;
    }
    float getHeadAngleX(float partial) {
        if (isBeingRidden()) return 0;
        int timer = entity.getData(ModAttachments.EATING_TICKS);
        if (timer > 4 && timer <= 76)
            return Mth.PI / 5 + Mth.PI * 7 / 150 * Mth.sin((timer - 4 - partial) / 24 * 28.7F);
        return timer > 0 ? Mth.PI / 5 : pitch * 0.017453292F;
    }
}
