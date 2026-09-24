package com.animania.common.entity.ai;

import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Bed-searching sleep AI corresponding to GenericAISleep in Animania 1.12. */
public final class LegacySleepGoal extends LegacySearchBlockGoal {
    private static final java.util.Map<Animal, java.lang.ref.WeakReference<LegacySleepGoal>> INSTANCES = new java.util.WeakHashMap<>();
    private final Animal sleeper;
    private final Block preferred;
    private final Block backup;
    private int delay;
    private int roostTravel;

    private boolean roostingBird() {
        return sleeper instanceof com.animania.farm.chicken.AnimaniaChicken
                || sleeper instanceof com.animania.extra.peafowl.AnimaniaPeafowl;
    }

    public LegacySleepGoal(PathfinderMob mob, Animal sleeper) {
        super(mob, 0.8D, DestinationOffsets.UP);
        this.sleeper = sleeper;
        INSTANCES.put(sleeper, new java.lang.ref.WeakReference<>(this));
        String key = bedKey(sleeper);
        this.preferred = resolve(LegacyConfig.PREFERRED_BEDS.get(key).get());
        this.backup = resolve(LegacyConfig.BACKUP_BEDS.get(key).get());
    }

    @Override
    public boolean canUse() {
        if (!LegacyConfig.ANIMALS_SLEEP.get() || sleeper.isPassenger() || sleeper.isVehicle() || sleeper.isLeashed()
                || sleeper instanceof TamableAnimal tame && tame.isInSittingPose()) return false;
        if (++delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get() + sleeper.getRandom().nextInt(100)) {
            return false;
        }
        if (sleeper.getData(ModAttachments.SLEEPING)) return false;
        if (roostingBird() && (sleeper.hurtTime > 0 || sleeper.getTarget() != null
                || sleeper.isOnFire() || sleeper.isInWaterOrBubble() || FarmHerdGoal.hasFoodLure(sleeper))) return false;
        return shouldSleepNow(sleeper) && !sleeper.level().isRainingAt(sleeper.blockPosition())
                && sleeper.getRandom().nextInt(3) == 0 && searchForDestination();
    }

    /** StayAsleep owns MOVE/LOOK, so the sleeping goal cannot be polled by
     * GoalSelector. Preserve its original three-tick candidate-check cadence here. */
    public static void checkSleepingWake(Animal animal) {
        var reference = INSTANCES.get(animal);
        LegacySleepGoal goal = reference == null ? null : reference.get();
        if (goal == null || animal.tickCount % 3 != 0) return;
        if (++goal.delay <= LegacyConfig.TICKS_BETWEEN_AI_FIRINGS.get() + animal.getRandom().nextInt(100)) return;
        goal.delay = 0;
        if (!shouldSleepNow(animal) || animal.isOnFire()
                || animal.level().isRainingAt(animal.blockPosition())
                && animal.level().canSeeSky(animal.blockPosition())) {
            animal.setData(ModAttachments.SLEEPING, false);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !sleeper.getData(ModAttachments.SLEEPING)
                && shouldSleepNow(sleeper) && (!roostingBird() || roostTravel < 240
                    && sleeper.hurtTime == 0 && sleeper.getTarget() == null && !sleeper.isOnFire()
                    && !sleeper.isInWaterOrBubble() && !FarmHerdGoal.hasFoodLure(sleeper));
    }

    private boolean clearRoost(BlockPos pos) {
        var box = sleeper.getBoundingBox().move(pos.getX() + 0.5 - sleeper.getX(),
                pos.getY() + 1 - sleeper.getY(), pos.getZ() + 0.5 - sleeper.getZ());
        return level.getBlockState(pos).isFaceSturdy(level, pos, net.minecraft.core.Direction.UP)
                && level.getFluidState(pos.above()).isEmpty() && !level.isRainingAt(pos.above())
                && level.noCollision(sleeper, box);
    }

    @Override public void start() {
        roostTravel = 0;
        super.start();
    }

    @Override public void tick() {
        if (!roostingBird()) { super.tick(); return; }
        if (seekingBlockPos == null) return;
        roostTravel++;
        double dx = sleeper.getX() - seekingBlockPos.getX() - 0.5;
        double dz = sleeper.getZ() - seekingBlockPos.getZ() - 0.5;
        // The generic search accepts a wide radius; a bird must actually reach its perch.
        if (dx * dx + dz * dz < 0.36 && Math.abs(sleeper.getY() - seekingBlockPos.getY() - 1) < 0.35
                && sleeper.onGround()) onArriveAtDestination();
        else if (roostTravel % 20 == 0) sleeper.getNavigation().moveTo(
                seekingBlockPos.getX() + 0.5, seekingBlockPos.getY() + 1, seekingBlockPos.getZ() + 0.5, 0.8);
    }

    @Override
    protected boolean shouldMoveTo(BlockPos pos) {
        if (roostingBird()) {
            var state = level.getBlockState(pos);
            return (state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.tags.BlockTags.PLANKS))
                    && clearRoost(pos)
                    && !level.isRainingAt(pos.above())
                    && level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()
                    && level.getEntitiesOfClass(Animal.class, new net.minecraft.world.phys.AABB(pos.above()).inflate(0.3),
                        other -> other != sleeper).isEmpty();
        }
        return preferred != Blocks.AIR && level.getBlockState(pos).is(preferred);
    }

