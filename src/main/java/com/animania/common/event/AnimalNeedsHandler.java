package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.entity.LegacyGrowth;
import com.animania.common.entity.LegacyReproduction;
import com.animania.common.entity.ai.LegacyFindFoodGoal;
import com.animania.common.entity.ai.LegacyFindNestGoal;
import com.animania.common.entity.ai.LegacyFindSaltLickGoal;
import com.animania.common.entity.ai.LegacyFindWaterGoal;
import com.animania.common.entity.ai.LegacyFollowParentGoal;
import com.animania.common.entity.ai.LegacyGrazeGoal;
import com.animania.common.entity.ai.LegacyPigSnuffleGoal;
import com.animania.common.entity.ai.LegacyMateGoal;
import com.animania.common.entity.ai.LegacySleepGoal;
import com.animania.common.entity.ai.LegacyTemptGoal;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModFluids;
import com.animania.common.world.block.entity.SaltLickBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class AnimalNeedsHandler {
    private static final Set<Animal> GOALS_INSTALLED =
            Collections.newSetFromMap(new WeakHashMap<>());

    private AnimalNeedsHandler() {
    }

    @SubscribeEvent
    public static void onAnimalTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animal animal) || animal.level().isClientSide()
                || LegacyAnimalNeeds.profile(animal) == null) return;
        maintainSleeping(animal);
        int eating = animal.getData(ModAttachments.EATING_TICKS);
        if (eating > 0) animal.setData(ModAttachments.EATING_TICKS, eating - 1);
        LegacyAnimalNeeds.tick(animal);
        LegacyGrowth.tick(animal);
        LegacyReproduction.tickFertility(animal);
        LegacyReproduction.tickMateReset(animal);

        if ((!LegacyConfig.FEED_TO_BREED.get() || animal.getData(ModAttachments.HAND_FED))
                && LegacyAnimalNeeds.isFed(animal) && LegacyAnimalNeeds.isWatered(animal)
                && animal.getAge() == 0 && !AnimalInformation.isSterilized(animal)
                && (!LegacyConfig.REQUIRE_ANIMAL_INTERACTION_FOR_AI.get()
                || LegacyAnimalNeeds.isInteracted(animal))
                && !(animal instanceof com.animania.farm.chicken.AnimaniaChicken)
                && !(animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl)) animal.setInLove(null);

    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)
                || !(animal instanceof PathfinderMob pathfinder) || !GOALS_INSTALLED.add(animal)) return;
        LegacyAnimalNeeds.Profile profile = LegacyAnimalNeeds.profile(animal);
        if (profile == null) {
            if (animal instanceof com.animania.extra.amphibian.AnimaniaAmphibian) {
                pathfinder.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(
                        pathfinder, net.minecraft.world.entity.player.Player.class, 6.0F, 1.5D, 1.5D,
                        player -> !animal.hasCustomName() || !animal.getName().getString().equals("Pepe")));
                pathfinder.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(
                        pathfinder, com.animania.farm.chicken.AnimaniaChicken.class, 6.0F, 1.5D, 1.5D));
            }
            return;
        }
        pathfinder.goalSelector.removeAllGoals(goal -> goal instanceof net.minecraft.world.entity.ai.goal.TemptGoal);
        pathfinder.goalSelector.removeAllGoals(goal -> goal instanceof net.minecraft.world.entity.ai.goal.BreedGoal);
        if (LegacyFollowParentGoal.supports(animal)
                && AnimalInformation.gender(animal) == AnimalInformation.Gender.YOUNG) {
            pathfinder.goalSelector.removeAllGoals(
                    goal -> goal instanceof net.minecraft.world.entity.ai.goal.FollowParentGoal);
            pathfinder.goalSelector.addGoal(1, new LegacyFollowParentGoal(pathfinder, animal, 1.1D));
        }
        if (animal instanceof TamableAnimal) {
            pathfinder.goalSelector.removeAllGoals(goal -> goal instanceof net.minecraft.world.entity.ai.goal.FollowOwnerGoal);
        }
        LegacyTemptGoal.Settings tempt = LegacyTemptGoal.Settings.forAnimal(animal);
        pathfinder.goalSelector.addGoal(tempt.priority(), new LegacyTemptGoal(pathfinder,
                tempt.speed(), tempt.scaredByMovement()));
        pathfinder.goalSelector.addGoal(profile.waterPriority(), new LegacyFindWaterGoal(pathfinder, profile));
        pathfinder.goalSelector.addGoal(profile.foodPriority(), new LegacyFindFoodGoal(pathfinder, profile));
        pathfinder.goalSelector.addGoal(13, new LegacySleepGoal(pathfinder, animal));
        pathfinder.goalSelector.addGoal(-1, new com.animania.common.entity.ai.LegacyStayAsleepGoal(animal));
        pathfinder.targetSelector.addGoal(-1, new com.animania.common.entity.ai.LegacyStayAsleepGoal(animal));
        pathfinder.goalSelector.addGoal(12, new LegacyFindSaltLickGoal(pathfinder, animal));
        if (animal instanceof com.animania.farm.chicken.AnimaniaChicken chicken
                && chicken.role() == com.animania.farm.chicken.ChickenRole.HEN
                || animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl peafowl
                && peafowl.role() == com.animania.extra.peafowl.PeafowlRole.PEAHEN) {
            pathfinder.goalSelector.addGoal(6, new LegacyFindNestGoal(pathfinder, animal));
        }
        if (AnimalInformation.gender(animal) == AnimalInformation.Gender.MALE
                && !(animal instanceof com.animania.farm.chicken.AnimaniaChicken)
                && !(animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl)) {
            pathfinder.goalSelector.addGoal(8, new LegacyMateGoal(pathfinder, animal, 1.0D));
        }
        if (animal instanceof com.animania.catsdogs.cat.AnimaniaCat cat
                && cat.role() == com.animania.catsdogs.cat.CatRole.KITTEN
                || animal instanceof com.animania.catsdogs.dog.AnimaniaDog dog
                && dog.role() == com.animania.catsdogs.dog.DogRole.PUPPY) {
            pathfinder.goalSelector.addGoal(8,
                    new com.animania.common.entity.ai.LegacyPlayGoal(pathfinder, animal));
        }
        if (animal instanceof com.animania.extra.rodent.AnimaniaRodent rodent
                && (rodent.kind().isFerret() || rodent.kind().isHedgehog())) {
            pathfinder.goalSelector.addGoal(4, new com.animania.common.entity.ai.LegacyRaidNestGoal(rodent));
        }
        if (animal instanceof com.animania.extra.rabbit.AnimaniaRabbit
                || animal instanceof com.animania.extra.rodent.AnimaniaRodent rodent
                && (rodent.kind().isFerret() || rodent.kind().isHedgehog())) {
            pathfinder.goalSelector.addGoal(9,
                    new com.animania.common.entity.ai.LegacyRodentGrazeGoal(pathfinder, animal));
        }
        if (animal instanceof TamableAnimal tame) {
            double followSpeed = animal instanceof com.animania.catsdogs.cat.AnimaniaCat
                    || animal instanceof com.animania.catsdogs.dog.AnimaniaDog ? 1.5D : 1.0D;
            float startDistance = animal instanceof com.animania.catsdogs.cat.AnimaniaCat
                    || animal instanceof com.animania.catsdogs.dog.AnimaniaDog ? 5.0F : 10.0F;
            float stopDistance = animal instanceof com.animania.catsdogs.cat.AnimaniaCat
                    || animal instanceof com.animania.catsdogs.dog.AnimaniaDog ? 30.0F : 2.0F;
            pathfinder.goalSelector.addGoal(4,
                    new com.animania.common.entity.ai.LegacyFollowOwnerGoal(tame, followSpeed,
                            startDistance, stopDistance));
        }
        if (animal instanceof com.animania.farm.livestock.AnimaniaGoat goat) {
            pathfinder.goalSelector.addGoal(1,
                    new com.animania.common.entity.ai.LegacyGetDogHerdedGoal(pathfinder, animal));
            pathfinder.goalSelector.addGoal(5, new com.animania.common.entity.ai.LegacyHeadButtGoal(goat));
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaSheep) {
            pathfinder.goalSelector.addGoal(1,
                    new com.animania.common.entity.ai.LegacyGetDogHerdedGoal(pathfinder, animal));
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaHorse horse) {
            pathfinder.goalSelector.addGoal(2,
                    new com.animania.common.entity.ai.LegacyFollowMateHorseGoal(horse));
        } else if (animal instanceof com.animania.farm.chicken.AnimaniaChicken chicken
                && chicken.role() == com.animania.farm.chicken.ChickenRole.ROOSTER) {
            pathfinder.goalSelector.addGoal(3,
                    new com.animania.common.entity.ai.LegacyRoosterFightGoal(chicken));
        }
        if (animal instanceof com.animania.farm.livestock.AnimaniaCow cow
                && cow.role() == com.animania.farm.livestock.FarmAnimalRole.MALE) {
            pathfinder.targetSelector.addGoal(14,
                    new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(pathfinder));
            if (LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get()) {
                pathfinder.goalSelector.addGoal(0,
                        new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(pathfinder, 1.8D, false));
            }
        }
        if (LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get()
                && animal instanceof com.animania.farm.chicken.AnimaniaChicken chicken
                && chicken.role() != com.animania.farm.chicken.ChickenRole.CHICK) {
            pathfinder.goalSelector.addGoal(9,
                    new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(pathfinder, 0.2F));
            pathfinder.goalSelector.addGoal(10,
                    new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(pathfinder, 1.0D, true));
            pathfinder.targetSelector.addGoal(2,
                    new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(pathfinder,
                            com.animania.extra.amphibian.AnimaniaAmphibian.class, true,
                            target -> target instanceof com.animania.extra.amphibian.AnimaniaAmphibian amphibian
                                    && amphibian.kind() != com.animania.extra.amphibian.AnimaniaAmphibian.Kind.DART_FROG));
        }
        if (LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get()
                && animal instanceof com.animania.extra.rodent.AnimaniaRodent rodent) {
            if (rodent.kind().isFerret() || rodent.kind().isHedgehog()) {
                pathfinder.goalSelector.addGoal(5,
                        new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(pathfinder, 0.2F));
            }
            if (rodent.kind().isFerret()) {
                pathfinder.targetSelector.addGoal(2,
                        new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(pathfinder,
                                com.animania.farm.chicken.AnimaniaChicken.class, true,
                                target -> target instanceof com.animania.farm.chicken.AnimaniaChicken chick
                                        && chick.role() == com.animania.farm.chicken.ChickenRole.CHICK));
            }
            if (rodent.kind().isFerret() || rodent.kind().isHedgehog()) {
                if (rodent.kind().isHedgehog()) {
                    pathfinder.goalSelector.addGoal(8,
                            new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(pathfinder, 1.0D, true));
                }
                pathfinder.targetSelector.addGoal(3,
                        new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(pathfinder,
                                com.animania.extra.amphibian.AnimaniaAmphibian.class, true,
                                target -> target instanceof com.animania.extra.amphibian.AnimaniaAmphibian amphibian
                                        && amphibian.kind() != com.animania.extra.amphibian.AnimaniaAmphibian.Kind.DART_FROG));
            }
            if (rodent.kind().isHedgehog()) {
                pathfinder.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(
                        pathfinder, com.animania.farm.chicken.AnimaniaChicken.class, 10.0F, 3.0D, 3.5D,
                        bird -> bird instanceof com.animania.farm.chicken.AnimaniaChicken chicken
                                && chicken.role() == com.animania.farm.chicken.ChickenRole.ROOSTER));
            }
        }
        if (LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get()
                && animal instanceof com.animania.farm.livestock.AnimaniaGoat goat
                && goat.role() == com.animania.farm.livestock.FarmAnimalRole.MALE) {
            pathfinder.goalSelector.addGoal(3,
                    new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(pathfinder, 0.25F));
            pathfinder.targetSelector.addGoal(14,
                    new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(pathfinder));
        }
        if (animal instanceof com.animania.farm.livestock.AnimaniaCow
                || animal instanceof com.animania.farm.livestock.AnimaniaGoat
                || animal instanceof com.animania.farm.livestock.AnimaniaSheep) {
            pathfinder.goalSelector.addGoal(8, new LegacyGrazeGoal(pathfinder));
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaHorse) {
            pathfinder.goalSelector.addGoal(6, new LegacyGrazeGoal(pathfinder));
        } else if (animal instanceof com.animania.farm.livestock.AnimaniaPig pig) {
            pathfinder.goalSelector.addGoal(11, new LegacyPigSnuffleGoal(pig));
        }
    }

    private static void maintainSleeping(Animal animal) {
        boolean sleeping = animal.getData(ModAttachments.SLEEPING);
        if (sleeping && (!LegacyConfig.ANIMALS_SLEEP.get() || !LegacySleepGoal.shouldSleepNow(animal)
                || animal.isOnFire() || animal.isPassenger() || animal.hurtTime > 0
                || animal.getTarget() != null
                || animal instanceof TamableAnimal tame && tame.isInSittingPose()
                || animal.level().isRainingAt(animal.blockPosition())
                && animal.level().canSeeSky(animal.blockPosition()))) {
            animal.setData(ModAttachments.SLEEPING, false);
            sleeping = false;
        }
        if (sleeping && animal instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onInspectAnimal(PlayerInteractEvent.EntityInteract event) {
        if (!event.getTarget().level().isClientSide() && event.getTarget() instanceof Animal animal
                && LegacyAnimalNeeds.profile(animal) != null) LegacyAnimalNeeds.setInteracted(animal, true);

        if (event.getTarget() instanceof Animal animal && LegacyAnimalNeeds.profile(animal) != null) {
            ItemStack held = event.getEntity().getItemInHand(event.getHand());
            if (!animal.level().isClientSide() && isWaterContainer(held)
                    && !animal.getData(ModAttachments.SLEEPING)) {
                if (!event.getEntity().getAbilities().instabuild) emptyOneWaterContainer(event, held);
                LegacyAnimalNeeds.water(animal);
                animal.setData(ModAttachments.EATING_TICKS, 40);
                showCareHeartsOrEnableBreeding(animal, event.getEntity());
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
            if (animal instanceof com.animania.farm.livestock.AnimaniaPig
                    && !animal.level().isClientSide() && isSlopContainer(held)
                    && !animal.getData(ModAttachments.SLEEPING)) {
                if (!event.getEntity().getAbilities().instabuild) emptyOneFluidContainer(event, held, false);
                LegacyAnimalNeeds.feed(animal, true, true);
                animal.setData(ModAttachments.EATING_TICKS, 40);
                showCareHeartsOrEnableBreeding(animal, event.getEntity());
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
            if (animal.isFood(held)) {
                if (animal.getData(ModAttachments.SLEEPING)) {
                    event.setCancellationResult(InteractionResult.sidedSuccess(animal.level().isClientSide()));
                    event.setCanceled(true);
                    return;
                }
                if (!animal.level().isClientSide()) {
                    if (!event.getEntity().getAbilities().instabuild) held.shrink(1);
                    LegacyAnimalNeeds.feed(animal, true, false);
                    animal.setData(ModAttachments.EATING_TICKS, 80);
                    showCareHeartsOrEnableBreeding(animal, event.getEntity());
                    if (animal instanceof TamableAnimal tame && !tame.isTame()) {
                        tame.tame(event.getEntity());
                        tame.setOrderedToSit(false);
                        animal.level().broadcastEntityEvent(animal, (byte) 7);
                    }
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(animal.level().isClientSide()));
                event.setCanceled(true);
                return;
            }
        }
        if (event.getTarget() instanceof Animal animal
                && event.getEntity().getItemInHand(event.getHand()).is(ModItems.CARVING_KNIFE.get())
                && AnimalInformation.canBeSterilized(animal)) {
            if (!animal.level().isClientSide() && !AnimalInformation.isSterilized(animal)) {
                animal.setData(ModAttachments.STERILIZED, true);
                animal.resetLove();
                var player = event.getEntity();
                var knife = player.getItemInHand(event.getHand());
                knife.hurtAndBreak(1, player,
                        event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                if (animal.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION,
                            animal.getX(), animal.getY() + animal.getBbHeight() * 0.5D, animal.getZ(),
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                animal.playSound(SoundEvents.MOOSHROOM_SHEAR, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("message.animania.sterilized", animal.getName()), true);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(animal.level().isClientSide()));
            event.setCanceled(true);
            return;
        }

        if (!event.getEntity().level().isClientSide()
                && AnimaniaConfig.SHOW_NEEDS_ON_EMPTY_HAND.get()
                && event.getTarget() instanceof Animal animal
                && event.getEntity().getItemInHand(event.getHand()).isEmpty()) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.animania.animal_needs",
                    animal.getName(),
                    animal.getData(ModAttachments.HUNGER),
                    animal.getData(ModAttachments.THIRST)), true);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        if (event.getEntity() instanceof Animal animal && AnimalInformation.isAnimaniaAnimal(animal)
                && animal.getLeashHolder() != null) {
            event.setDamageMultiplier(event.getDamageMultiplier()
                    * LegacyConfig.FALL_DAMAGE_REDUCE_MULTIPLIER.get().floatValue());
        }
    }

    private static boolean isWaterContainer(ItemStack stack) {
        return FluidUtil.getFluidContained(stack)
                .map(fluid -> fluid.getFluid().isSame(Fluids.WATER)).orElse(false);
    }

    private static void showCareHeartsOrEnableBreeding(Animal animal,
                                                        net.minecraft.world.entity.player.Player player) {
        if (animal instanceof com.animania.farm.chicken.AnimaniaChicken
                || animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl) {
            animal.level().broadcastEntityEvent(animal, (byte) 18);
        } else {
            animal.setInLove(player);
        }
    }

    private static boolean isSlopContainer(ItemStack stack) {
        return FluidUtil.getFluidContained(stack)
                .map(fluid -> fluid.getFluid().isSame(ModFluids.SLOP.source())).orElse(false);
    }

    private static void emptyOneWaterContainer(PlayerInteractEvent.EntityInteract event, ItemStack held) {
        emptyOneFluidContainer(event, held, true);
    }

    private static void emptyOneFluidContainer(PlayerInteractEvent.EntityInteract event, ItemStack held,
                                                boolean water) {
        FluidTank sink = new FluidTank(1000, fluid -> water
                ? fluid.getFluid().isSame(Fluids.WATER)
                : fluid.getFluid().isSame(ModFluids.SLOP.source()));
        var result = FluidUtil.tryEmptyContainer(held, sink, 1000, event.getEntity(), true);
        if (!result.isSuccess()) return;
        ItemStack empty = result.getResult();
        if (held.getCount() == 1) event.getEntity().setItemInHand(event.getHand(), empty);
        else {
            held.shrink(1);
            if (!event.getEntity().addItem(empty)) event.getEntity().drop(empty, false);
        }
    }

}
