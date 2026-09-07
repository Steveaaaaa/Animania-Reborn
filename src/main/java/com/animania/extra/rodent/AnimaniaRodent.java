package com.animania.extra.rodent;

import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;

import javax.annotation.Nullable;

public final class AnimaniaRodent extends TamableAnimal {
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(AnimaniaRodent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BALL_COLOR =
            SynchedEntityData.defineId(AnimaniaRodent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD_STACK =
            SynchedEntityData.defineId(AnimaniaRodent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STANDING =
            SynchedEntityData.defineId(AnimaniaRodent.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> INTERESTED =
            SynchedEntityData.defineId(AnimaniaRodent.class, EntityDataSerializers.BOOLEAN);
    private int standCount = 30, eatCount = 5000;
    private float interest, previousInterest;
    public static final int NO_BALL = -2;
    private final Kind kind;

    public AnimaniaRodent(EntityType<? extends TamableAnimal> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        registerKindGoals();
        if (kind == Kind.HAMSTER) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(10);
            setHealth(10);
        }
        setPersistenceRequired();
    }

    public Kind kind() {
        return kind;
    }

    public int color() {
        return entityData.get(COLOR);
    }

    public String textureName() {
        if (kind == Kind.HAMSTER) return Kind.HAMSTER_COLORS[color()];
        if (kind.isHedgehog() && hasCustomName()) {
            if (getName().getString().equalsIgnoreCase("Sonic")) return "hedgehog_sonic";
            if (getName().getString().equalsIgnoreCase("Sanic")) return "hedgehog_sanic";
        }
        return kind.texture;
    }

    public int ballColor() {
        return entityData.get(BALL_COLOR);
    }

    public boolean isInBall() {
        return kind == Kind.HAMSTER && ballColor() != NO_BALL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (kind == Kind.HAMSTER) return ModSounds.HAMSTER_AMBIENT.get();
        if (kind.isFerret()) return ModSounds.FERRET_AMBIENT.get();
        return ModSounds.HEDGEHOG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (kind == Kind.HAMSTER) return ModSounds.HAMSTER_HURT.get();
        if (kind.isFerret()) return ModSounds.FERRET_HURT.get();
        return ModSounds.HEDGEHOG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return kind == Kind.HAMSTER ? ModSounds.HAMSTER_DEATH.get() : super.getDeathSound();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0);
        entityData.define(BALL_COLOR, NO_BALL);
        entityData.define(FOOD_STACK, 0);
        entityData.define(STANDING, false);
        entityData.define(INTERESTED, false);
    }

    public int getFoodStackCount() { return entityData.get(FOOD_STACK); }
    public boolean isHamsterStanding() { return entityData.get(STANDING); }
    public float getInterestedAngle(float partial) {
        return net.minecraft.util.Mth.lerp(partial, previousInterest, interest) * 0.15F * 3.141593F;
    }

    /** The original hand-feeding action fills up to five cheek sections. */
    public void storeHamsterFood() {
        if (kind != Kind.HAMSTER || ModAttachments.getData(this, ModAttachments.SLEEPING)) return;
        entityData.set(STANDING, true);
        standCount = 100;
        if (getFoodStackCount() < 5) entityData.set(FOOD_STACK, getFoodStackCount() + 1);
        else heal(1);
    }

    private void eatStoredFood() {
        if (getFoodStackCount() > 0) {
            entityData.set(FOOD_STACK, getFoodStackCount() - 1);
            heal(1);
        }
    }

    @Override public void aiStep() {
        super.aiStep();
        if (kind != Kind.HAMSTER || level().isClientSide()) return;
        if (getHealth() < 10) { eatStoredFood(); eatCount = 5000; }
        if (!isHamsterStanding() && !isInSittingPose() && !ModAttachments.getData(this, ModAttachments.SLEEPING)) {
            if (random.nextInt(20) == 0 && random.nextInt(20) == 0) {
                entityData.set(STANDING, true);
                standCount = 30;
                navigation.stop();
                setJumping(false);
            }
        } else if (isHamsterStanding() && standCount-- <= 0 && random.nextInt(10) == 0) {
            entityData.set(STANDING, false);
        }
        if (getFoodStackCount() > 0) {
            if (eatCount == 0) {
                if (random.nextInt(30) == 0 && random.nextInt(30) == 0) {
                    eatStoredFood(); eatCount = 5000;
                }
            } else eatCount--;
        }
        entityData.set(INTERESTED, navigation.isDone() && getTarget() instanceof Player player
                && player.getMainHandItem().is(Items.WHEAT_SEEDS));
        if (isInSittingPose() || isHamsterStanding()) navigation.stop();
    }

    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        previousInterest = interest;
        interest += ((entityData.get(INTERESTED) ? 1 : 0) - interest) * 0.4F;
        com.animania.common.entity.AnimalTickBridge.after(this);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new PanicGoal(this, 1.4D));
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    /**
     * Mob invokes registerGoals() from its constructor, before this subclass can
     * assign kind.  Register only kind-independent goals there and install the
     * remaining goals after the constructor argument has been stored.
     */
    private void registerKindGoals() {
        goalSelector.addGoal(4, new TemptGoal(this, 1.2D, Ingredient.of(kind.food), false));
        if (kind.isFerret()) goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.1D, true));
        if (kind.isFerret()) targetSelector.addGoal(5,
                new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, Silverfish.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data, net.minecraft.nbt.CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, spawnTag);
        if (kind == Kind.HAMSTER) entityData.set(COLOR, random.nextInt(Kind.HAMSTER_COLORS.length));
        return result;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        String key = kind == Kind.HAMSTER ? "hamster"
                : kind == Kind.FERRET_GREY || kind == Kind.FERRET_WHITE ? "ferret" : "hedgehog";
        return com.animania.common.config.LegacyItemMatcher.matches(stack, key);
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        return com.animania.common.config.LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get() && super.canAttack(target);
    }

    /** Keeps carried rodents at the same shoulder/chest anchor used by the 1.12 renderer. */
    @Override
    public void rideTick() {
        super.rideTick();
        if (getVehicle() instanceof Player player) {
            double yaw = Math.toRadians(player.yBodyRot);
            double side = -0.32D;
            setPos(player.getX() + Math.cos(yaw) * side,
                    player.getY() + (player.isCrouching() ? 1.07D : 1.37D),
                    player.getZ() + Math.sin(yaw) * side);
            setYRot(player.getYRot());
            setYHeadRot(player.getYRot());
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (kind == Kind.HAMSTER && held.getItem() instanceof HamsterBallItem ball && !isInBall()) {
            if (!level().isClientSide()) {
                entityData.set(BALL_COLOR, ball.ballColor());
                setOrderedToSit(false);
                if (!player.isCreative()) held.shrink(1);
                playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.5F, 1.4F);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (kind == Kind.HAMSTER && isInBall() && held.isEmpty() && player.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                ItemStack returned = new ItemStack(ModItems.hamsterBall(ballColor()).get());
                entityData.set(BALL_COLOR, NO_BALL);
                if (!player.addItem(returned)) player.drop(returned, false);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (isFood(held)) {
            if (!level().isClientSide()) {
                if (!isTame()) {
                    tame(player);
                    setOrderedToSit(false);
                    level().broadcastEntityEvent(this, (byte) 7);
                }
                storeHamsterFood();
                ModAttachments.setData(this, ModAttachments.HUNGER, ModAttachments.MAX_NEED);
                heal(2.0F);
                if (!player.isCreative()) held.shrink(1);
                playSound(kind == Kind.HAMSTER ? ModSounds.HAMSTER_EAT.get() : SoundEvents.GENERIC_EAT,
                        0.5F, 1.4F);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (isTame() && isOwnedBy(player) && held.isEmpty()) {
            if (!level().isClientSide()) {
                if (player.isShiftKeyDown()) {
                    setOrderedToSit(false);
                    startRiding(player, true);
                } else {
                    setOrderedToSit(!isOrderedToSit());
                    getNavigation().stop();
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("foodStackCount", getFoodStackCount());
        tag.putInt("ColorNumber", color());
        tag.putInt("BallColor", ballColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(FOOD_STACK, Math.max(0, Math.min(5, tag.getInt("foodStackCount"))));
        entityData.set(COLOR, kind == Kind.HAMSTER
                ? Math.floorMod(tag.getInt("ColorNumber"), Kind.HAMSTER_COLORS.length) : 0);
        entityData.set(BALL_COLOR, kind == Kind.HAMSTER && tag.contains("BallColor")
                ? Math.max(NO_BALL, Math.min(DyeColor.values().length - 1, tag.getInt("BallColor"))) : NO_BALL);
    }

    public enum Kind {
        HAMSTER("hamster", Items.WHEAT_SEEDS),
        HEDGEHOG("hedgehog", Items.CARROT),
        HEDGEHOG_ALBINO("hedgehog_white", Items.CARROT),
        FERRET_GREY("ferret_grey", Items.CHICKEN),
        FERRET_WHITE("ferret_white", Items.CHICKEN);

        private static final String[] HAMSTER_COLORS =
                {"black", "brown", "darkbrown", "darkgray", "gray", "plum", "tarou", "white", "gold"};
        private final String texture;
        private final net.minecraft.world.item.Item food;

        Kind(String texture, net.minecraft.world.item.Item food) {
            this.texture = texture;
            this.food = food;
        }

        public boolean isHedgehog() {
            return this == HEDGEHOG || this == HEDGEHOG_ALBINO;
        }

        public boolean isFerret() {
            return this == FERRET_GREY || this == FERRET_WHITE;
        }
    }
}
