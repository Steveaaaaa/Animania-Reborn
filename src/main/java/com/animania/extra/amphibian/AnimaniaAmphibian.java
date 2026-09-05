package com.animania.extra.amphibian;

import com.animania.common.registry.ModSounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaAmphibian extends Frog {
    private static final EntityDataAccessor<Integer> SKIN =
            SynchedEntityData.defineId(AnimaniaAmphibian.class, EntityDataSerializers.INT);
    private final Kind kind;
    private int poisonHarvestCooldown;
    private boolean pepeGoalsInstalled;

    public AnimaniaAmphibian(EntityType<? extends Animal> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public int skin() {
        return entityData.get(SKIN);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (kind) {
            case FROG -> ModSounds.FROG_AMBIENT.get();
            case DART_FROG -> ModSounds.DART_FROG_AMBIENT.get();
            case TOAD -> ModSounds.TOAD_AMBIENT.get();
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, 0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        entityData.set(SKIN, random.nextInt(kind.skins));
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            if (poisonHarvestCooldown > 0) poisonHarvestCooldown--;
            if (!pepeGoalsInstalled && kind == Kind.FROG && hasCustomName()
                    && getName().getString().equals("Pepe")) installPepeGoals();
        }
    }

    private void installPepeGoals() {
        pepeGoalsInstalled = true;
        goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.5F));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 2.0D, true));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this,
                com.animania.extra.rodent.AnimaniaRodent.class, true,
                target -> target instanceof com.animania.extra.rodent.AnimaniaRodent rodent
                        && (rodent.kind().isFerret() || rodent.kind().isHedgehog())));
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
            setHealth(20.0F);
        }
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if (!level().isClientSide() && kind == Kind.DART_FROG && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1), this);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (kind == Kind.DART_FROG && held.is(Items.ARROW) && poisonHarvestCooldown == 0) {
            if (!level().isClientSide()) {
                poisonHarvestCooldown = 800;
                ItemStack poisonedArrow = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.POISON);
                if (!player.isCreative()) held.shrink(1);
                if (held.isEmpty()) player.setItemInHand(hand, poisonedArrow);
                else if (!player.getInventory().add(poisonedArrow)) player.drop(poisonedArrow, false);
                playSound(SoundEvents.MAGMA_CUBE_SQUISH_SMALL, 0.3F, 1.8F);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        // The original Extra amphibians spawn naturally and have no breeding lifecycle.
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("animania", "entities/" + kind.lootPath));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FrogsType", skin());
        tag.putInt("PoisonTimer", poisonHarvestCooldown);
        tag.putBoolean("PepeGoals", pepeGoalsInstalled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SKIN, Math.floorMod(tag.getInt("FrogsType"), kind.skins));
        poisonHarvestCooldown = Math.max(0, tag.getInt("PoisonTimer"));
        // Goals are reconstructed after load so their selectors are never serialized.
        pepeGoalsInstalled = false;
    }

    public enum Kind {
        FROG("frog", 2), DART_FROG("dart_frog", 3), TOAD("toad", 1);

        private final String lootPath;
        private final int skins;

        Kind(String lootPath, int skins) {
            this.lootPath = lootPath;
            this.skins = skins;
        }
    }
}
