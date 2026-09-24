package com.animania.modern;

import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.entity.LegacyGrowth;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ModernAxolotl extends Axolotl {
    private final Variant fixedVariant;
    private final BreedGoal breeding;
    private final AxolotlShelterGoal shelter;
    private boolean careControlsMovement;

    public ModernAxolotl(EntityType<? extends Axolotl> type, Level level, Variant variant) {
        super(type, level);
        fixedVariant = variant;
        super.setVariant(variant);
        breeding = new BreedGoal(this, 1.0, ModernAxolotl.class) {
            @Override public boolean canUse() {
                return !isPlayingDead() && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() > 100) && super.canUse();
            }
            @Override public boolean canContinueToUse() {
                return !isPlayingDead() && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() > 100) && super.canContinueToUse();
            }
        };
        goalSelector.addGoal(1, breeding);
        shelter = new AxolotlShelterGoal(this);
        goalSelector.addGoal(3, shelter);
    }
    public String breed() { return fixedVariant.getName(); }
    @Override public void setVariant(Variant variant) { super.setVariant(fixedVariant == null ? variant : fixedVariant); }
    @Override public boolean canMate(Animal other) {
        return other instanceof ModernAxolotl && com.animania.common.entity.HusbandryMood.breedingAllowed(this)
                && com.animania.common.entity.HusbandryMood.breedingAllowed(other) && super.canMate(other)
                && LegacyAnimalNeeds.isFed(this) && LegacyAnimalNeeds.isWatered(this)
                && LegacyAnimalNeeds.isFed(other) && LegacyAnimalNeeds.isWatered(other);
    }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        if (!(partner instanceof ModernAxolotl other)) return null;
        Variant variant = random.nextInt(1200) == 0 ? Variant.BLUE : random.nextBoolean() ? fixedVariant : other.fixedVariant;
        ModernAxolotl child = ModEntities.AXOLOTL_BREEDS.get(variant.getName()).get().create(level);
        if (child != null) child.setPersistenceRequired();
        return child;
    }
    void pauseBrainForShelter() {
        // Stop native movement before the shelter goal installs its own path.
        getBrain().stopAll((ServerLevel) level(), this);
        getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        careControlsMovement = true;
    }
    @Override protected void customServerAiStep() {
        boolean active = goalSelector.getAvailableGoals().stream().anyMatch(goal -> goal.isRunning() && (goal.getGoal() == breeding || goal.getGoal() == shelter));
        if (active) {
            if (!careControlsMovement) {
                getBrain().stopAll((ServerLevel) level(), this);
                getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            }
        } else super.customServerAiStep();
        careControlsMovement = active;
    }
    public void tickCare() {
        if (isInWaterOrBubble() && (!ModAttachments.getData(this, ModAttachments.WATERED) || ModAttachments.getData(this, ModAttachments.WATERED_TIMER) <= 1))
            LegacyAnimalNeeds.setWatered(this, true);
        LegacyAnimalNeeds.tick(this);
        LegacyGrowth.tick(this);
    }
    @Override public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity prey && !prey.isAlive() && isInWaterOrBubble())
            LegacyAnimalNeeds.feed(this, false, false);
        return hit;
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean food = isFood(player.getItemInHand(hand));
        InteractionResult result = super.mobInteract(player, hand);
        if (food) {
            if (!level().isClientSide()) {
                if (!result.consumesAction()) usePlayerItem(player, hand, player.getItemInHand(hand));
                LegacyAnimalNeeds.feed(this, true, false);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return result;
    }
    @Override public ItemStack getBucketItemStack() { return new ItemStack(ModItems.AXOLOTL_BUCKETS.get(breed()).get()); }
    @Override public void saveToBucketTag(ItemStack stack) {
        super.saveToBucketTag(stack);
        CompoundTag care = new CompoundTag();
        care.putBoolean("NEEDS_INITIALIZED", ModAttachments.getData(this, ModAttachments.NEEDS_INITIALIZED));
        care.putBoolean("FED", ModAttachments.getData(this, ModAttachments.FED));
        care.putBoolean("WATERED", ModAttachments.getData(this, ModAttachments.WATERED));
        care.putBoolean("HAND_FED", ModAttachments.getData(this, ModAttachments.HAND_FED));
        care.putBoolean("INTERACTED", ModAttachments.getData(this, ModAttachments.INTERACTED));
        care.putInt("FED_TIMER", ModAttachments.getData(this, ModAttachments.FED_TIMER));
        care.putInt("WATERED_TIMER", ModAttachments.getData(this, ModAttachments.WATERED_TIMER));
        care.putInt("STARVATION_TIMER", ModAttachments.getData(this, ModAttachments.STARVATION_TIMER));
        care.putInt("UNHAPPY_TIMER", ModAttachments.getData(this, ModAttachments.UNHAPPY_TIMER));
        care.putInt("HUNGER", ModAttachments.getData(this, ModAttachments.HUNGER));
        care.putInt("THIRST", ModAttachments.getData(this, ModAttachments.THIRST));
        care.putInt("CHILD_GROWTH", ModAttachments.getData(this, ModAttachments.CHILD_GROWTH));
        care.putInt("CHILD_GROWTH_TIMER", ModAttachments.getData(this, ModAttachments.CHILD_GROWTH_TIMER));
        care.putInt("CARE_LEASE", ModAttachments.getData(this, ModAttachments.CARE_LEASE));
        care.putInt("MOOD_ENROLLED", ModAttachments.getData(this, ModAttachments.MOOD_ENROLLED));
        care.putInt("MOOD_SCORE", ModAttachments.getData(this, ModAttachments.MOOD_SCORE));
        care.putInt("MOOD_GRACE", ModAttachments.getData(this, ModAttachments.MOOD_GRACE));
        care.putLong("MOOD_CALENDAR", ModAttachments.getData(this, ModAttachments.MOOD_CALENDAR));
        care.putInt("MOOD_MISSING", ModAttachments.getData(this, ModAttachments.MOOD_MISSING));

        stack.getOrCreateTag().put("AnimaniaCare", care);
    }
    @Override public void loadFromBucketTag(CompoundTag tag) {
        super.loadFromBucketTag(tag);
        if (!tag.contains("AnimaniaCare")) return;
        CompoundTag care = tag.getCompound("AnimaniaCare");
        if (care.contains("CARE_LEASE")) ModAttachments.setData(this, ModAttachments.CARE_LEASE, care.getInt("CARE_LEASE"));
        if (care.contains("MOOD_ENROLLED")) ModAttachments.setData(this, ModAttachments.MOOD_ENROLLED, care.getInt("MOOD_ENROLLED"));
        if (care.contains("MOOD_SCORE")) ModAttachments.setData(this, ModAttachments.MOOD_SCORE, care.getInt("MOOD_SCORE"));
        if (care.contains("MOOD_GRACE")) ModAttachments.setData(this, ModAttachments.MOOD_GRACE, care.getInt("MOOD_GRACE"));
        if (care.contains("MOOD_CALENDAR")) ModAttachments.setData(this, ModAttachments.MOOD_CALENDAR, care.getLong("MOOD_CALENDAR"));
        if (care.contains("MOOD_MISSING")) ModAttachments.setData(this, ModAttachments.MOOD_MISSING, care.getInt("MOOD_MISSING"));

        ModAttachments.setData(this, ModAttachments.NEEDS_INITIALIZED, care.getBoolean("NEEDS_INITIALIZED"));
        ModAttachments.setData(this, ModAttachments.FED, care.getBoolean("FED"));
        ModAttachments.setData(this, ModAttachments.WATERED, care.getBoolean("WATERED"));
        ModAttachments.setData(this, ModAttachments.HAND_FED, care.getBoolean("HAND_FED"));
        ModAttachments.setData(this, ModAttachments.INTERACTED, care.getBoolean("INTERACTED"));
        ModAttachments.setData(this, ModAttachments.FED_TIMER, care.getInt("FED_TIMER"));
        ModAttachments.setData(this, ModAttachments.WATERED_TIMER, care.getInt("WATERED_TIMER"));
        ModAttachments.setData(this, ModAttachments.STARVATION_TIMER, care.getInt("STARVATION_TIMER"));
        ModAttachments.setData(this, ModAttachments.UNHAPPY_TIMER, care.getInt("UNHAPPY_TIMER"));
        ModAttachments.setData(this, ModAttachments.HUNGER, care.getInt("HUNGER"));
        ModAttachments.setData(this, ModAttachments.THIRST, care.getInt("THIRST"));
        ModAttachments.setData(this, ModAttachments.CHILD_GROWTH, care.getInt("CHILD_GROWTH"));
        ModAttachments.setData(this, ModAttachments.CHILD_GROWTH_TIMER, care.getInt("CHILD_GROWTH_TIMER"));
    }
    @Override public void tick() {
        com.animania.common.entity.AnimalTickBridge.before(this);
        super.tick();
        com.animania.common.entity.AnimalTickBridge.after(this);
    }

}
