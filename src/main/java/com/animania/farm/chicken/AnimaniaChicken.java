package com.animania.farm.chicken;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModSounds;
import com.animania.common.config.AnimaniaConfig;
import com.animania.farm.world.block.NestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaChicken extends Chicken {
    private int crowTimer;
    private boolean lookingForNest;
    private int nestLayTimer;

    public AnimaniaChicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
        if (role() == ChickenRole.CHICK) {
            setBaby(true);
        }
        crowTimer = 200 + random.nextInt(200);
        nestLayTimer = com.animania.common.config.LegacyConfig.LAID_TIMER.get() / 2 + random.nextInt(100);
    }

    public ChickenBreed breed() {
        return ChickenBreed.fromEntityPath(entityPath());
    }

    public ChickenRole role() {
        return ChickenRole.fromEntityPath(entityPath());
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    @Override
    public void aiStep() {
        ChickenRole role = role();
        if (!level().isClientSide()) {
            if (role == ChickenRole.HEN && !isBaby()) {
                if (!com.animania.common.config.LegacyConfig.CHICKENS_DROP_EGGS.get()) eggTime = 1000;
                if (nestLayTimer > -1) nestLayTimer--;
                else lookingForNest = true;
            } else if (role != ChickenRole.HEN) {
                lookingForNest = false;
                eggTime = Math.max(eggTime, 1000);
            }
        }

        super.aiStep();

        if (!level().isClientSide() && role == ChickenRole.CHICK && !isBaby()) {
            growIntoAdult();
        } else if (!level().isClientSide() && role == ChickenRole.ROOSTER && !isBaby()) {
            tickCrow();
        }
    }

    private boolean wellCaredFor() {
        return getData(ModAttachments.HUNGER) > 20 && getData(ModAttachments.THIRST) > 20;
    }

    public boolean isLookingForNest() {
        return role() == ChickenRole.HEN && lookingForNest;
    }

    public boolean canUseNest(BlockPos pos) {
        return isLookingForNest() && level().getBlockState(pos).is(ModBlocks.NEST.get())
                && NestBlock.canAccept(level().getBlockState(pos), breed());
    }

    public boolean layEggInNest(BlockPos pos) {
        if (canUseNest(pos) && NestBlock.tryInsert(level(), pos, breed())) {
            playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.0F);
            lookingForNest = false;
            nestLayTimer = com.animania.common.config.LegacyConfig.LAID_TIMER.get() + random.nextInt(100);
            return true;
        }
        return false;
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ChickenRole adultRole = random.nextBoolean() ? ChickenRole.HEN : ChickenRole.ROOSTER;
        AnimaniaChicken adult = ModEntities.chicken(adultRole, breed()).create(serverLevel);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
            com.animania.common.entity.LegacyAnimalNeeds.copyState(this, adult);
            serverLevel.addFreshEntity(adult);
            discard();
        }
    }

    private void tickCrow() {
        long time = level().getDayTime() % 24000L;
        if (--crowTimer <= 0 && (time >= 23000L || time <= 500L)) {
            playSound(ModSounds.ROOSTER_AMBIENT.get(), 0.8F, 0.9F + random.nextFloat() * 0.1F);
            crowTimer = 200 + random.nextInt(200);
        }
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaChicken chicken) || other == this || isBaby() || chicken.isBaby()) {
            return false;
        }
        boolean oppositeSex = role() == ChickenRole.HEN && chicken.role() == ChickenRole.ROOSTER
                || role() == ChickenRole.ROOSTER && chicken.role() == ChickenRole.HEN;
        return oppositeSex && wellCaredFor() && chicken.wellCaredFor() && isInLove() && chicken.isInLove()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, chicken);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof com.animania.extra.amphibian.AnimaniaAmphibian amphibian
                && amphibian.kind() != com.animania.extra.amphibian.AnimaniaAmphibian.Kind.DART_FROG) {
            com.animania.common.entity.LegacyAnimalNeeds.setFed(this, true);
        }
        return hit;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (role() == ChickenRole.HEN) {
            com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaChicken.class, 4,
                    () -> ModEntities.chicken(ChickenRole.ROOSTER, breed()).create(level.getLevel()),
                    () -> ModEntities.chicken(ChickenRole.CHICK, breed()).create(level.getLevel()),
                    companion -> { });
        }
        return result;
    }

    @Override
    @Nullable
    public Chicken getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        ChickenBreed childBreed = otherParent instanceof AnimaniaChicken chicken && random.nextBoolean()
                ? chicken.breed() : breed();
        AnimaniaChicken chick = ModEntities.chicken(ChickenRole.CHICK, childBreed).create(level);
        if (chick != null) AnimalInformation.recordParent(chick, this);
        if (otherParent instanceof AnimaniaChicken chicken) AnimalInformation.recordMating(this, chicken);
        return chick;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "chicken");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CHICKEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CHICKEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CHICKEN_DEATH.get();
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        if (role() == ChickenRole.CHICK) return BuiltInLootTables.EMPTY;
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("animania",
                breed().isPrime() ? "entities/chicken_prime" : "entities/chicken_regular"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CrowTime", crowTimer);
        tag.putBoolean("LookingForNest", lookingForNest);
        tag.putInt("NestLayTimer", nestLayTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CrowTime")) {
            crowTimer = tag.getInt("CrowTime");
        }
        lookingForNest = tag.getBoolean("LookingForNest");
        if (tag.contains("NestLayTimer")) nestLayTimer = tag.getInt("NestLayTimer");
    }
}
