package com.animania.modern;

import com.animania.common.config.AnimaniaConfig;
import com.animania.farm.world.block.entity.HiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A temporary carrier for saved hive residents. Rendered bees do not create additional mobs. */
public final class DefensiveBeeSwarm extends PathfinderMob {
    public static final int WARNING = 0, EMERGING = 1, PURSUIT = 2, RETURNING = 3;
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(DefensiveBeeSwarm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COUNT = SynchedEntityData.defineId(DefensiveBeeSwarm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> BREEDS = SynchedEntityData.defineId(DefensiveBeeSwarm.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> OFFENDER = SynchedEntityData.defineId(DefensiveBeeSwarm.class, EntityDataSerializers.INT);
    public Entity visualOffender() { return level().getEntity(entityData.get(OFFENDER)); }
    private final List<CompoundTag> residents = new ArrayList<>();
    private BlockPos home = BlockPos.ZERO;
    private UUID homeId, responsible;
    private int elapsed, warning, pursuit, returnTicks, emergence = 20;

    public DefensiveBeeSwarm(EntityType<? extends DefensiveBeeSwarm> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
        setNoGravity(true);
        setPersistenceRequired();
        setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, -1);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER_BORDER, -1);
    }
    @Override protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false); nav.setCanFloat(false); nav.setCanPassDoors(true);
        return nav;
    }
    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OFFENDER, -1); entityData.define(STATE, WARNING); entityData.define(COUNT, 0); entityData.define(BREEDS, "amber");
    }
    public int defenseState() { return entityData.get(STATE); }
    public int residentCount() { return entityData.get(COUNT); }
    public String[] breeds() { return entityData.get(BREEDS).split(","); }
    public void begin(BlockPos pos, UUID colony, LivingEntity attacker, List<CompoundTag> bees, int delay) {
        home = pos.immutable(); homeId = colony; responsible = attacker.getUUID();
        bees.forEach(record -> residents.add(record.copy()));
        warning = delay; pursuit = AnimaniaConfig.BEE_PURSUIT_TICKS.get();
        entityData.set(STATE, delay == 0 ? EMERGING : WARNING);
        syncResidents();
    }
    private void syncResidents() {
        entityData.set(COUNT, residents.size());
        entityData.set(BREEDS, String.join(",", residents.stream().map(record -> {
            String id = record.getCompound("Entity").getString("id");
            return id.endsWith("dark_bee") ? "dark" : id.endsWith("pale_bee") ? "pale" : "amber";
        }).toList()));
    }
    public void provoke(LivingEntity attacker) {
        responsible = attacker.getUUID();
        if (defenseState() == WARNING) entityData.set(STATE, EMERGING);
    }
    public void calm() {
        if (defenseState() == RETURNING) return;
        entityData.set(STATE, RETURNING); setTarget(null); navigation.stop();
    }
    private HiveColony colony() {
        if (!level().hasChunkAt(home) || !(level().getBlockEntity(home) instanceof HiveBlockEntity hive)) return null;
        return homeId != null && homeId.equals(hive.colony().identity()) ? hive.colony() : null;
    }
    private boolean validTarget(LivingEntity target) {
        return target != null && target.isAlive() && !target.isSpectator()
                && !(target instanceof Player p && p.isCreative())
                && (target instanceof Player || AnimaniaConfig.BEE_ATTACK_ANIMALS.get())
                && target.position().distanceToSqr(Vec3.atCenterOf(home)) <= Math.pow(AnimaniaConfig.BEE_PURSUIT_RADIUS.get(), 2)
                && !target.isUnderWater();
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (residents.isEmpty()) { discard(); return; }
        elapsed++;
        if (tickCount % 25 == 0) playSound(defenseState() == PURSUIT ? SoundEvents.BEE_LOOP_AGGRESSIVE : SoundEvents.BEE_LOOP, .55F, 1.15F);
        LivingEntity offender = responsible != null && server.getEntity(responsible) instanceof LivingEntity living ? living : null;
        entityData.set(OFFENDER, offender == null ? -1 : offender.getId());
        HiveColony colony = colony();
        if (!AnimaniaConfig.BEE_DEFENSE.get() || !validTarget(offender) || isInWaterOrBubble()
                || colony != null && colony.isSmoked()) calm();
        if (defenseState() == WARNING) {
            if (offender.distanceToSqr(Vec3.atCenterOf(home)) > 36) { calm(); return; }
            if (--warning <= 0) entityData.set(STATE, EMERGING);
        } else if (defenseState() == EMERGING) {
            if (--emergence <= 0) entityData.set(STATE, PURSUIT);
        } else if (defenseState() == PURSUIT) {
            if (--pursuit <= 0) { calm(); return; }
            if (tickCount % 10 == 0) navigation.moveTo(offender.getX(), offender.getY() + .7, offender.getZ(), 1.2);
            if (tickCount % 20 == 0 && distanceToSqr(offender) < 2.25 && getSensing().hasLineOfSight(offender)) {
                float protection = BeekeepingProtection.protection(offender);
                float damage = AnimaniaConfig.BEE_SWARM_DAMAGE.get().floatValue() * (1 - protection);
                if (damage > 0 && offender.hurt(damageSources().mobAttack(this), damage))
                    playSound(SoundEvents.BEE_STING, .7F, 1.1F);
                BeekeepingProtection.wear(offender);
            }
        } else {
            returnTicks++;
            if (colony != null && distanceToSqr(Vec3.atCenterOf(home)) < 4 && colony.receiveSwarm(getUUID(), residents, elapsed)) {
                residents.clear(); discard(); return;
            }
            if (tickCount % 20 == 0) navigation.moveTo(home.getX() + .5, home.getY() + 1, home.getZ() + .5, 1);
            // A removed/blocked/unloaded home must not keep the original bees captive forever.
            if (returnTicks >= 200 || level().hasChunkAt(home) && colony == null) releaseResidents(server);
        }
    }
    private void releaseResidents(ServerLevel server) {
        residents.removeIf(record -> {
            Entity loaded = EntityType.loadEntityRecursive(record.getCompound("Entity").copy(), server, e -> e);
            if (!(loaded instanceof Bee bee)) return false;
            bee.moveTo(getX(), getY(), getZ(), getYRot(), 0);
            bee.stopBeingAngry(); bee.setTarget(null); bee.setStayOutOfHiveCountdown(400);
            int age = bee.getAge(), passed = record.getInt("Ticks") + elapsed;
            bee.setAge(age < 0 ? Math.min(0, age + passed) : Math.max(0, age - passed));
            if (bee instanceof ModernBee modern) modern.leaveFarmHive(home);
            return server.addFreshEntity(bee);
        });
        syncResidents();
        if (residents.isEmpty()) { HiveColony colony = colony(); if (colony != null) colony.finishSwarm(getUUID()); discard(); }
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (!(level() instanceof ServerLevel server) || amount <= 0) return false;
        if (source.getEntity() instanceof LivingEntity attacker && validTarget(attacker)) {
            responsible = attacker.getUUID(); entityData.set(STATE, PURSUIT);
            // Repeated attacks do not extend the total pursuit time.
        }
        setHealth(Math.max(1, getHealth() - amount));
        if (getHealth() <= 1 || source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) { calm(); releaseResidents(server); }
        return true;
    }
    @Override public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED && !residents.isEmpty() && level() instanceof ServerLevel server) {
            calm(); releaseResidents(server); return;
        }
        super.remove(reason);
    }
    @Override public boolean isPushable() { return false; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("Home", home.asLong());
        if (homeId != null) tag.putUUID("Colony", homeId);
        if (responsible != null) tag.putUUID("Responsible", responsible);
        tag.putInt("DefenseState", defenseState()); tag.putInt("Elapsed", elapsed);
        tag.putInt("Emergence", emergence); tag.putInt("Warning", warning); tag.putInt("Pursuit", pursuit); tag.putInt("Return", returnTicks);
        ListTag list = new ListTag(); residents.forEach(record -> list.add(record.copy())); tag.put("Residents", list);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        home = BlockPos.of(tag.getLong("Home")); homeId = tag.hasUUID("Colony") ? tag.getUUID("Colony") : null;
        responsible = tag.hasUUID("Responsible") ? tag.getUUID("Responsible") : null;
        entityData.set(STATE, Math.max(0, Math.min(3, tag.getInt("DefenseState"))));
        emergence = tag.getInt("Emergence"); elapsed = tag.getInt("Elapsed"); warning = tag.getInt("Warning"); pursuit = tag.getInt("Pursuit"); returnTicks = tag.getInt("Return");
        residents.clear(); ListTag list = tag.getList("Residents", 10);
        for (int i = 0; i < list.size(); i++) residents.add(list.getCompound(i).copy());
        syncResidents(); setNoGravity(true);
    }
}
