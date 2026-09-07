package com.animania.extra.peafowl;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModSounds;
import com.animania.common.registry.ModItems;
import com.animania.farm.world.block.NestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaPeafowl extends Chicken {
    private static final EntityDataAccessor<Boolean> DISPLAYING =
            SynchedEntityData.defineId(AnimaniaPeafowl.class, EntityDataSerializers.BOOLEAN);
    private int layTimer = 6_000;
    private int featherTimer = 12_000;
    private boolean lookingForNest;

    public AnimaniaPeafowl(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
        if (role() == PeafowlRole.PEACHICK) setBaby(true);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public PeafowlBreed breed() {
        return PeafowlBreed.fromPath(entityPath());
    }

    public PeafowlRole role() {
        return PeafowlRole.fromPath(entityPath());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PEAFOWL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PEAFOWL_HURT.get();
    }

    public boolean isDisplaying() {
        return entityData.get(DISPLAYING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DISPLAYING, false);
    }

    @Override
    public void aiStep() {
        eggTime = Math.max(eggTime, 10_000);
        super.aiStep();
        if (level().isClientSide()) return;
        if (role() == PeafowlRole.PEACHICK && !isBaby()) {
            growIntoAdult();
            return;
        }
        if (role() == PeafowlRole.PEAHEN) {
            if (layTimer > -1) layTimer--;
            else lookingForNest = true;
        }
        if (role() == PeafowlRole.PEACOCK) {
            boolean femaleNearby = !level().getEntitiesOfClass(AnimaniaPeafowl.class,
                    getBoundingBox().inflate(8.0D), bird -> bird.role() == PeafowlRole.PEAHEN).isEmpty();
            entityData.set(DISPLAYING, level().isDay() && femaleNearby);
            if (com.animania.common.config.LegacyConfig.BIRDS_DROP_FEATHERS.get() && --featherTimer <= 0) {
                spawnAtLocation(ModItems.peacockFeather(breed()).get());
                int timer = com.animania.common.config.LegacyConfig.FEATHER_TIMER.get();
                featherTimer = timer + random.nextInt(Math.max(1, timer / 2));
            }
        }
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        PeafowlRole adultRole = random.nextBoolean() ? PeafowlRole.PEAHEN : PeafowlRole.PEACOCK;
        AnimaniaPeafowl adult = ModEntities.peafowl(adultRole, breed()).create(server);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
            com.animania.common.entity.LegacyAnimalNeeds.copyState(this, adult);
            server.addFreshEntity(adult);
            discard();
        }
    }

    public boolean isLookingForNest() {
        return role() == PeafowlRole.PEAHEN && lookingForNest;
    }

    public boolean canUseNest(BlockPos pos) {
        return isLookingForNest() && level().getBlockState(pos).is(ModBlocks.NEST.get())
                && NestBlock.canAcceptPeafowl(level().getBlockState(pos), breed());
    }

    public boolean layEggInNest(BlockPos pos) {
        if (canUseNest(pos) && NestBlock.tryInsertPeafowl(level(), pos, breed())) {
            lookingForNest = false;
            layTimer = com.animania.common.config.LegacyConfig.LAID_TIMER.get() + random.nextInt(100);
            playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "peacock");
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaPeafowl bird) || other == this || isBaby() || bird.isBaby()) return false;
        boolean opposite = role() != bird.role() && role() != PeafowlRole.PEACHICK
                && bird.role() != PeafowlRole.PEACHICK;
        return opposite && ModAttachments.getData(this, ModAttachments.HUNGER) > 20 && ModAttachments.getData(this, ModAttachments.THIRST) > 20
                && ModAttachments.getData(bird, ModAttachments.HUNGER) > 20 && ModAttachments.getData(bird, ModAttachments.THIRST) > 20
                && isInLove() && bird.isInLove()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, bird);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaPeafowl bird)) return;
        AnimalInformation.recordMating(this, bird);
        setAge(6000);
        bird.setAge(6000);
        resetLove();
        bird.resetLove();
        level.broadcastEntityEvent(role() == PeafowlRole.PEAHEN ? this : bird, (byte) 18);
    }

    @Override
    @Nullable
    public Chicken getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        PeafowlBreed child = otherParent instanceof AnimaniaPeafowl bird && random.nextBoolean()
                ? bird.breed() : breed();
        return ModEntities.peafowl(PeafowlRole.PEACHICK, child).create(level);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data, net.minecraft.nbt.CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, spawnTag);
        int timer = com.animania.common.config.LegacyConfig.LAID_TIMER.get();
        layTimer = timer + random.nextInt(timer);
        return result;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        if (role() == PeafowlRole.PEACHICK) return BuiltInLootTables.EMPTY;
        return new ResourceLocation("animania",
                "entities/peacock_" + breed().getSerializedName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LaidTimer", layTimer);
        tag.putBoolean("LookingForNest", lookingForNest);
        tag.putInt("FeatherTimer", featherTimer);
        tag.putBoolean("Displaying", isDisplaying());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        layTimer = tag.getInt("LaidTimer");
        lookingForNest = tag.getBoolean("LookingForNest");
        featherTimer = tag.getInt("FeatherTimer");
        entityData.set(DISPLAYING, tag.getBoolean("Displaying"));
    }
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }
}
