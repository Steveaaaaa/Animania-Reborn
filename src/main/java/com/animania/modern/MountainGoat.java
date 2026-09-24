package com.animania.modern;

import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.entity.LegacyAnimalNeeds;
import net.minecraft.world.entity.animal.goat.Goat;

public final class MountainGoat extends Goat implements ModernAnimal {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> FEMALE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(MountainGoat.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> COAT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(MountainGoat.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    public int coat() { return entityData.get(COAT); }
    private final String fixedBreed;
    public String coatName() { if (fixedBreed != null) return fixedBreed; return switch (coat()) { case 1 -> "cream"; case 2 -> "slate"; default -> "white"; }; }
    private final ModernCare care = new ModernCare(this);

    public MountainGoat(net.minecraft.world.entity.EntityType<? extends Goat> type, net.minecraft.world.level.Level level) {
        this(type, level, null);
    }

    public MountainGoat(net.minecraft.world.entity.EntityType<? extends Goat> type, net.minecraft.world.level.Level level, String breed) {
        super(type, level);
        fixedBreed = breed;
        if (!level.isClientSide()) {
            entityData.set(FEMALE, random.nextBoolean());
            int roll = random.nextInt(10);
            entityData.set(COAT, roll < 6 ? 0 : roll < 9 ? 1 : 2);
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FEMALE, false);
        builder.define(COAT, 0);
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

    private boolean careControlsMovement;

    @Override public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return super.isFood(stack) || com.animania.common.config.LegacyItemMatcher.matches(stack, "goat");
    }

    @Override public MountainGoat getBreedOffspring(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.AgeableMob parent) {
        if (!(parent instanceof MountainGoat goat)) return null;
        MountainGoat child = ModEntities.mountainGoat(random.nextBoolean() ? coatName() : goat.coatName()).create(level);
        if (child != null) {
            Goat inherited = super.getBreedOffspring(level, goat);
            if (inherited != null) {
                var jump = net.minecraft.world.entity.ai.memory.MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS;
                var ram = net.minecraft.world.entity.ai.memory.MemoryModuleType.RAM_COOLDOWN_TICKS;
                child.getBrain().setMemory(jump, inherited.getBrain().getMemory(jump));
                child.getBrain().setMemory(ram, inherited.getBrain().getMemory(ram));
                child.setScreamingGoat(inherited.isScreamingGoat());
            }
        }
        return child;
    }

    @Override protected void customServerAiStep() {
        boolean careActive = goalSelector.getAvailableGoals().stream().filter(net.minecraft.world.entity.ai.goal.WrappedGoal::isRunning).anyMatch(goal ->
                goal.getGoal() instanceof ModernGoals.CareGoal);
        if (careActive) {
            if (!careControlsMovement) {
                getBrain().stopAll((net.minecraft.server.level.ServerLevel) level(), this);
                getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
                getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET);
            }
        } else super.customServerAiStep();
        careControlsMovement = careActive;
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return getData(ModAttachments.SLEEPING) ? null : super.getAmbientSound();
    }

    @Override public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) care.tick();
    }

    @Override public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        if (player.getItemInHand(hand).is(net.minecraft.world.item.Items.BUCKET)) {
            if (!isFemale() || isBaby() || !care.hasMilk() || !LegacyAnimalNeeds.isFed(this)
                    || !LegacyAnimalNeeds.isWatered(this) || getData(ModAttachments.SLEEPING))
                return net.minecraft.world.InteractionResult.PASS;
            var result = super.mobInteract(player, hand);
            if (!level().isClientSide() && result.consumesAction()) care.milked();
            return result;
        }
        return super.mobInteract(player, hand);
    }
}
