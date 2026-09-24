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
        if (isInWaterOrBubble() && (!getData(ModAttachments.WATERED) || getData(ModAttachments.WATERED_TIMER) <= 1))
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
        care.putBoolean("NEEDS_INITIALIZED", getData(ModAttachments.NEEDS_INITIALIZED));
        care.putBoolean("FED", getData(ModAttachments.FED));
        care.putBoolean("WATERED", getData(ModAttachments.WATERED));
        care.putBoolean("HAND_FED", getData(ModAttachments.HAND_FED));
        care.putBoolean("INTERACTED", getData(ModAttachments.INTERACTED));
        care.putInt("FED_TIMER", getData(ModAttachments.FED_TIMER));
        care.putInt("WATERED_TIMER", getData(ModAttachments.WATERED_TIMER));
        care.putInt("STARVATION_TIMER", getData(ModAttachments.STARVATION_TIMER));
        care.putInt("UNHAPPY_TIMER", getData(ModAttachments.UNHAPPY_TIMER));
        care.putInt("HUNGER", getData(ModAttachments.HUNGER));
        care.putInt("THIRST", getData(ModAttachments.THIRST));
        care.putInt("CHILD_GROWTH", getData(ModAttachments.CHILD_GROWTH));
        care.putInt("CHILD_GROWTH_TIMER", getData(ModAttachments.CHILD_GROWTH_TIMER));
        care.putInt("CARE_LEASE", getData(ModAttachments.CARE_LEASE));
        care.putInt("MOOD_ENROLLED", getData(ModAttachments.MOOD_ENROLLED));
        care.putInt("MOOD_SCORE", getData(ModAttachments.MOOD_SCORE));
        care.putInt("MOOD_GRACE", getData(ModAttachments.MOOD_GRACE));
        care.putLong("MOOD_CALENDAR", getData(ModAttachments.MOOD_CALENDAR));
        care.putInt("MOOD_MISSING", getData(ModAttachments.MOOD_MISSING));

        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA,
                stack, tag -> tag.put("AnimaniaCare", care));
    }
    @Override public void loadFromBucketTag(CompoundTag tag) {
        super.loadFromBucketTag(tag);
        if (!tag.contains("AnimaniaCare")) return;
        CompoundTag care = tag.getCompound("AnimaniaCare");
        if (care.contains("CARE_LEASE")) setData(ModAttachments.CARE_LEASE, care.getInt("CARE_LEASE"));
        if (care.contains("MOOD_ENROLLED")) setData(ModAttachments.MOOD_ENROLLED, care.getInt("MOOD_ENROLLED"));
        if (care.contains("MOOD_SCORE")) setData(ModAttachments.MOOD_SCORE, care.getInt("MOOD_SCORE"));
        if (care.contains("MOOD_GRACE")) setData(ModAttachments.MOOD_GRACE, care.getInt("MOOD_GRACE"));
        if (care.contains("MOOD_CALENDAR")) setData(ModAttachments.MOOD_CALENDAR, care.getLong("MOOD_CALENDAR"));
        if (care.contains("MOOD_MISSING")) setData(ModAttachments.MOOD_MISSING, care.getInt("MOOD_MISSING"));

        setData(ModAttachments.NEEDS_INITIALIZED, care.getBoolean("NEEDS_INITIALIZED"));
        setData(ModAttachments.FED, care.getBoolean("FED"));
        setData(ModAttachments.WATERED, care.getBoolean("WATERED"));
        setData(ModAttachments.HAND_FED, care.getBoolean("HAND_FED"));
        setData(ModAttachments.INTERACTED, care.getBoolean("INTERACTED"));
        setData(ModAttachments.FED_TIMER, care.getInt("FED_TIMER"));
        setData(ModAttachments.WATERED_TIMER, care.getInt("WATERED_TIMER"));
        setData(ModAttachments.STARVATION_TIMER, care.getInt("STARVATION_TIMER"));
        setData(ModAttachments.UNHAPPY_TIMER, care.getInt("UNHAPPY_TIMER"));
        setData(ModAttachments.HUNGER, care.getInt("HUNGER"));
        setData(ModAttachments.THIRST, care.getInt("THIRST"));
        setData(ModAttachments.CHILD_GROWTH, care.getInt("CHILD_GROWTH"));
        setData(ModAttachments.CHILD_GROWTH_TIMER, care.getInt("CHILD_GROWTH_TIMER"));
    }
}
