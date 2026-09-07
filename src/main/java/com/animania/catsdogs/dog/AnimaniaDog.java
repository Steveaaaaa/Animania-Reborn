package com.animania.catsdogs.dog;
import net.minecraft.resources.ResourceLocation;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModItems;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;
import java.util.List;

public final class AnimaniaDog extends TamableAnimal {
    private static final int GESTATION_TICKS = 12_000;
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(AnimaniaDog.class, EntityDataSerializers.INT);
    private boolean pregnant;
    private int gestation;
    @Nullable private DogBreed mateBreed;

    public AnimaniaDog(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        if (role() == DogRole.PUPPY) setBaby(true);
        setPersistenceRequired();
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public DogBreed breed() {
        return DogBreed.fromPath(entityPath());
    }

    public DogRole role() {
        return DogRole.fromPath(entityPath());
    }

    public int variant() {
        return entityData.get(VARIANT);
    }

    public String textureName() {
        if (breed() == DogBreed.FOX && hasCustomName() && getName().getString().equalsIgnoreCase("Razz")) {
            return "razz_fox";
        }
        if (breed() == DogBreed.LABRADOR && role() == DogRole.FEMALE && hasCustomName()
                && getName().getString().equalsIgnoreCase("Gloria")) return "gloria";
        return breed().variants() > 1 ? breed().getSerializedName() + variant() : breed().getSerializedName();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(6, new TemptGoal(this, 1.1D,
                Ingredient.of(Items.BEEF, ModItems.RAW_PRIME_BEEF.get(), ModItems.RAW_PRIME_STEAK.get()), false));
        goalSelector.addGoal(7, new PanicGoal(this, 1.4D));
        goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(4, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, AbstractSkeleton.class, true));
        targetSelector.addGoal(5, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, Sheep.class, true,
                entity -> !isTame()));
        targetSelector.addGoal(6, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, Rabbit.class, true,
                entity -> !isTame()));
        if (breed() == DogBreed.FOX || breed() == DogBreed.WOLF) {
            targetSelector.addGoal(5, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this,
                    com.animania.extra.peafowl.AnimaniaPeafowl.class, true,
                    entity -> !isTame() && entity instanceof com.animania.extra.peafowl.AnimaniaPeafowl bird
                            && bird.role() == com.animania.extra.peafowl.PeafowlRole.PEACHICK));
            targetSelector.addGoal(6, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, AnimaniaRodent.class, true,
                    entity -> !isTame()));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 2.5D);
    }

    private boolean wellCaredFor() {
        return ModAttachments.getData(this, ModAttachments.HUNGER) > 20 && ModAttachments.getData(this, ModAttachments.THIRST) > 20;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;
        if (role() == DogRole.PUPPY && !isBaby()) growIntoAdult();
        else if (role() == DogRole.FEMALE && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        AnimaniaDog adult = ModEntities.dog(random.nextBoolean() ? DogRole.FEMALE : DogRole.MALE, breed()).create(server);
        if (adult != null) {
            copyIdentity(adult);
            server.addFreshEntity(adult);
            discard();
        }
    }

    private void copyIdentity(AnimaniaDog dog) {
        dog.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
        dog.entityData.set(VARIANT, variant());
        dog.setCustomName(getCustomName());
        dog.setCustomNameVisible(isCustomNameVisible());
        com.animania.common.entity.LegacyAnimalNeeds.copyState(this, dog);
        if (isTame() && getOwnerUUID() != null) {
            dog.setOwnerUUID(getOwnerUUID());
            dog.setTame(true);
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
        int litter = com.animania.common.config.LegacyBreedingRules.litterSize(random);
        for (int i = 0; i < litter; i++) {
            DogBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaDog puppy = ModEntities.dog(DogRole.PUPPY, childBreed).create(server);
            if (puppy != null) {
                AnimalInformation.recordParent(puppy, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(puppy,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                puppy.moveTo(getX() + (random.nextDouble() - 0.5) * 0.7, getY() + 0.1,
                        getZ() + (random.nextDouble() - 0.5) * 0.7, getYRot(), 0);
                if (childBreed == breed()) puppy.entityData.set(VARIANT, variant());
                if (isTame() && getOwnerUUID() != null) {
                    puppy.setOwnerUUID(getOwnerUUID());
                    puppy.setTame(true);
                }
                server.addFreshEntity(puppy);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        mateBreed = null;
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaDog dog) || other == this || isBaby() || dog.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(dog)) return false;
        AnimaniaDog female = role() == DogRole.FEMALE ? this : dog.role() == DogRole.FEMALE ? dog : null;
        boolean opposite = role() != dog.role() && role() != DogRole.PUPPY && dog.role() != DogRole.PUPPY;
        return opposite && female != null && !female.pregnant && wellCaredFor() && dog.wellCaredFor()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, dog);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaDog dog)) return;
        AnimaniaDog female = role() == DogRole.FEMALE ? this : dog;
        AnimaniaDog male = female == this ? dog : this;
        AnimalInformation.recordMating(this, dog);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        dog.setAge(6000);
        resetLove();
        dog.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "dog");
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        return com.animania.common.config.LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get() && super.canAttack(target);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (isFood(held) && !isTame()) {
            if (!level().isClientSide()) {
                tame(player);
                setOrderedToSit(false);
                ModAttachments.setData(this, ModAttachments.HUNGER, ModAttachments.MAX_NEED);
                level().broadcastEntityEvent(this, (byte) 7);
                if (!player.isCreative()) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (held.isEmpty() && player.isShiftKeyDown() && role() == DogRole.FEMALE) {
            if (!level().isClientSide()) player.displayClientMessage(Component.translatable(pregnant
                    ? "message.animania.pregnant" : "message.animania.not_pregnant", gestation), true);
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (isTame() && isOwnedBy(player) && held.isEmpty()) {
            if (!level().isClientSide()) {
                setOrderedToSit(!isOrderedToSit());
                getNavigation().stop();
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data, net.minecraft.nbt.CompoundTag spawnTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, spawnTag);
        entityData.set(VARIANT, random.nextInt(breed().variants()));
        if (role() == DogRole.FEMALE && breed().naturallySpawns()) {
            com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, reason,
                    AnimaniaDog.class, 8,
                    () -> ModEntities.dog(DogRole.MALE, breed()).create(level.getLevel()),
                    () -> ModEntities.dog(DogRole.PUPPY, breed()).create(level.getLevel()),
                    companion -> ((AnimaniaDog) companion).entityData.set(VARIANT, variant()));
        }
        return result;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
        DogBreed childBreed = parent instanceof AnimaniaDog dog && random.nextBoolean() ? dog.breed() : breed();
        return ModEntities.dog(DogRole.PUPPY, childBreed).create(level);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", variant());
        tag.putBoolean("Pregnant", pregnant);
        tag.putInt("Gestation", gestation);
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VARIANT, Math.floorMod(tag.getInt("Variant"), breed().variants()));
        pregnant = tag.getBoolean("Pregnant");
        gestation = tag.getInt("Gestation");
        if (tag.contains("MateBreed")) mateBreed = DogBreed.fromPath(tag.getString("MateBreed"));
    }
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }
}
