package com.animania.farm.livestock;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModSounds;
import com.animania.common.registry.ModItems;
import com.animania.farm.dairy.MilkType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaGoat extends Goat {
    private static final int GESTATION_TICKS = 20_000;
    private static final EntityDataAccessor<Boolean> ANGORA_SHEARED =
            SynchedEntityData.defineId(AnimaniaGoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SPOOKED_TIMER =
            SynchedEntityData.defineId(AnimaniaGoat.class, EntityDataSerializers.FLOAT);
    private boolean pregnant;
    private boolean milkable;
    private int gestation;
    private GoatBreed mateBreed;
    private int woolRegrowth;

    public AnimaniaGoat(EntityType<? extends Goat> type, Level level) {
        super(type, level);
        if (role() == FarmAnimalRole.YOUNG) setBaby(true);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public GoatBreed breed() {
        return GoatBreed.fromPath(entityPath());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.PanicGoal(this, 1.5D));
        goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(11, new com.animania.common.entity.ai.LegacyIdleLookGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        // The modern Goat brain installs unrelated long-jump/ram activities and
        // overwrites navigation. Animania 1.12 uses the goal selector exclusively.
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGORA_SHEARED, false);
        builder.define(SPOOKED_TIMER, 0.0F);
    }

    public FarmAnimalRole role() {
        return FarmAnimalRole.goatRole(entityPath());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "goat");
    }

    public boolean isAngoraSheared() {
        return breed() == GoatBreed.ANGORA && entityData.get(ANGORA_SHEARED);
    }

    public float spookedTimer() {
        return entityData.get(SPOOKED_TIMER);
    }

    public boolean isSpooked() {
        return breed() == GoatBreed.FAINTING && spookedTimer() > 0.0F;
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if (!level().isClientSide() && breed() == GoatBreed.FAINTING
                && entity instanceof Player player && player.isSprinting()) {
            entityData.set(SPOOKED_TIMER, 1.0F);
            setJumping(true);
        }
    }

    private boolean wellCaredFor() {
        return getData(ModAttachments.HUNGER) > 20 && getData(ModAttachments.THIRST) > 20;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return role() == FarmAnimalRole.YOUNG ? ModSounds.KID_AMBIENT.get() : ModSounds.GOAT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return role() == FarmAnimalRole.YOUNG ? ModSounds.KID_HURT.get() : ModSounds.GOAT_HURT.get();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && isSpooked()) {
            boolean initialJump = spookedTimer() == 1.0F;
            setNoAi(true);
            float timer = Math.max(0.0F, spookedTimer() - 0.01F);
            entityData.set(SPOOKED_TIMER, timer);
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            setJumping(initialJump || timer > 0.10F && timer <= 0.20F);
            if (timer == 0.0F) { setJumping(false); setNoAi(false); }
        }
        if (!level().isClientSide() && woolRegrowth > 0 && --woolRegrowth == 0) {
            entityData.set(ANGORA_SHEARED, false);
        }
        if (!level().isClientSide() && role() == FarmAnimalRole.YOUNG && !isBaby()) {
            growIntoAdult();
        } else if (!level().isClientSide() && role() == FarmAnimalRole.FEMALE && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        FarmAnimalRole adultRole = random.nextBoolean() ? FarmAnimalRole.FEMALE : FarmAnimalRole.MALE;
        AnimaniaGoat adult = ModEntities.goat(adultRole, breed()).create(server);
        if (adult != null) {
            copyInto(adult);
            server.addFreshEntity(adult);
            discard();
        }
    }

    private void giveBirth() {
        if (!(level() instanceof ServerLevel server)) return;
        if (com.animania.common.config.LegacyBreedingRules.shouldLosePregnancy(this, random)) {
            pregnant = false;
            com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
            mateBreed = null;
            return;
        }
        for (int i = 0; i < com.animania.common.config.LegacyBreedingRules.litterSize(random); i++) {
            GoatBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaGoat kid = ModEntities.goat(FarmAnimalRole.YOUNG, childBreed).create(server);
            if (kid != null) {
                AnimalInformation.recordParent(kid, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(kid,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                kid.moveTo(getX(), getY() + 0.2, getZ(), getYRot(), 0.0F);
                server.addFreshEntity(kid);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        milkable = true;
        mateBreed = null;
    }

    private void copyInto(AnimaniaGoat adult) {
        adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        adult.setCustomName(getCustomName());
        adult.setCustomNameVisible(isCustomNameVisible());
        com.animania.common.entity.LegacyAnimalNeeds.copyState(this, adult);
    }

    public void childMatured() {
        milkable = false;
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaGoat goat) || other == this || isBaby() || goat.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(goat)) return false;
        AnimaniaGoat female = role() == FarmAnimalRole.FEMALE ? this : goat.role() == FarmAnimalRole.FEMALE ? goat : null;
        boolean opposite = role() != goat.role() && role() != FarmAnimalRole.YOUNG && goat.role() != FarmAnimalRole.YOUNG;
        return opposite && female != null && !female.pregnant && wellCaredFor() && goat.wellCaredFor()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, goat);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (role() == FarmAnimalRole.FEMALE) {
            var family = com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaGoat.class, 8,
                    () -> ModEntities.goat(FarmAnimalRole.MALE, breed()).create(level.getLevel()),
                    () -> ModEntities.goat(FarmAnimalRole.YOUNG, breed()).create(level.getLevel()),
                    companion -> { });
            if (family == com.animania.common.entity.LegacyNaturalFamily.Result.YOUNG) milkable = true;
        }
        return result;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaGoat goat)) return;
        AnimaniaGoat female = role() == FarmAnimalRole.FEMALE ? this : goat;
        AnimaniaGoat male = female == this ? goat : this;
        AnimalInformation.recordMating(this, goat);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        goat.setAge(6000);
        resetLove();
        goat.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SHEARS) && breed() == GoatBreed.ANGORA && role() != FarmAnimalRole.YOUNG && woolRegrowth == 0) {
            if (!level().isClientSide()) {
                spawnAtLocation(new ItemStack(Items.WHITE_WOOL, 2 + random.nextInt(2)));
                stack.hurtAndBreak(1, player, getSlotForHand(hand));
                woolRegrowth = com.animania.common.config.LegacyConfig.WOOL_REGROWTH_TIMER.get();
                entityData.set(ANGORA_SHEARED, true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (stack.is(Items.BUCKET)) {
            if (role() == FarmAnimalRole.FEMALE && milkable && wellCaredFor() && !isBaby()) {
                if (!level().isClientSide()) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                            new ItemStack(ModItems.milkBucket(MilkType.GOAT).get())));
                    com.animania.common.entity.LegacyAnimalNeeds.setWatered(this, false);
                    milkable = false;
                }
                player.playSound(getMilkingSound(), 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
            return InteractionResult.PASS;
        }
        if (stack.isEmpty() && player.isShiftKeyDown() && !level().isClientSide()) {
            player.displayClientMessage(Component.translatable(pregnant
                    ? "message.animania.pregnant" : milkable
                    ? "message.animania.milkable" : "message.animania.not_pregnant", gestation), true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public Goat getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        GoatBreed childBreed = otherParent instanceof AnimaniaGoat goat && random.nextBoolean() ? goat.breed() : breed();
        return ModEntities.goat(FarmAnimalRole.YOUNG, childBreed).create(level);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        if (role() == FarmAnimalRole.YOUNG) return BuiltInLootTables.EMPTY;
        return breed().isPrime()
                ? ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("animania", "entities/goat_prime"))
                : ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("animania", "entities/goat_regular"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putBoolean("HasKids", milkable);
        tag.putInt("Gestation", gestation);
        tag.putInt("WoolRegrowth", woolRegrowth);
        tag.putFloat("SpookedTimer", spookedTimer());
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        milkable = tag.getBoolean("HasKids");
        gestation = tag.getInt("Gestation");
        woolRegrowth = tag.getInt("WoolRegrowth");
        entityData.set(SPOOKED_TIMER, Math.max(0.0F, tag.getFloat("SpookedTimer")));
        entityData.set(ANGORA_SHEARED, woolRegrowth > 0);
        if (tag.contains("MateBreed")) mateBreed = GoatBreed.fromPath(tag.getString("MateBreed"));
    }
}
