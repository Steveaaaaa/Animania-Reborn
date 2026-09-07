package com.animania.common.entity.ai;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModAttachments;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** Dynamic-food-list equivalent of 1.12 GenericAITempt. */
public final class LegacyTemptGoal extends Goal {
    private final PathfinderMob mob;
    private final Animal animal;
    private final double speed;
    private final boolean scaredByMovement;
    private Player player;
    private double lastX;
    private double lastY;
    private double lastZ;
    private double lastXRot;
    private double lastYRot;
    private int cooldown;
    private java.util.function.Predicate<net.minecraft.world.item.ItemStack> itemPredicate;
    private boolean stackTempt;

    public LegacyTemptGoal(PathfinderMob mob, double speed, boolean scaredByMovement) {
        this.mob = mob;
        this.animal = (Animal) mob;
        this.speed = speed;
        this.scaredByMovement = scaredByMovement;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public LegacyTemptGoal(PathfinderMob mob, double speed,
                           java.util.function.Predicate<net.minecraft.world.item.ItemStack> items, boolean stackTempt) {
        this(mob, speed, false); this.itemPredicate = items; this.stackTempt = stackTempt;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (ModAttachments.getData(animal, ModAttachments.SLEEPING)
                || animal instanceof TamableAnimal tame && tame.isInSittingPose()) return false;
        player = nearestTemptingPlayer();
        return player != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (player == null || !player.isAlive() || player.isSpectator() || !isTempting(player)) return false;
        if (scaredByMovement && mob.distanceToSqr(player) < 36.0D) {
            if (player.distanceToSqr(lastX, lastY, lastZ) > 0.01D) return false;
            if (Math.abs(player.getXRot() - lastXRot) > 5.0D
                    || Math.abs(player.getYRot() - lastYRot) > 5.0D) return false;
        }
        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
        lastXRot = player.getXRot();
        lastYRot = player.getYRot();
        player = nearestTemptingPlayer();
        return player != null && !ModAttachments.getData(animal, ModAttachments.SLEEPING)
                && (!(animal instanceof TamableAnimal tame) || !tame.isInSittingPose());
    }

    @Override
    public void start() {
        if (player != null) {
            lastX = player.getX();
            lastY = player.getY();
            lastZ = player.getZ();
            lastXRot = player.getXRot();
            lastYRot = player.getYRot();
        }
    }

    @Override
    public void stop() {
        player = null;
        mob.getNavigation().stop();
        cooldown = stackTempt ? com.animania.common.config.LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get() : 100;
    }

    @Override
    public void tick() {
        if (player == null) return;
        if (!stackTempt && !LegacyAnimalNeeds.isInteracted(animal)) LegacyAnimalNeeds.setInteracted(animal, true);
        mob.getLookControl().setLookAt(player, mob.getMaxHeadYRot() + 20.0F, mob.getMaxHeadXRot());
        if (mob.distanceToSqr(player) < 6.25D) mob.getNavigation().stop();
        else mob.getNavigation().moveTo(player, speed);
    }

    private Player nearestTemptingPlayer() {
        Player nearest = null;
        double nearestDistance = 100.0D;
        for (Player candidate : mob.level().players()) {
            if (!candidate.isAlive() || candidate.isSpectator()) continue;
            double distance = mob.distanceToSqr(candidate);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest != null && isTempting(nearest) ? nearest : null;
    }

    private boolean isTempting(Player candidate) {
        if (itemPredicate != null) return itemPredicate.test(candidate.getMainHandItem())
                || itemPredicate.test(candidate.getOffhandItem());
        return animal.isFood(candidate.getMainHandItem()) || animal.isFood(candidate.getOffhandItem());
    }

    public record Settings(int priority, double speed, boolean scaredByMovement) {
        public static Settings forAnimal(Animal animal) {
            if (animal instanceof com.animania.catsdogs.cat.AnimaniaCat) return new Settings(3, 0.6D, true);
            if (animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl) return new Settings(3, 1.2D, false);
            if (animal instanceof com.animania.farm.chicken.AnimaniaChicken) return new Settings(4, 1.2D, false);
            if (animal instanceof com.animania.farm.livestock.AnimaniaHorse) return new Settings(5, 1.25D, false);
            if (animal instanceof com.animania.extra.rodent.AnimaniaRodent rodent
                    && rodent.kind() == com.animania.extra.rodent.AnimaniaRodent.Kind.HAMSTER) {
                return new Settings(6, 1.2D, false);
            }
            if (animal instanceof com.animania.farm.livestock.AnimaniaCow
                    || animal instanceof com.animania.farm.livestock.AnimaniaGoat
                    || animal instanceof com.animania.farm.livestock.AnimaniaSheep
                    || animal instanceof com.animania.extra.rabbit.AnimaniaRabbit) {
                return new Settings(7, 1.25D, false);
            }
            if (animal instanceof com.animania.extra.rodent.AnimaniaRodent rodent
                    && !rodent.kind().isFerret()) return new Settings(9, 1.2D, false);
            return new Settings(10, 1.2D, false);
        }
    }
    @Override
    public boolean requiresUpdateEveryTick() { return true; }
}
