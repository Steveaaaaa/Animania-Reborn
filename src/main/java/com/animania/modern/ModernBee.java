package com.animania.modern;

import com.animania.common.registry.ModEntities;
import com.animania.farm.world.block.HiveBlock;
import com.animania.farm.world.block.entity.HiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public final class ModernBee extends Bee {
    private final String breed;
    private BlockPos farmHive;
    private int returnDelay;
    private boolean returningToFarmHive;
    public boolean isReturningToFarmHive() { return returningToFarmHive; }

    public ModernBee(EntityType<? extends Bee> type, Level level, String breed) {
        super(type, level);
        this.breed = breed;
        goalSelector.addGoal(1, new ReturnToFarmHive());
    }
    public BlockPos husbandryHive() { return farmHive != null ? farmHive : getHivePos(); }
    @Override public boolean canMate(net.minecraft.world.entity.animal.Animal other) {
        return com.animania.common.entity.HusbandryMood.breedingAllowed(this)
                && com.animania.common.entity.HusbandryMood.breedingAllowed(other) && super.canMate(other);
    }
    @Override public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        boolean food = isFood(player.getItemInHand(hand));
        var result = super.mobInteract(player, hand);
        if (food && result.consumesAction() && !level().isClientSide()) com.animania.common.entity.HusbandryMood.caredFor(this);
        return result;
    }
    public String breed() { return breed; }
    public void leaveFarmHive(BlockPos pos) {
        farmHive = pos.immutable();
        returnDelay = 400;
        setStayOutOfHiveCountdown(400);
    }
    @Override public void tick() {
        super.tick();
        if (returnDelay > 0) --returnDelay;
    }
    @Override public Bee getBreedOffspring(ServerLevel level, AgeableMob other) {
        String childBreed = other instanceof ModernBee bee && random.nextBoolean() ? bee.breed : breed;
        ModernBee child = ModEntities.BEE_BREEDS.get(childBreed).get().create(level);
        if (child != null) child.farmHive = farmHive != null ? farmHive
                : other instanceof ModernBee bee ? bee.farmHive : null;
        if (child != null) com.animania.common.entity.HusbandryMood.inherit(child, this);
        return child;
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (farmHive != null) tag.putLong("AnimaniaHive", farmHive.asLong());
        tag.putInt("FarmHiveReturnDelay", returnDelay);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        farmHive = tag.contains("AnimaniaHive") ? BlockPos.of(tag.getLong("AnimaniaHive")) : null;
        returnDelay = Math.max(0, tag.getInt("FarmHiveReturnDelay"));
    }
    private final class ReturnToFarmHive extends Goal {
        private int searchDelay;
        private int travelTicks;
        private net.minecraft.world.level.pathfinder.Path route;
        private final java.util.Map<BlockPos, Long> avoided = new java.util.LinkedHashMap<>();
        ReturnToFarmHive() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        private boolean needsShelter() {
            return (isBaby() || returnDelay == 0 || level().isNight() || level().isRaining())
                    && !isAngry() && !hasStung() && !isLeashed() && !isPassenger() && !isVehicle()
                    && !isOnFire() && !isInLove() && getTarget() == null
                    && (isBaby() || hasNectar() || level().isNight() || level().isRaining());
        }
        private boolean usable(BlockPos pos) {
            if (!level().hasChunkAt(pos) || avoided.containsKey(pos)
                    || !(level().getBlockEntity(pos) instanceof HiveBlockEntity hive) || !hive.colony().hasSpace()) return false;
            BlockPos entrance = pos.relative(hive.getBlockState().getValue(HiveBlock.FACING));
            Vec3 target = Vec3.atCenterOf(entrance);
            if (!level().hasChunkAt(entrance) || !level().getFluidState(entrance).isEmpty()
                    || !level().noCollision(ModernBee.this, getBoundingBox().move(target.subtract(position())))) return false;
            for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1,-1,-1), pos.offset(1,1,1))) {
                if (level().hasChunkAt(near) && level().getBlockState(near).is(net.minecraft.tags.BlockTags.FIRE)) return false;
            }
            return true;
        }
        private void avoid(BlockPos pos) {
            if (pos == null) return;
            if (avoided.size() >= 8) avoided.remove(avoided.keySet().iterator().next());
            avoided.put(pos.immutable(), level().getGameTime() + 600);
        }
        private boolean prepare(BlockPos pos) {
            if (!usable(pos)) return false;
            HiveBlockEntity hive = (HiveBlockEntity) level().getBlockEntity(pos);
            BlockPos entrance = pos.relative(hive.getBlockState().getValue(HiveBlock.FACING));
            var path = getNavigation().createPath(entrance, 0);
            if (path == null || !path.canReach()) { avoid(pos); return false; }
            route = path; farmHive = pos.immutable();
            return true;
        }
        @Override public boolean canUse() {
            if (!needsShelter() || --searchDelay > 0) return false;
            searchDelay = 100 + random.nextInt(100);
            avoided.entrySet().removeIf(entry -> entry.getValue() <= level().getGameTime());
            if (farmHive != null && farmHive.closerToCenterThan(position(), 32) && prepare(farmHive)) return true;
            farmHive = null;
            if (hasHive()) return false;
            var candidates = new java.util.ArrayList<BlockPos>();
            for (BlockPos pos : BlockPos.betweenClosed(blockPosition().offset(-12, -6, -12), blockPosition().offset(12, 6, 12))) {
                if (usable(pos)) candidates.add(pos.immutable());
            }
            candidates.sort(java.util.Comparator.comparingDouble(pos -> pos.distToCenterSqr(position())));
            for (int i = 0; i < Math.min(4, candidates.size()); i++) if (prepare(candidates.get(i))) return true;
            return false;
        }
        @Override public boolean canContinueToUse() {
            return needsShelter() && travelTicks < 600 && farmHive != null && usable(farmHive);
        }
        @Override public void start() {
            travelTicks = 0;
            returningToFarmHive = true;
            getNavigation().moveTo(route, 1.0);
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void tick() {
            ++travelTicks;
            if (!(level().getBlockEntity(farmHive) instanceof HiveBlockEntity hive)) return;
            BlockPos entrance = farmHive.relative(hive.getBlockState().getValue(HiveBlock.FACING));
            Vec3 target = Vec3.atCenterOf(entrance);
            getLookControl().setLookAt(target.x, target.y, target.z);
            if (distanceToSqr(target) < 0.8 && usable(farmHive)) { hive.colony().enter(ModernBee.this); return; }
            if (travelTicks % 40 == 0 && !getNavigation().moveTo(target.x, target.y, target.z, 1.0)) travelTicks = 600;
        }
        @Override public void stop() {
            getNavigation().stop();
            returningToFarmHive = false;
            route = null;
            if (farmHive != null && (travelTicks >= 600 || !usable(farmHive))) {
                avoid(farmHive); farmHive = null;
            }
        }
    }
}
