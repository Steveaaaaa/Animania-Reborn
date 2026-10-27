package com.animania.common.entity.ai;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.entity.HusbandryMood;
import com.animania.common.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Visits existing furniture or enrichment surfaces without destroying or creating blocks. */
public final class HusbandryFacilityGoal extends Goal {
    public enum Use { CAT_SCRATCH, CAT_PERCH, CAT_BED, DOG_BED, CATTLE_RUB, PIG_ROOT, RABBIT_DIG, FERRET_HIDE }
    private final Animal animal;
    private final Use use;
    private BlockPos facility;
    private Vec3 destination;
    private Path path;
    private int nextAttempt, travel, remaining, activity;
    private boolean performing;

    public HusbandryFacilityGoal(Animal animal, Use use) {
        this.animal = animal; this.use = use;
        nextAttempt = animal.tickCount + 100 + animal.getRandom().nextInt(500);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }
    public static void install(Animal animal) {
        var kind = HusbandryMood.kind(animal);
        if (kind == null) return;
        switch (kind) {
            case CAT -> { add(animal, Use.CAT_SCRATCH); add(animal, Use.CAT_PERCH); add(animal, Use.CAT_BED); }
            case DOG -> add(animal, Use.DOG_BED);
            case COW -> add(animal, Use.CATTLE_RUB);
            case PIG -> add(animal, Use.PIG_ROOT);
            case RABBIT -> add(animal, Use.RABBIT_DIG);
            case FERRET -> add(animal, Use.FERRET_HIDE);
            default -> { }
        }
    }
    private static void add(Animal animal, Use use) { animal.goalSelector.addGoal(8, new HusbandryFacilityGoal(animal, use)); }
    private boolean matches(BlockPos pos) {
        if (!animal.level().hasChunkAt(pos)) return false;
        var state = animal.level().getBlockState(pos);
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String name = id.getNamespace().equals("animania") ? id.getPath() : "";
        return switch (use) {
            case CAT_SCRATCH -> name.equals("cat_tower") || state.is(BlockTags.LOGS);
            // The tower's 1.5-block collision needs a step alongside it; pathfinding must reach its top.
            case CAT_PERCH -> name.equals("cat_tower") || state.is(BlockTags.LOGS);
            case CAT_BED -> name.equals("cat_bed_1") || name.equals("cat_bed_2");
            case DOG_BED -> name.equals("dog_pillow");
            case CATTLE_RUB -> state.is(BlockTags.LOGS) && animal.level().getBlockState(pos.above()).is(BlockTags.LOGS);
            case PIG_ROOT -> state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT);
            case RABBIT_DIG -> state.is(Blocks.DIRT) || state.is(Blocks.SAND);
            case FERRET_HIDE -> (state.is(BlockTags.PLANKS) || state.is(BlockTags.LOGS)
                    || state.is(Blocks.HAY_BLOCK)) && !state.getCollisionShape(animal.level(), pos).isEmpty();
        };
    }
    private boolean safe(Vec3 point) {
        BlockPos foot = BlockPos.containing(point);
        for (BlockPos nearby : BlockPos.betweenClosed(foot.offset(-1, -1, -1), foot.offset(1, 1, 1))) {
            if (!animal.level().hasChunkAt(nearby)) return false;
            var state = animal.level().getBlockState(nearby);
            if (state.is(BlockTags.FIRE) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CAMPFIRE)
                    || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.CACTUS)
                    || animal.level().getFluidState(nearby).is(net.minecraft.tags.FluidTags.LAVA)) return false;
        }
        if (!animal.level().hasChunkAt(foot) || !animal.level().getFluidState(foot).isEmpty()
                || animal.level().isRainingAt(foot)
                || !animal.level().noCollision(animal, animal.getBoundingBox().move(point.subtract(animal.position())))) return false;
        // Fractional furniture tops also supply ground support.
        if (animal.level().noCollision(animal, animal.getBoundingBox().move(point.subtract(animal.position())).move(0, -.08, 0))) return false;
        return animal.level().getEntitiesOfClass(Animal.class,
                animal.getBoundingBox().move(point.subtract(animal.position())).inflate(.3), other -> other != animal).isEmpty();
    }
    private List<Vec3> positions(BlockPos pos) {
        List<Vec3> points = new ArrayList<>();
        if (use == Use.CAT_SCRATCH || use == Use.CATTLE_RUB) {
            double offset = .55 + animal.getBbWidth() / 2;
            for (Direction d : Direction.Plane.HORIZONTAL)
                points.add(new Vec3(pos.getX() + .5 + d.getStepX() * offset, pos.getY(), pos.getZ() + .5 + d.getStepZ() * offset));
        } else if (use == Use.FERRET_HIDE) {
            points.add(Vec3.atBottomCenterOf(pos.below()));
        } else {
            var shape = animal.level().getBlockState(pos).getCollisionShape(animal.level(), pos);
            if (!shape.isEmpty()) points.add(new Vec3(pos.getX() + .5, pos.getY() + shape.max(Direction.Axis.Y), pos.getZ() + .5));
        }
        return points;
    }
    @Override public boolean canUse() {
        if (animal.tickCount < nextAttempt) return false;
        nextAttempt = animal.tickCount + 400 + animal.getRandom().nextInt(400);
        if (!AnimaniaConfig.FACILITY_INTERACTIONS.get() || !HusbandryActivityGuard.free(animal)
                || animal.getData(ModAttachments.FARM_ACTIVITY) != 0) return false;
        List<BlockPos> sites = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(animal.blockPosition().offset(-5, -1, -5), animal.blockPosition().offset(5, 2, 5))) {
            if (matches(pos)) sites.add(pos.immutable());
        }
        sites.sort(Comparator.comparingDouble(pos -> pos.distSqr(animal.blockPosition())));
        int paths = 0;
        for (BlockPos site : sites) {
            for (Vec3 point : positions(site)) {
                if (!safe(point)) continue;
                if (++paths > 4) return false;
                BlockPos node = BlockPos.containing(point);
                if ((use == Use.CAT_BED || use == Use.DOG_BED) && node.equals(site)) node = site.above();
                Path route = animal.getNavigation().createPath(node, 0);
                if (route == null || !route.canReach()) continue;
                facility = site; destination = point; path = route;
                return true;
            }
        }
        return false;
    }
    @Override public void start() {
        travel = 0; performing = false;
        activity = switch (use) {
            case CAT_SCRATCH -> FarmActivityGoal.POST_SCRATCH;
            case CATTLE_RUB -> FarmActivityGoal.RUB;
            case PIG_ROOT -> FarmActivityGoal.ROOT;
            case RABBIT_DIG -> FarmActivityGoal.SCRATCH;
            case CAT_BED, DOG_BED, FERRET_HIDE -> FarmActivityGoal.PET_REST;
            case CAT_PERCH -> FarmActivityGoal.PET_EXPLORE;
        };
        remaining = FarmActivityGoal.duration(activity);
        animal.getNavigation().moveTo(path, .8);
    }
    @Override public boolean canContinueToUse() {
        return AnimaniaConfig.FACILITY_INTERACTIONS.get() && HusbandryActivityGuard.free(animal)
                && remaining > 0 && travel < 240 && facility != null && matches(facility)
                && (!performing || animal.position().distanceToSqr(destination) < .65
                    && (remaining % 20 != 0 || safe(destination)));
    }
    @Override public void tick() {
        if (!performing) {
            travel++;
            if (animal.position().distanceToSqr(destination) < .3 && Math.abs(animal.getY() - destination.y) < .2) {
                if (!safe(destination)) { travel = 240; return; }
                performing = true;
                animal.getNavigation().stop();
                animal.setData(ModAttachments.FARM_ACTIVITY, activity);
                animal.setData(ModAttachments.FARM_ACTIVITY_START, (int) animal.level().getGameTime());
            } else if (travel % 20 == 0 && animal.getNavigation().isDone()) travel = 240;
            return;
        }
        remaining--;
        animal.getNavigation().stop();
        if ((use == Use.PIG_ROOT || use == Use.RABBIT_DIG) && remaining % 16 == 0
                && animal.level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                    net.minecraft.core.particles.ParticleTypes.BLOCK, animal.level().getBlockState(facility)),
                    animal.getX(), animal.getY() + .08, animal.getZ(), 2, .15, .03, .15, .01);
        }
        if (use == Use.CAT_SCRATCH || use == Use.CATTLE_RUB) {
            double x = facility.getX() + .5 - animal.getX(), z = facility.getZ() + .5 - animal.getZ();
            float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
            // Cattle place their shoulder against the post; cats face it with both paws.
            if (use == Use.CATTLE_RUB) yaw += 90;
            animal.setYRot(net.minecraft.util.Mth.approachDegrees(animal.getYRot(), yaw, 5));
            animal.yBodyRot = animal.getYRot();
            animal.getLookControl().setLookAt(facility.getX() + .5, animal.getEyeY(), facility.getZ() + .5, 5, 10);
        }
    }
    @Override public void stop() {
        if (performing && animal.getData(ModAttachments.FARM_ACTIVITY) == activity) animal.setData(ModAttachments.FARM_ACTIVITY, 0);
        animal.getNavigation().stop(); performing = false; facility = null; destination = null; path = null;
        nextAttempt = animal.tickCount + 1000 + animal.getRandom().nextInt(1800);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
}
