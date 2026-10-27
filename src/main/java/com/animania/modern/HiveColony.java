package com.animania.modern;

import com.animania.farm.world.block.HiveBlock;
import com.animania.farm.world.block.entity.HiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Bees are stored only after their entity data has been saved successfully. */
public final class HiveColony {
    private final HiveBlockEntity hive;
    private java.util.UUID identity = java.util.UUID.randomUUID(), activeSwarm;
    private int away;
    private long smokedUntil, quietUntil, reservationUntil;
    public java.util.UUID identity() { return identity; }
    public int defenders() { return away; }
    public boolean isSmoked() {
        return hive.getLevel() != null && (hive.getLevel().getGameTime() < smokedUntil
                || CampfireBlock.isSmokeyPos(hive.getLevel(), hive.getBlockPos()));
    }
    public void smoke() {
        if (!(hive.getLevel() instanceof ServerLevel level)) return;
        smokedUntil = level.getGameTime() + com.animania.common.config.AnimaniaConfig.BEE_SMOKE_TICKS.get();
        if (activeSwarm != null && level.getEntity(activeSwarm) instanceof DefensiveBeeSwarm swarm) swarm.calm();
        changed();
    }
    public boolean receiveSwarm(java.util.UUID id, List<CompoundTag> returning, int elapsed) {
        if (!id.equals(activeSwarm) || occupants.size() + returning.size() > 3) return false;
        for (CompoundTag record : returning) {
            CompoundTag copy = record.copy();
            copy.putInt("Ticks", copy.getInt("Ticks") + elapsed);
            copy.putInt("Minimum", copy.getInt("Ticks") + 600);
            occupants.add(copy);
        }
        finishSwarm(id);
        return true;
    }
    public void finishSwarm(java.util.UUID id) {
        if (!id.equals(activeSwarm)) return;
        activeSwarm = null; away = 0;
        quietUntil = hive.getLevel().getGameTime() + 200;
        changed();
    }
    public void disturb(net.minecraft.world.entity.player.Player player, boolean broken) {
        if (!(hive.getLevel() instanceof ServerLevel level) || player.isCreative() || player.isSpectator()
                || !com.animania.common.config.AnimaniaConfig.BEE_DEFENSE.get() || isSmoked()
                || !broken && level.getGameTime() < quietUntil) return;
        if (activeSwarm != null) {
            if (broken && level.getEntity(activeSwarm) instanceof DefensiveBeeSwarm swarm) swarm.provoke(player);
            return;
        }
        List<CompoundTag> adults = occupants.stream().filter(record -> record.getCompound("Entity").getInt("Age")
                + record.getInt("Ticks") >= 0 && !record.getCompound("Entity").getBoolean("HasStung")).toList();
        if (adults.isEmpty()) return;
        DefensiveBeeSwarm swarm = com.animania.common.registry.ModEntities.DEFENSIVE_SWARM.get().create(level);
        if (swarm == null) return;
        double temperament = adults.stream().mapToDouble(record -> {
            String id = record.getCompound("Entity").getString("id");
            return id.endsWith("dark_bee") ? com.animania.common.config.AnimaniaConfig.BEE_DARK_DEFENSE.get()
                : id.endsWith("pale_bee") ? com.animania.common.config.AnimaniaConfig.BEE_PALE_DEFENSE.get()
                : com.animania.common.config.AnimaniaConfig.BEE_AMBER_DEFENSE.get();
        }).average().orElse(1);
        swarm.begin(hive.getBlockPos(), identity, player, adults, broken ? 0 : Math.max(10,
                (int)(com.animania.common.config.AnimaniaConfig.BEE_WARNING_TICKS.get() / temperament)));
        BlockPos exit = hive.getBlockPos().relative(hive.getBlockState().getValue(HiveBlock.FACING));
        Vec3 location = Vec3.atCenterOf(exit);
        swarm.moveTo(location.x, location.y, location.z, 0, 0);
        if (!level.noCollision(swarm)) swarm.moveTo(hive.getBlockPos().getX() + .5, hive.getBlockPos().getY() + 1.1, hive.getBlockPos().getZ() + .5, 0, 0);
        if (!level.addFreshEntity(swarm)) return;
        reservationUntil = level.getGameTime() + com.animania.common.config.AnimaniaConfig.BEE_PURSUIT_TICKS.get() + 1200L;
        occupants.removeAll(adults); activeSwarm = swarm.getUUID(); away = adults.size(); changed();
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(broken
                ? "message.animania.bee_defending" : "message.animania.bee_warning"), true);
        level.playSound(null, hive.getBlockPos(), SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1, .8F);
    }
    private final List<CompoundTag> occupants = new ArrayList<>();
    private final java.util.Map<BlockPos, Long> flowers = new java.util.LinkedHashMap<>();

    private boolean validFlower(BlockPos pos, ServerLevel level) {
        return pos != null && pos.closerToCenterThan(Vec3.atCenterOf(hive.getBlockPos()), 32)
                && level.hasChunkAt(pos) && level.getBlockState(pos).is(BlockTags.FLOWERS);
    }
    private void pruneFlowers(ServerLevel level) {
        if (flowers.entrySet().removeIf(entry -> entry.getValue() <= level.getGameTime()
                || level.hasChunkAt(entry.getKey()) && !level.getBlockState(entry.getKey()).is(BlockTags.FLOWERS)))
            hive.setChanged();
    }
    public int knownFlowerCount() {
        if (!(hive.getLevel() instanceof ServerLevel level)) return 0;
        pruneFlowers(level);
        return (int) flowers.keySet().stream().filter(pos -> validFlower(pos, level)).count();
    }
    private void rememberFlower(ModernBee bee, ServerLevel level) {
        BlockPos flower = bee.getSavedFlowerPos();
        if (!bee.hasNectar() || !validFlower(flower, level)) return;
        pruneFlowers(level);
        flowers.remove(flower);
        if (flowers.size() >= 4) flowers.remove(flowers.keySet().iterator().next());
        flowers.put(flower.immutable(), level.getGameTime() + 24_000L);
    }
    private void shareFlower(Bee bee, ServerLevel level) {
        pruneFlowers(level);
        if (validFlower(bee.getSavedFlowerPos(), level)) return;
        var available = flowers.keySet().stream().filter(pos -> validFlower(pos, level)).toList();
        if (!available.isEmpty()) bee.setSavedFlowerPos(available.get(level.random.nextInt(available.size())));
    }
    public HiveColony(HiveBlockEntity hive) { this.hive = hive; }
    public int count() { return occupants.size(); }
    public boolean hasSpace() { return count() + away < 3; }
    public void enter(ModernBee bee) {
        if (!(hive.getLevel() instanceof ServerLevel level) || !hasSpace() || bee.isRemoved()) return;
        com.animania.common.entity.HusbandryMood.caredFor(bee);
        CompoundTag data = new CompoundTag();
        if (!bee.save(data)) return;
        CompoundTag resident = new CompoundTag();
        resident.put("Entity", data);
        resident.putInt("Ticks", 0);
        resident.putInt("Minimum", Math.max(bee.isBaby() ? -bee.getAge() : 0, bee.hasNectar() ? 2400 : 600));
        occupants.add(resident);
        rememberFlower(bee, level);
        bee.discard();
        level.playSound(null, hive.getBlockPos(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1, 1);
        changed();
    }
    public void tick() {
        if (!(hive.getLevel() instanceof ServerLevel level)) return;
        if (activeSwarm != null && level.getGameTime() >= reservationUntil) finishSwarm(activeSwarm);
        if (occupants.isEmpty()) return;
        boolean fire = false;
        for (BlockPos pos : BlockPos.betweenClosed(hive.getBlockPos().offset(-1,-1,-1), hive.getBlockPos().offset(1,1,1)))
            if (level.getBlockState(pos).is(BlockTags.FIRE)) { fire = true; break; }
        for (var iterator = occupants.iterator(); iterator.hasNext();) {
            CompoundTag resident = iterator.next();
            int ticks = resident.getInt("Ticks") + 1;
            resident.putInt("Ticks", ticks);
            if ((fire || ticks >= resident.getInt("Minimum")) && release(resident, fire)) {
                iterator.remove();
                changed();
            }
        }
        hive.setChanged();
    }
    public void releaseAll() {
        occupants.removeIf(resident -> release(resident, true));
        changed();
    }
    private boolean release(CompoundTag resident, boolean emergency) {
        if (!(hive.getLevel() instanceof ServerLevel level)) return false;
        BlockPos pos = hive.getBlockPos();
        BlockPos exit = pos.relative(hive.getBlockState().getValue(HiveBlock.FACING));
        boolean blocked = !level.getBlockState(exit).getCollisionShape(level, exit).isEmpty();
        if (!emergency && (level.isNight() || level.isRaining() || blocked)) return false;
        var entity = EntityType.loadEntityRecursive(resident.getCompound("Entity").copy(), level, e -> e);
        if (!(entity instanceof Bee bee)) return false;
        Vec3 location = Vec3.atCenterOf(blocked ? pos : exit);
        bee.moveTo(location.x, location.y, location.z, bee.getYRot(), bee.getXRot());
        int elapsed = resident.getInt("Ticks");
        int age = bee.getAge();
        bee.setAge(age < 0 ? Math.min(0, age + elapsed) : Math.max(0, age - elapsed));
        boolean nectar = bee.hasNectar() && !emergency;
        if (nectar) bee.dropOffNectar();
        bee.resetTicksWithoutNectarSinceExitingHive();
        bee.setStayOutOfHiveCountdown(400);
        if (bee instanceof ModernBee modern) modern.leaveFarmHive(pos);
        if (!emergency) shareFlower(bee, level);
        if (!level.addFreshEntity(bee)) return false;
        if (nectar) hive.acceptNectar(com.animania.common.entity.HusbandryMood.effect(bee) > 0 ? 125
                : com.animania.common.entity.HusbandryMood.effect(bee) < 0 ? 50 : 100);
        level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1, 1);
        return true;
    }
    private void changed() {
        hive.setChanged();
        if (hive.getLevel() instanceof ServerLevel level)
            level.sendBlockUpdated(hive.getBlockPos(), hive.getBlockState(), hive.getBlockState(), 3);
    }
    public void save(CompoundTag tag) {
        tag.putUUID("ColonyIdentity", identity);
        if (activeSwarm != null) tag.putUUID("DefensiveSwarm", activeSwarm);
        tag.putInt("DefendersAway", away); tag.putLong("SmokedUntil", smokedUntil); tag.putLong("QuietUntil", quietUntil); tag.putLong("ReservationUntil", reservationUntil);
        ListTag list = new ListTag();
        for (CompoundTag resident : occupants) list.add(resident.copy());
        tag.put("AnimaniaBees", list);
        ListTag forage = new ListTag();
        flowers.forEach((pos, expires) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", pos.asLong()); entry.putLong("Expires", expires);
            forage.add(entry);
        });
        tag.put("AnimaniaKnownFlowers", forage);
    }
    public void load(CompoundTag tag) {
        identity = tag.hasUUID("ColonyIdentity") ? tag.getUUID("ColonyIdentity") : java.util.UUID.randomUUID();
        activeSwarm = tag.hasUUID("DefensiveSwarm") ? tag.getUUID("DefensiveSwarm") : null;
        away = activeSwarm == null ? 0 : Math.max(0, Math.min(3, tag.getInt("DefendersAway")));
        smokedUntil = tag.getLong("SmokedUntil"); quietUntil = tag.getLong("QuietUntil"); reservationUntil = tag.getLong("ReservationUntil");
        occupants.clear();
        flowers.clear();
        ListTag forage = tag.getList("AnimaniaKnownFlowers", 10);
        for (int i = 0; i < Math.min(4, forage.size()); i++) {
            CompoundTag entry = forage.getCompound(i);
            flowers.put(BlockPos.of(entry.getLong("Pos")), entry.getLong("Expires"));
        }
        ListTag list = tag.getList("AnimaniaBees", 10);
        for (int i = 0; i < list.size(); i++) occupants.add(list.getCompound(i).copy());
    }
}
