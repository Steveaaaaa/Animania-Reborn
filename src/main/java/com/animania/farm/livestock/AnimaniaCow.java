package com.animania.farm.livestock;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModSounds;
import com.animania.farm.dairy.MilkType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public final class AnimaniaCow extends Cow {
    private static final int GESTATION_TICKS = 20_000;
    private boolean pregnant;
    private boolean milkable;
    private int gestation;
    private CowBreed mateBreed;

    public AnimaniaCow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
        if (role() == FarmAnimalRole.YOUNG) setBaby(true);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public CowBreed breed() {
        return CowBreed.fromPath(entityPath());
    }

    public FarmAnimalRole role() {
        return FarmAnimalRole.cowRole(entityPath());
    }

    private boolean wellCaredFor() {
        return ModAttachments.getData(this, ModAttachments.HUNGER) > 20 && ModAttachments.getData(this, ModAttachments.THIRST) > 20;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "cow");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (role()) {
            case YOUNG -> ModSounds.CALF_AMBIENT.get();
            case MALE -> ModSounds.BULL_AMBIENT.get();
            case FEMALE -> ModSounds.COW_AMBIENT.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return role() == FarmAnimalRole.YOUNG ? ModSounds.CALF_HURT.get() : ModSounds.COW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.COW_DEATH.get();
    }

    @Override
    public void aiStep() {
        super.aiStep();
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
        AnimaniaCow adult = ModEntities.cow(adultRole, breed()).create(server);
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
            CowBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaCow calf = ModEntities.cow(FarmAnimalRole.YOUNG, childBreed).create(server);
            if (calf != null) {
                AnimalInformation.recordParent(calf, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(calf,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                calf.moveTo(getX(), getY() + 0.2, getZ(), getYRot(), 0.0F);
                server.addFreshEntity(calf);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        milkable = true;
        mateBreed = null;
    }

    private void copyInto(AnimaniaCow adult) {
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
        if (!(other instanceof AnimaniaCow cow) || other == this || isBaby() || cow.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(cow)) return false;
        AnimaniaCow female = role() == FarmAnimalRole.FEMALE ? this : cow.role() == FarmAnimalRole.FEMALE ? cow : null;
        boolean opposite = role() != cow.role() && role() != FarmAnimalRole.YOUNG && cow.role() != FarmAnimalRole.YOUNG;
        return opposite && female != null && !female.pregnant && wellCaredFor() && cow.wellCaredFor()
                && isInLove() && cow.isInLove()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, cow);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, net.minecraft.nbt.CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, spawnTag);
        if (role() == FarmAnimalRole.FEMALE && !isBaby()
                && com.animania.common.config.LegacyConfig.COWS_MILKABLE_AT_SPAWN.get()) milkable = true;
        if (role() == FarmAnimalRole.FEMALE) {
            var family = com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaCow.class, 8,
                    () -> ModEntities.cow(FarmAnimalRole.MALE, breed()).create(level.getLevel()),
                    () -> ModEntities.cow(FarmAnimalRole.YOUNG, breed()).create(level.getLevel()),
                    companion -> { });
            if (family == com.animania.common.entity.LegacyNaturalFamily.Result.YOUNG) milkable = true;
        }
        return result;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaCow cow)) return;
        AnimaniaCow female = role() == FarmAnimalRole.FEMALE ? this : cow;
        AnimaniaCow male = female == this ? cow : this;
        AnimalInformation.recordMating(this, cow);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        cow.setAge(6000);
        resetLove();
        cow.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.BUCKET)) {
            if (role() == FarmAnimalRole.FEMALE && milkable && wellCaredFor() && !isBaby()) {
                if (!level().isClientSide()) {
                    ItemStack milk = switch (breed()) {
                        case HOLSTEIN -> new ItemStack(ModItems.milkBucket(MilkType.HOLSTEIN).get());
                        case FRIESIAN -> new ItemStack(ModItems.milkBucket(MilkType.FRIESIAN).get());
                        case JERSEY -> new ItemStack(ModItems.milkBucket(MilkType.JERSEY).get());
                        default -> Items.MILK_BUCKET.getDefaultInstance();
                    };
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, milk));
                    com.animania.common.entity.LegacyAnimalNeeds.setWatered(this, false);
                    milkable = false;
                }
                player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
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
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CowBreed childBreed = otherParent instanceof AnimaniaCow cow && random.nextBoolean() ? cow.breed() : breed();
        return ModEntities.cow(FarmAnimalRole.YOUNG, childBreed).create(level);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        if (role() == FarmAnimalRole.YOUNG) return BuiltInLootTables.EMPTY;
        return breed().isPrime()
                ? new ResourceLocation("animania", "entities/cow_prime")
                : EntityType.COW.getDefaultLootTable();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putBoolean("HasKids", milkable);
        tag.putInt("Gestation", gestation);
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        milkable = tag.getBoolean("HasKids");
        gestation = tag.getInt("Gestation");
        if (tag.contains("MateBreed")) mateBreed = CowBreed.fromPath(tag.getString("MateBreed"));
    }
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }
}
