package com.animania.catsdogs.cat;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.extra.rodent.AnimaniaRodent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaCat extends TamableAnimal {
    private static final int GESTATION_TICKS = 12_000;
    private boolean pregnant;
    private int gestation;
    private int blinkTimer;
    @Nullable private CatBreed mateBreed;

    public AnimaniaCat(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        if (role() == CatRole.KITTEN) setBaby(true);
        blinkTimer = 80 + random.nextInt(80);
        setPersistenceRequired();
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public CatBreed breed() {
        return CatBreed.fromPath(entityPath());
    }

    public CatRole role() {
        return CatRole.fromPath(entityPath());
    }

    private boolean wellCaredFor() {
        return ModAttachments.getData(this, ModAttachments.HUNGER) > 20 && ModAttachments.getData(this, ModAttachments.THIRST) > 20;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(3, new com.animania.common.entity.ai.LegacyCatAttackGoal(this));
        goalSelector.addGoal(6, new TemptGoal(this, 0.8D,
                Ingredient.of(Items.COD, Items.SALMON), true));
        goalSelector.addGoal(7, new PanicGoal(this, 1.4D));
        goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(4, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, Silverfish.class, true,
                entity -> !isTame()));
        targetSelector.addGoal(5, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, Animal.class, true,
                entity -> !isTame() && isLegacyPrey(entity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 2.5D);
    }

    private static boolean isLegacyPrey(net.minecraft.world.entity.LivingEntity entity) {
        if (entity instanceof com.animania.farm.chicken.AnimaniaChicken chicken) {
            return chicken.role() == com.animania.farm.chicken.ChickenRole.CHICK;
        }
        if (entity instanceof com.animania.extra.peafowl.AnimaniaPeafowl peafowl) {
            return peafowl.role() == com.animania.extra.peafowl.PeafowlRole.PEACHICK;
        }
        if (entity instanceof com.animania.extra.amphibian.AnimaniaAmphibian amphibian) {
            return amphibian.kind() != com.animania.extra.amphibian.AnimaniaAmphibian.Kind.DART_FROG;
        }
        return entity instanceof AnimaniaRodent;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (blinkTimer > 0 && --blinkTimer == 0) blinkTimer = 100 + random.nextInt(100);
        if (level().isClientSide()) return;
        if (role() == CatRole.KITTEN && !isBaby()) growIntoAdult();
        else if (role() == CatRole.QUEEN && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    public boolean isBlinking() {
        return blinkTimer > 0 && blinkTimer < 7;
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        CatRole adultRole = random.nextBoolean() ? CatRole.QUEEN : CatRole.TOM;
        AnimaniaCat adult = ModEntities.cat(adultRole, breed()).create(server);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
            com.animania.common.entity.LegacyAnimalNeeds.copyState(this, adult);
            if (isTame() && getOwnerUUID() != null) {
                adult.setOwnerUUID(getOwnerUUID());
                adult.setTame(true);
            }
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
        int litter = com.animania.common.config.LegacyBreedingRules.litterSize(random);
        for (int i = 0; i < litter; i++) {
            CatBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaCat kitten = ModEntities.cat(CatRole.KITTEN, childBreed).create(server);
            if (kitten != null) {
                AnimalInformation.recordParent(kitten, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(kitten,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                kitten.moveTo(getX() + (random.nextDouble() - 0.5D) * 0.7D, getY() + 0.1D,
                        getZ() + (random.nextDouble() - 0.5D) * 0.7D, getYRot(), 0);
                if (isTame() && getOwnerUUID() != null) {
                    kitten.setOwnerUUID(getOwnerUUID());
                    kitten.setTame(true);
                }
                server.addFreshEntity(kitten);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        mateBreed = null;
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaCat cat) || other == this || isBaby() || cat.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(cat)) return false;
        AnimaniaCat female = role() == CatRole.QUEEN ? this : cat.role() == CatRole.QUEEN ? cat : null;
        boolean opposite = role() != cat.role() && role() != CatRole.KITTEN && cat.role() != CatRole.KITTEN;
        return opposite && female != null && !female.pregnant && wellCaredFor() && cat.wellCaredFor()
                && isInLove() && cat.isInLove()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, cat);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaCat cat)) return;
        AnimaniaCat female = role() == CatRole.QUEEN ? this : cat;
        AnimaniaCat male = female == this ? cat : this;
        AnimalInformation.recordMating(this, cat);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        cat.setAge(6000);
        resetLove();
        cat.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "cat");
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
        if (held.isEmpty() && player.isShiftKeyDown() && role() == CatRole.QUEEN) {
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
        if (role() == CatRole.QUEEN && breed() == CatBreed.OCELOT) {
            com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, reason,
                    AnimaniaCat.class, 8,
                    () -> ModEntities.cat(CatRole.TOM, breed()).create(level.getLevel()),
                    () -> ModEntities.cat(CatRole.KITTEN, breed()).create(level.getLevel()),
                    companion -> { });
        }
        return result;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
        CatBreed childBreed = parent instanceof AnimaniaCat cat && random.nextBoolean() ? cat.breed() : breed();
        return ModEntities.cat(CatRole.KITTEN, childBreed).create(level);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putInt("Gestation", gestation);
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        gestation = tag.getInt("Gestation");
        if (tag.contains("MateBreed")) mateBreed = CatBreed.fromPath(tag.getString("MateBreed"));
    }
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }
}
