package com.animania.extra.rabbit;

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
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public final class AnimaniaRabbit extends Rabbit {
    private static final int GESTATION_TICKS = 12_000;
    private static final String[] LOP_COLORS =
            {"black", "brown", "golden", "olive", "patch_black", "patch_brown", "patch_grey"};
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(AnimaniaRabbit.class, EntityDataSerializers.INT);
    private boolean pregnant;
    private boolean killerGoalsInstalled;
    private int gestation;
    private RabbitBreed mateBreed;

    @SuppressWarnings("unchecked")
    public static boolean checkSpawnRules(EntityType<AnimaniaRabbit> type, LevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        return Rabbit.checkRabbitSpawnRules((EntityType<Rabbit>) (EntityType<?>) type, level, reason, pos, random);
    }

    public AnimaniaRabbit(EntityType<? extends Rabbit> type, Level level) {
        super(type, level);
        if (role() == RabbitRole.KIT) setBaby(true);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public RabbitBreed breed() {
        return RabbitBreed.fromPath(entityPath());
    }

    public RabbitRole role() {
        return RabbitRole.fromPath(entityPath());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RABBIT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RABBIT_HURT.get();
    }

    public String textureName() {
        if (hasCustomName() && getName().getString().equalsIgnoreCase("Killer")) return "killer";
        return breed() == RabbitBreed.LOP ? "lop_" + LOP_COLORS[entityData.get(COLOR)]
                : breed().getSerializedName();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, 0);
    }

    private boolean wellCaredFor() {
        return getData(ModAttachments.HUNGER) > 20 && getData(ModAttachments.THIRST) > 20;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && !killerGoalsInstalled && hasCustomName()
                && getName().getString().equals("Killer")) installKillerGoals();
        if (!level().isClientSide() && role() == RabbitRole.KIT && !isBaby()) growIntoAdult();
        else if (!level().isClientSide() && role() == RabbitRole.DOE && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    private void installKillerGoals() {
        killerGoalsInstalled = true;
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.LeapAtTargetGoal(this, 0.7F));
        goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 2.0D, true));
        goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.8D));
        goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 10));
        targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        targetSelector.addGoal(2, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this, Player.class, true));
        getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(50.0D);
        setHealth(50.0F);
        setData(ModAttachments.SLEEPING, false);
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        RabbitRole adultRole = random.nextBoolean() ? RabbitRole.DOE : RabbitRole.BUCK;
        AnimaniaRabbit adult = ModEntities.rabbit(adultRole, breed()).create(server);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
            adult.entityData.set(COLOR, entityData.get(COLOR));
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
        int litterSize = com.animania.common.config.LegacyBreedingRules.litterSize(random);
        for (int i = 0; i < litterSize; i++) {
            RabbitBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaRabbit kit = ModEntities.rabbit(RabbitRole.KIT, childBreed).create(server);
            if (kit != null) {
                AnimalInformation.recordParent(kit, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(kit,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                kit.moveTo(getX() + (random.nextDouble() - 0.5D) * 0.6D, getY() + 0.2D,
                        getZ() + (random.nextDouble() - 0.5D) * 0.6D, getYRot(), 0.0F);
                if (childBreed == RabbitBreed.LOP) kit.entityData.set(COLOR, entityData.get(COLOR));
                server.addFreshEntity(kit);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        mateBreed = null;
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaRabbit rabbit) || other == this || isBaby() || rabbit.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(rabbit)) return false;
        AnimaniaRabbit female = role() == RabbitRole.DOE ? this : rabbit.role() == RabbitRole.DOE ? rabbit : null;
        boolean opposite = role() != rabbit.role() && role() != RabbitRole.KIT && rabbit.role() != RabbitRole.KIT;
        return opposite && female != null && !female.pregnant && wellCaredFor() && rabbit.wellCaredFor()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, rabbit);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaRabbit rabbit)) return;
        AnimaniaRabbit female = role() == RabbitRole.DOE ? this : rabbit;
        AnimaniaRabbit male = female == this ? rabbit : this;
        AnimalInformation.recordMating(this, rabbit);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        rabbit.setAge(6000);
        resetLove();
        rabbit.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        if (breed() == RabbitBreed.LOP) entityData.set(COLOR, random.nextInt(LOP_COLORS.length));
        if (role() == RabbitRole.DOE) {
            com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaRabbit.class, 8,
                    () -> ModEntities.rabbit(RabbitRole.BUCK, breed()).create(level.getLevel()),
                    () -> ModEntities.rabbit(RabbitRole.KIT, breed()).create(level.getLevel()),
                    companion -> ((AnimaniaRabbit) companion).entityData.set(COLOR, entityData.get(COLOR)));
        }
        return result;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        var type = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath("animania", "killer_rabbit"));
        var source = new net.minecraft.world.damagesource.DamageSource(
                level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(type));
        boolean hit = target.hurt(source, 5.0F);
        target.hurt(source, 5.0F);
        if (hit && level() instanceof ServerLevel server)
            net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(server, target, source);
        if (target instanceof Player player) player.knockback(1.0D, getX() - player.getX(), getZ() - player.getZ());
        return hit;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "rabbit");
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
    public Rabbit getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        RabbitBreed childBreed = otherParent instanceof AnimaniaRabbit rabbit && random.nextBoolean()
                ? rabbit.breed() : breed();
        return ModEntities.rabbit(RabbitRole.KIT, childBreed).create(level);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        if (role() == RabbitRole.KIT) return BuiltInLootTables.EMPTY;
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("animania",
                breed().isPrime() ? "entities/rabbit_prime" : "entities/rabbit_regular"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putInt("Gestation", gestation);
        tag.putInt("ColorNumber", entityData.get(COLOR));
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        gestation = tag.getInt("Gestation");
        entityData.set(COLOR, Math.floorMod(tag.getInt("ColorNumber"), LOP_COLORS.length));
        if (tag.contains("MateBreed")) mateBreed = RabbitBreed.fromPath(tag.getString("MateBreed"));
    }
}
