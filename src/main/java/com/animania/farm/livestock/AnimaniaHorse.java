package com.animania.farm.livestock;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaHorse extends Horse {
    private static final int GESTATION_TICKS = 20_000;
    private static final EntityDataAccessor<Integer> COAT =
            SynchedEntityData.defineId(AnimaniaHorse.class, EntityDataSerializers.INT);
    private boolean pregnant;
    private int gestation;

    public boolean isPullingVehicle() {
        return !level().getEntitiesOfClass(com.animania.farm.vehicle.FarmVehicleEntity.class,
                getBoundingBox().inflate(8.0D), vehicle -> vehicle.isPulledBy(this)).isEmpty();
    }

    public AnimaniaHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
        if (role() == FarmAnimalRole.YOUNG) setBaby(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COAT, 0);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public FarmAnimalRole role() {
        return FarmAnimalRole.horseRole(entityPath());
    }

    public int coat() {
        return entityData.get(COAT);
    }

    private boolean wellCaredFor() {
        return ModAttachments.getData(this, ModAttachments.HUNGER) > 20 && ModAttachments.getData(this, ModAttachments.THIRST) > 20;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "horse");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HORSE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HORSE_HURT.get();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && role() == FarmAnimalRole.YOUNG && !isBaby()) growIntoAdult();
        else if (!level().isClientSide() && role() == FarmAnimalRole.FEMALE && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        FarmAnimalRole adultRole = random.nextBoolean() ? FarmAnimalRole.FEMALE : FarmAnimalRole.MALE;
        AnimaniaHorse adult = ModEntities.horse(adultRole).create(server);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.entityData.set(COAT, coat());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
            adult.setTamed(isTamed());
            adult.setOwnerUUID(getOwnerUUID());
            com.animania.common.entity.LegacyAnimalNeeds.copyState(this, adult);
            server.addFreshEntity(adult);
            discard();
        }
    }

    private void giveBirth() {
        if (!(level() instanceof ServerLevel server)) return;
        if (com.animania.common.config.LegacyBreedingRules.shouldLosePregnancy(this, random)) {
            pregnant = false;
            com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
            return;
        }
        for (int i = 0; i < com.animania.common.config.LegacyBreedingRules.litterSize(random); i++) {
            AnimaniaHorse foal = ModEntities.horse(FarmAnimalRole.YOUNG).create(server);
            if (foal != null) {
                AnimalInformation.recordParent(foal, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(foal,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                foal.moveTo(getX(), getY() + 0.2, getZ(), getYRot(), 0.0F);
                foal.entityData.set(COAT, random.nextBoolean() ? coat() : random.nextInt(6));
                server.addFreshEntity(foal);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaHorse horse) || other == this || isBaby() || horse.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(horse)) return false;
        AnimaniaHorse female = role() == FarmAnimalRole.FEMALE ? this
                : horse.role() == FarmAnimalRole.FEMALE ? horse : null;
        boolean opposite = role() != horse.role() && role() != FarmAnimalRole.YOUNG
                && horse.role() != FarmAnimalRole.YOUNG;
        return opposite && female != null && !female.pregnant && wellCaredFor() && horse.wellCaredFor()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, horse);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaHorse horse)) return;
        AnimaniaHorse female = role() == FarmAnimalRole.FEMALE ? this : horse;
        AnimalInformation.recordMating(this, horse);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        setAge(6000);
        horse.setAge(6000);
        resetLove();
        horse.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, net.minecraft.nbt.CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, spawnTag);
        entityData.set(COAT, random.nextInt(6));
        if (role() == FarmAnimalRole.FEMALE) {
            com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaHorse.class, 8,
                    () -> ModEntities.horse(FarmAnimalRole.MALE).create(level.getLevel()),
                    () -> ModEntities.horse(FarmAnimalRole.YOUNG).create(level.getLevel()),
                    companion -> ((AnimaniaHorse) companion).entityData.set(COAT, coat()));
        }
        return result;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() && player.isShiftKeyDown() && !level().isClientSide()) {
            player.displayClientMessage(Component.translatable(pregnant
                    ? "message.animania.pregnant" : "message.animania.not_pregnant", gestation), true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        AnimaniaHorse foal = ModEntities.horse(FarmAnimalRole.YOUNG).create(level);
        if (foal != null) foal.entityData.set(COAT,
                otherParent instanceof AnimaniaHorse horse && random.nextBoolean() ? horse.coat() : coat());
        return foal;
    }

    @Override
    public ResourceLocation getDefaultLootTable() {
        if (role() == FarmAnimalRole.YOUNG) return BuiltInLootTables.EMPTY;
        return new ResourceLocation("animania", "entities/horse");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putInt("Gestation", gestation);
        tag.putInt("ColorNumber", coat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        gestation = tag.getInt("Gestation");
        entityData.set(COAT, Math.floorMod(tag.getInt("ColorNumber"), 6));
    }
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }
}
