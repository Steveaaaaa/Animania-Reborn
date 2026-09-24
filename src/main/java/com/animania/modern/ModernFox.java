package com.animania.modern;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.registry.ModEntities;
import net.minecraft.world.entity.animal.Fox;

public final class ModernFox extends Fox implements ModernAnimal {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> FEMALE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ModernFox.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> COAT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(ModernFox.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    public int coat() { return entityData.get(COAT); }
    private final String fixedBreed;
    public String coatName() { if (fixedBreed != null) return fixedBreed; return getVariant() == Fox.Type.SNOW ? "snow" : switch (coat()) { case 1 -> "silver"; case 2 -> "cross"; default -> "red"; }; }
    private final ModernCare care = new ModernCare(this);

    public ModernFox(net.minecraft.world.entity.EntityType<? extends Fox> type, net.minecraft.world.level.Level level) {
        this(type, level, null);
    }

    public ModernFox(net.minecraft.world.entity.EntityType<? extends Fox> type, net.minecraft.world.level.Level level, String breed) {
        super(type, level);
        fixedBreed = breed;
        setVariant("snow".equals(breed) ? Fox.Type.SNOW : Fox.Type.RED);
        if (!level.isClientSide()) {
            entityData.set(FEMALE, random.nextBoolean());
            int roll = random.nextInt(10);
            entityData.set(COAT, roll < 6 ? 0 : roll < 9 ? 2 : 1);
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FEMALE, false);
        builder.define(COAT, 0);
    }
    @Override public void setVariant(Fox.Type variant) {
        super.setVariant(fixedBreed == null ? variant : fixedBreed.equals("snow") ? Fox.Type.SNOW : Fox.Type.RED);
    }

    @Override public boolean isFemale() { return entityData.get(FEMALE); }
    @Override public ModernCare care() { return care; }
    @Override public boolean canMate(net.minecraft.world.entity.animal.Animal other) { return care.canMate(other); }
    @Override public void spawnChildFromBreeding(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.animal.Animal mate) { care.conceive(level, mate); }

    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("AnimaniaFemale", isFemale());
        tag.putInt("AnimaniaCoat", coat());
        care.save(tag);
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AnimaniaFemale")) entityData.set(FEMALE, tag.getBoolean("AnimaniaFemale"));
        if (tag.contains("AnimaniaCoat")) entityData.set(COAT, net.minecraft.util.Mth.clamp(tag.getInt("AnimaniaCoat"), 0, 2));
        care.load(tag);
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(4, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this,
                com.animania.farm.chicken.AnimaniaChicken.class, true, bird -> true));
        targetSelector.addGoal(4, new com.animania.common.entity.ai.LegacyNearestAttackableTargetGoal<>(this,
                com.animania.extra.rabbit.AnimaniaRabbit.class, true, rabbit -> true));
    }

    @Override public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        return com.animania.common.config.LegacyConfig.ANIMALS_CAN_ATTACK_OTHERS.get() && super.canAttack(target);
    }

    @Override public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return super.isFood(stack) || com.animania.common.config.LegacyItemMatcher.matches(stack, "dog");
    }

    @Override public ModernFox getBreedOffspring(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.AgeableMob parent) {
        if (!(parent instanceof ModernFox fox)) return null;
        ModernFox child = ModEntities.modernFox(random.nextBoolean() ? coatName() : fox.coatName()).create(level);
        if (child != null) {
            // The registered breed fixes appearance and native red/snow behavior.
            // Vanilla trusted-player data is private; transfer the saved trust list to the cub.
            var inherited = new net.minecraft.nbt.CompoundTag();
            addAdditionalSaveData(inherited);
            var data = new net.minecraft.nbt.CompoundTag();
            var trusted = inherited.getList("Trusted", 11).copy();
            for (var feeder : new java.util.UUID[]{care.feeder(), fox.care.feeder()}) {
                if (feeder != null && trusted.size() < 2) {
                    var entry = net.minecraft.nbt.NbtUtils.createUUID(feeder);
                    if (!trusted.contains(entry)) trusted.add(entry);
                }
            }
            data.put("Trusted", trusted);
            data.putString("Type", child.getVariant().getSerializedName());
            child.readAdditionalSaveData(data);
        }
        return child;
    }

    @Override public void aiStep() {
        var before = getMainHandItem().copy();
        super.aiStep();
        if (!level().isClientSide()) {
            if (!before.isEmpty() && isFood(before) && getMainHandItem().getCount() < before.getCount())
                LegacyAnimalNeeds.feed(this, false, false);
            care.tick();
        }
    }
}