    @Override
    protected boolean hasSecondaryTarget() {
        return roostingBird() || backup != Blocks.AIR;
    }

    @Override
    protected boolean shouldMoveToSecondary(BlockPos pos) {
        return (level.getBlockState(pos).is(backup)
                || roostingBird() && level.getBlockState(pos).is(preferred))
                && (!roostingBird() || clearRoost(pos));
    }

    @Override
    protected boolean targetStillValid() {
        if (seekingBlockPos == null) return false;
        Block block = level.getBlockState(seekingBlockPos).getBlock();
        return shouldMoveTo(seekingBlockPos) || shouldMoveToSecondary(seekingBlockPos);
    }

    @Override
    protected void onArriveAtDestination() {
        sleeper.setData(ModAttachments.SLEEPING, true);
        sleeper.getNavigation().stop();
        delay = 0;
    }

    public static boolean nocturnalWildCatOrFox(Animal animal) {
        return animal instanceof com.animania.catsdogs.cat.AnimaniaCat cat
                    && cat.breed() == com.animania.catsdogs.cat.CatBreed.OCELOT
                || animal instanceof com.animania.catsdogs.dog.AnimaniaDog dog
                    && dog.breed() == com.animania.catsdogs.dog.DogBreed.FOX;
    }

    public static boolean shouldSleepNow(Animal animal) {
        long time = animal.level().getDayTime() % 24_000L;
        if (nocturnalWildCatOrFox(animal)) return time >= 2_000L && time < 10_000L;
        if (animal instanceof AnimaniaRodent rodent
                && (rodent.kind() == AnimaniaRodent.Kind.HAMSTER || rodent.kind().isHedgehog())) {
            return time < 13_000L;
        }
        if (animal instanceof com.animania.extra.rabbit.AnimaniaRabbit) {
            return time >= 2_000L && time < 10_000L || time >= 17_000L && time < 20_000L;
        }
        if (animal instanceof AnimaniaRodent rodent && rodent.kind().isFerret()) {
            long phase = Math.floorMod(time + Math.floorMod(animal.getUUID().getLeastSignificantBits(), 6000L), 6000L);
            return phase < 4500L;
        }
        return time >= 13_000L;
    }

    private static String bedKey(Animal animal) {
        if (animal instanceof com.animania.farm.livestock.AnimaniaCow) return "cow";
        if (animal instanceof com.animania.farm.livestock.AnimaniaGoat || animal instanceof com.animania.modern.MountainGoat) return "goat";
        if (animal instanceof com.animania.farm.livestock.AnimaniaHorse) return "horse";
        if (animal instanceof com.animania.farm.livestock.AnimaniaPig) return "pig";
        if (animal instanceof com.animania.farm.livestock.AnimaniaSheep) return "sheep";
        if (animal instanceof com.animania.farm.chicken.AnimaniaChicken) return "chicken";
        if (animal instanceof com.animania.extra.peafowl.AnimaniaPeafowl) return "peacock";
        if (animal instanceof com.animania.extra.rabbit.AnimaniaRabbit) return "rabbit";
        if (animal instanceof com.animania.catsdogs.cat.AnimaniaCat) return "cat";
        if (animal instanceof com.animania.catsdogs.dog.AnimaniaDog) return "dog";
        if (animal instanceof AnimaniaRodent rodent) {
            if (rodent.kind() == AnimaniaRodent.Kind.HAMSTER) return "hamster";
            if (rodent.kind().isHedgehog()) return "hedgehog";
            return "ferret";
        }
        throw new IllegalArgumentException("No legacy bed mapping for " + animal.getType());
    }

    private static Block resolve(String configured) {
        if (configured == null || configured.isBlank()) return Blocks.AIR;
        if (configured.equals("minecraft:grass")) return Blocks.GRASS_BLOCK;
        ResourceLocation id = ResourceLocation.tryParse(configured);
        return id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(id);
    }
}
