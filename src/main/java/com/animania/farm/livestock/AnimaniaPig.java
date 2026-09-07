package com.animania.farm.livestock;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;

import java.util.EnumSet;

import javax.annotation.Nullable;

public final class AnimaniaPig extends Pig {
    private static final int GESTATION_TICKS = 20_000;
    private static final int MUD_DURATION = 12_000;
    private static final EntityDataAccessor<Boolean> MUDDY =
            SynchedEntityData.defineId(AnimaniaPig.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_MUD =
            SynchedEntityData.defineId(AnimaniaPig.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> MUD_AMOUNT =
            SynchedEntityData.defineId(AnimaniaPig.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPLASH_TIMER =
            SynchedEntityData.defineId(AnimaniaPig.class, EntityDataSerializers.FLOAT);
    private float splashTimer;
    private boolean pregnant;
    private int gestation;
    private PigBreed mateBreed;
    private int mudTicks;
    private boolean played = true;
    private int playedTicks = MUD_DURATION;

    public AnimaniaPig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
        if (role() == FarmAnimalRole.YOUNG) setBaby(true);
        playedTicks = com.animania.common.config.LegacyConfig.PLAY_TIMER.get() + random.nextInt(100);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public PigBreed breed() {
        return PigBreed.fromPath(entityPath());
    }

    public FarmAnimalRole role() {
        return FarmAnimalRole.pigRole(entityPath());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "pig");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MUDDY, false);
        builder.define(IN_MUD, false);
        builder.define(MUD_AMOUNT, 0.0F);
        builder.define(SPLASH_TIMER, 0.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new com.animania.common.entity.ai.LegacyFindMudGoal(this));
    }

    public boolean isMuddy() {
        return entityData.get(MUDDY);
    }

    public boolean isInMud() { return entityData.get(IN_MUD); }
    public float splashTimer() { return entityData.get(SPLASH_TIMER); }
    public float mudAmount() { return entityData.get(MUD_AMOUNT); }
    public boolean hasPlayed() { return played; }
    public void refreshPlay() {
        played = true;
        playedTicks = com.animania.common.config.LegacyConfig.PLAY_TIMER.get() + random.nextInt(100);
    }
    public static boolean isMud(Level level, BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        return block == ModBlocks.MUD.get() || BuiltInRegistries.BLOCK.getKey(block).getPath().equals("mud");
    }

    private boolean wellCaredFor() {
        return getData(ModAttachments.HUNGER) > 20 && getData(ModAttachments.THIRST) > 20;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (role()) {
            case YOUNG -> ModSounds.PIGLET_AMBIENT.get();
            case MALE -> ModSounds.HOG_AMBIENT.get();
            case FEMALE -> ModSounds.PIG_AMBIENT.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return role() == FarmAnimalRole.YOUNG ? ModSounds.PIGLET_HURT.get() : ModSounds.PIG_HURT.get();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) tickMudAndTruffles();
        if (!level().isClientSide() && role() == FarmAnimalRole.YOUNG && !isBaby()) growIntoAdult();
        else if (!level().isClientSide() && role() == FarmAnimalRole.FEMALE && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    private void tickMudAndTruffles() {
        boolean inMud = isMud(level(), blockPosition());
        boolean wasInMud = isInMud();
        entityData.set(IN_MUD, inMud);
        if (inMud) {
            entityData.set(MUDDY, true);
            if (!wasInMud) splashTimer = 1.0F;
            splashTimer = Math.max(0.0F, splashTimer - 0.045F);
            if (splashTimer <= 0.0F) entityData.set(MUD_AMOUNT, 1.0F);
            refreshPlay();
            addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 2, 4, false, false));
        } else if (isInWaterOrRain()) {
            entityData.set(MUDDY, false);
            entityData.set(MUD_AMOUNT, 0.0F);
            splashTimer = 0.0F;
        } else {
            entityData.set(MUDDY, false);
            if (mudAmount() > 0.0F && random.nextInt(3) == 0)
                entityData.set(MUD_AMOUNT, Math.max(0.0F, mudAmount() - 0.0025F));
        }
        entityData.set(SPLASH_TIMER, splashTimer);
        if (mudAmount() > 0.0F) refreshPlay();
        if (!com.animania.common.config.LegacyConfig.AMBIANCE_MODE.get()
                && playedTicks > -1 && --playedTicks == 0) played = false;

    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        FarmAnimalRole adultRole = random.nextBoolean() ? FarmAnimalRole.FEMALE : FarmAnimalRole.MALE;
        AnimaniaPig adult = ModEntities.pig(adultRole, breed()).create(server);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
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
            mateBreed = null;
            return;
        }
        for (int i = 0; i < com.animania.common.config.LegacyBreedingRules.litterSize(random); i++) {
            PigBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaPig piglet = ModEntities.pig(FarmAnimalRole.YOUNG, childBreed).create(server);
            if (piglet != null) {
                AnimalInformation.recordParent(piglet, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(piglet,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                piglet.moveTo(getX(), getY() + 0.2, getZ(), getYRot(), 0.0F);
                server.addFreshEntity(piglet);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        mateBreed = null;
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaPig pig) || other == this || isBaby() || pig.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(pig)) return false;
        AnimaniaPig female = role() == FarmAnimalRole.FEMALE ? this : pig.role() == FarmAnimalRole.FEMALE ? pig : null;
        boolean opposite = role() != pig.role() && role() != FarmAnimalRole.YOUNG && pig.role() != FarmAnimalRole.YOUNG;
        return opposite && female != null && !female.pregnant && wellCaredFor() && pig.wellCaredFor()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, pig);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaPig pig)) return;
        AnimaniaPig female = role() == FarmAnimalRole.FEMALE ? this : pig;
        AnimaniaPig male = female == this ? pig : this;
        AnimalInformation.recordMating(this, pig);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        pig.setAge(6000);
        resetLove();
        pig.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (role() == FarmAnimalRole.FEMALE) {
            com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaPig.class, 8,
                    () -> ModEntities.pig(FarmAnimalRole.MALE, breed()).create(level.getLevel()),
                    () -> ModEntities.pig(FarmAnimalRole.YOUNG, breed()).create(level.getLevel()),
                    companion -> { });
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
    public Pig getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        PigBreed childBreed = otherParent instanceof AnimaniaPig pig && random.nextBoolean() ? pig.breed() : breed();
        return ModEntities.pig(FarmAnimalRole.YOUNG, childBreed).create(level);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        if (role() == FarmAnimalRole.YOUNG) return BuiltInLootTables.EMPTY;
        return breed().isPrime()
                ? ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("animania", "entities/pig_prime"))
                : EntityType.PIG.getDefaultLootTable();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putInt("Gestation", gestation);
        tag.putBoolean("Muddy", isMuddy());
        tag.putInt("MudTicks", mudTicks);
        tag.putFloat("MudAmount", mudAmount());
        tag.putFloat("SplashTimer", splashTimer);
        tag.putBoolean("Played", played);
        tag.putInt("PlayedTicks", playedTicks);
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        gestation = tag.getInt("Gestation");
        entityData.set(MUDDY, tag.getBoolean("Muddy"));
        mudTicks = tag.getInt("MudTicks");
        entityData.set(MUD_AMOUNT, tag.contains("MudAmount")
                ? net.minecraft.util.Mth.clamp(tag.getFloat("MudAmount"), 0, 1) : mudTicks > 0 ? 1.0F : 0.0F);
        splashTimer = tag.getFloat("SplashTimer");
        entityData.set(SPLASH_TIMER, splashTimer);
        played = !tag.contains("Played") || tag.getBoolean("Played");
        playedTicks = tag.contains("PlayedTicks") ? Math.max(0, tag.getInt("PlayedTicks"))
                : played ? MUD_DURATION : 0;
        if (tag.contains("MateBreed")) mateBreed = PigBreed.fromPath(tag.getString("MateBreed"));
    }

}
