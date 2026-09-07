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
import net.minecraft.world.item.alchemy.PotionUtils;
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
    private int landingDelay, jumpTicks, jumpDuration;
    private boolean wasOnGround;
    public float squishFactor, previousSquishFactor;
    private float squishAmount;
    private net.minecraft.world.entity.LivingEntity legacyTarget;

    public AnimaniaAmphibian(EntityType<? extends Animal> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.moveControl = new LegacyFrogMoveControl();
        this.jumpControl = new LegacyFrogJumpControl();
        registerLegacyGoals();
    }

    @Override protected void registerGoals() { }

    private void registerLegacyGoals() {
        goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.PanicGoal(this, 2.2D) {
            @Override public void tick() { super.tick(); setMovementSpeed(2.2D); }
            @Override public boolean requiresUpdateEveryTick() { return true; }
        });
        goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this,
                Player.class, 6, 1.5D, 1.5D));
        goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 10));
        if (kind == Kind.FROG) goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.6D));
        else {
            goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.6D));
            goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this,
                    com.animania.extra.peafowl.AnimaniaPeafowl.class, 10, 3.0D, 3.5D));
            goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this,
                    com.animania.farm.chicken.AnimaniaChicken.class, 10, 3.0D, 3.5D));
        }
    }

    @Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        return new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, level);
    }
    @Override public net.minecraft.world.entity.LivingEntity getTarget() { return legacyTarget; }
    @Override public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        legacyTarget = target;
        super.setTarget(target);
    }

    @Override protected void customServerAiStep() {
        // Do not tick FrogAi: 1.12 amphibians have no tongue hunting or long-jump brain.
        if (landingDelay > 0) --landingDelay;
        if (onGround()) {
            if (!wasOnGround) {
                setJumping(false);
                landingDelay = moveControl.getSpeedModifier() < 2.2D ? 10 : 1;
                ((LegacyFrogJumpControl) jumpControl).canJump = false;
            }
            LegacyFrogJumpControl jump = (LegacyFrogJumpControl) jumpControl;
            if (!jump.requested()) {
                if (moveControl.hasWanted() && landingDelay == 0) {
                    var path = navigation.getPath();
                    var pos = path != null && !path.isDone() ? path.getNextEntityPos(this)
                            : new net.minecraft.world.phys.Vec3(moveControl.getWantedX(), moveControl.getWantedY(), moveControl.getWantedZ());
                    setYRot((float) (net.minecraft.util.Mth.atan2(pos.z - getZ(), pos.x - getX()) * 180 / Math.PI) - 90);
                    startLegacyJump();
                }
            } else if (!jump.canJump) jump.canJump = true;
        }
        wasOnGround = onGround();
    }

    private void startLegacyJump() { setJumping(true); jumpDuration = 10; jumpTicks = 0; }
    private void setMovementSpeed(double speed) {
        navigation.setSpeedModifier(speed);
        moveControl.setWantedPosition(moveControl.getWantedX(), moveControl.getWantedY(), moveControl.getWantedZ(), speed);
    }
    @Override protected float getJumpPower() {
        if (horizontalCollision || moveControl.hasWanted() && moveControl.getWantedY() > getY() + 0.5D) return 0.5F;
        var path = navigation.getPath();
        if (path != null && !path.isDone() && path.getNextEntityPos(this).x > getY() + 0.5D) return 0.5F;
        return moveControl.getSpeedModifier() <= 0.6D ? 0.2F : 0.3F;
    }
    @Override public void jumpFromGround() {
        super.jumpFromGround();
        if (moveControl.getSpeedModifier() > 0 && getDeltaMovement().horizontalDistanceSqr() < 0.01D)
            moveRelative(0.1F, new net.minecraft.world.phys.Vec3(0, 0, 1));
        if (!level().isClientSide()) level().broadcastEntityEvent(this, (byte) 1);
    }
    @Override public void handleEntityEvent(byte event) {
        if (event == 1) { jumpDuration = 3; jumpTicks = 0; }
        else super.handleEntityEvent(event);
    }

    private final class LegacyFrogJumpControl extends net.minecraft.world.entity.ai.control.JumpControl {
        private boolean canJump;
        private LegacyFrogJumpControl() { super(AnimaniaAmphibian.this); }
        private boolean requested() { return jump; }
        @Override public void tick() { if (jump) { startLegacyJump(); jump = false; } }
    }
    private final class LegacyFrogMoveControl extends net.minecraft.world.entity.ai.control.MoveControl {
        private double nextJumpSpeed;
        private LegacyFrogMoveControl() { super(AnimaniaAmphibian.this); }
        @Override public void tick() {
            if (onGround() && !jumping && !((LegacyFrogJumpControl) jumpControl).requested()) setMovementSpeed(0);
            else if (hasWanted()) setMovementSpeed(nextJumpSpeed);
            super.tick();
        }
        @Override public void setWantedPosition(double x, double y, double z, double speed) {
            if (isInWater()) speed = 1.5D;
            super.setWantedPosition(x, y, z, speed);
            if (speed > 0) nextJumpSpeed = speed + random.nextFloat() / 25;
        }
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData data, net.minecraft.nbt.CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data, spawnTag);
        entityData.set(SKIN, random.nextInt(kind.skins));
        return result;
    }

    @Override
    public void aiStep() {
        squishFactor += (squishAmount - squishFactor) * 0.5F;
        previousSquishFactor = squishFactor;
        if (jumpTicks != jumpDuration) ++jumpTicks;
        else if (jumpDuration != 0) { jumpTicks = 0; jumpDuration = 0; setJumping(false); }
        super.aiStep();
        squishAmount = (onGround() ? -0.5F : 0.5F) * 0.6F;
        if (!level().isClientSide()) {
            if (poisonHarvestCooldown > 0) poisonHarvestCooldown--;
            if (!pepeGoalsInstalled && kind == Kind.FROG && hasCustomName()
                    && getName().getString().equals("Pepe")) installPepeGoals();
        }
    }

    private void installPepeGoals() {
        pepeGoalsInstalled = true;
        goalSelector.removeAllGoals(goal -> true);
        goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 10));
        goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.6D));
        goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.5F));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 2.0D, true));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this,
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
                ItemStack poisonedArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.POISON);
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
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        var type = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                new ResourceLocation("animania", "pepe"));
        var source = new net.minecraft.world.damagesource.DamageSource(
                level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(type));
        boolean hit = target.hurt(source, 2.0F);
        target.hurt(source, 2.0F);
        if (hit && level() instanceof ServerLevel server)
            doEnchantDamageEffects(this, target);
        if (target instanceof Player player) player.knockback(1.0D, getX() - player.getX(), getZ() - player.getZ());
        return hit;
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
    public ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("animania", "entities/" + kind.lootPath);
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
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }
}
