package com.animania.farm.livestock;

import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModSounds;
import com.animania.farm.dairy.MilkType;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;

public final class AnimaniaSheep extends Sheep {
    private static final int GESTATION_TICKS = 20_000;
    private boolean pregnant;
    private boolean milkable;
    private int gestation;
    private SheepBreed mateBreed;
    private int woolRegrowth;

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(3, new RamButtGoal(this));
    }

    public AnimaniaSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
        if (role() == FarmAnimalRole.YOUNG) setBaby(true);
    }

    private String entityPath() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath();
    }

    public SheepBreed breed() {
        return SheepBreed.fromPath(entityPath());
    }

    public FarmAnimalRole role() {
        return FarmAnimalRole.sheepRole(entityPath());
    }

    private boolean wellCaredFor() {
        return getData(ModAttachments.HUNGER) > 20 && getData(ModAttachments.THIRST) > 20;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return com.animania.common.config.LegacyItemMatcher.matches(stack, "sheep");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return role() == FarmAnimalRole.YOUNG ? ModSounds.LAMB_AMBIENT.get() : ModSounds.SHEEP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHEEP_HURT.get();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && woolRegrowth > 0) {
            if (!isSheared()) setSheared(true);
            if (--woolRegrowth == 0) setSheared(false);
        }
        if (!level().isClientSide() && role() == FarmAnimalRole.YOUNG && !isBaby()) growIntoAdult();
        else if (!level().isClientSide() && role() == FarmAnimalRole.FEMALE && pregnant) {
            com.animania.common.entity.LegacyReproduction.wakeForBirth(this, --gestation);
            if (gestation <= 0) giveBirth();
        }
    }

    private void growIntoAdult() {
        if (!(level() instanceof ServerLevel server)) return;
        FarmAnimalRole adultRole = random.nextBoolean() ? FarmAnimalRole.FEMALE : FarmAnimalRole.MALE;
        AnimaniaSheep adult = ModEntities.sheep(adultRole, breed()).create(server);
        if (adult != null) {
            adult.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            adult.setCustomName(getCustomName());
            adult.setCustomNameVisible(isCustomNameVisible());
            adult.setColor(getColor());
            adult.setSheared(isSheared());
            com.animania.common.entity.LegacyAnimalNeeds.copyState(this, adult);
            server.addFreshEntity(adult);
            discard();
        }
    }

    public void childMatured() {
        milkable = false;
    }

    private void giveBirth() {
        if (!(level() instanceof ServerLevel server)) return;
        if (com.animania.common.config.LegacyBreedingRules.shouldLosePregnancy(this, random)) {
            pregnant = false;
            com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
            mateBreed = null;
            return;
        }
        for (int i = 0; i < com.animania.common.config.LegacyBreedingRules.litterSize(random); i++) {
            SheepBreed childBreed = mateBreed != null && random.nextBoolean() ? mateBreed : breed();
            AnimaniaSheep lamb = ModEntities.sheep(FarmAnimalRole.YOUNG, childBreed).create(server);
            if (lamb != null) {
                AnimalInformation.recordParent(lamb, this);
                com.animania.common.entity.LegacyAnimalNeeds.setInteracted(lamb,
                        com.animania.common.entity.LegacyAnimalNeeds.isInteracted(this));
                lamb.moveTo(getX(), getY() + 0.2, getZ(), getYRot(), 0.0F);
                lamb.setColor(getColor());
                server.addFreshEntity(lamb);
            }
        }
        pregnant = false;
        com.animania.common.entity.LegacyReproduction.completedPregnancy(this);
        milkable = true;
        mateBreed = null;
    }

    @Override
    public boolean canMate(Animal other) {
        if (!(other instanceof AnimaniaSheep sheep) || other == this || isBaby() || sheep.isBaby()) return false;
        if (AnimalInformation.isSterilized(this) || AnimalInformation.isSterilized(sheep)) return false;
        AnimaniaSheep female = role() == FarmAnimalRole.FEMALE ? this
                : sheep.role() == FarmAnimalRole.FEMALE ? sheep : null;
        boolean opposite = role() != sheep.role() && role() != FarmAnimalRole.YOUNG
                && sheep.role() != FarmAnimalRole.YOUNG;
        return opposite && female != null && !female.pregnant && wellCaredFor() && sheep.wellCaredFor()
                && isInLove() && sheep.isInLove()
                && com.animania.common.config.LegacyBreedingRules.canMate(this, sheep);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        if (!(mate instanceof AnimaniaSheep sheep)) return;
        AnimaniaSheep female = role() == FarmAnimalRole.FEMALE ? this : sheep;
        AnimaniaSheep male = female == this ? sheep : this;
        AnimalInformation.recordMating(this, sheep);
        female.pregnant = true;
        female.gestation = com.animania.common.config.LegacyConfig.GESTATION_TIMER.get() + random.nextInt(200);
        female.mateBreed = male.breed();
        setAge(6000);
        sheep.setAge(6000);
        resetLove();
        sheep.resetLove();
        level.broadcastEntityEvent(female, (byte) 18);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (breed() == SheepBreed.FRIESIAN) {
            setColor(switch (random.nextInt(3)) {
                case 0 -> DyeColor.BLACK;
                case 1 -> DyeColor.BROWN;
                default -> DyeColor.WHITE;
            });
        } else if (breed() == SheepBreed.DORSET || breed() == SheepBreed.MERINO || breed() == SheepBreed.SUFFOLK) {
            setColor(random.nextBoolean() ? DyeColor.BROWN : DyeColor.WHITE);
        } else {
            setColor(DyeColor.WHITE);
        }
        if (role() == FarmAnimalRole.FEMALE) {
            var family = com.animania.common.entity.LegacyNaturalFamily.spawn(level, this, spawnType,
                    AnimaniaSheep.class, 8,
                    () -> ModEntities.sheep(FarmAnimalRole.MALE, breed()).create(level.getLevel()),
                    () -> ModEntities.sheep(FarmAnimalRole.YOUNG, breed()).create(level.getLevel()),
                    companion -> ((AnimaniaSheep) companion).setColor(getColor()));
            if (family == com.animania.common.entity.LegacyNaturalFamily.Result.YOUNG) milkable = true;
        }
        return result;
    }

    @Override
    public void shear(SoundSource category) {
        level().playSound(null, this, SoundEvents.SHEEP_SHEAR, category, 1.0F, 1.0F);
        setSheared(true);
        woolRegrowth = com.animania.common.config.LegacyConfig.WOOL_REGROWTH_TIMER.get();
        ItemStack wool = breedWool();
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            ItemEntity drop = spawnAtLocation(wool.getItem(), 1);
            if (drop != null) drop.setDeltaMovement(drop.getDeltaMovement().add(
                    (random.nextFloat() - random.nextFloat()) * 0.1F, random.nextFloat() * 0.05F,
                    (random.nextFloat() - random.nextFloat()) * 0.1F));
        }
    }

    private ItemStack breedWool() {
        String type = switch (breed()) {
            case DORSET -> getColor() == DyeColor.BROWN ? "dorset_brown" : null;
            case FRIESIAN -> getColor() == DyeColor.BLACK ? "friesian_black"
                    : getColor() == DyeColor.BROWN ? "friesian_brown" : null;
            case JACOB -> "jacob";
            case MERINO -> getColor() == DyeColor.BROWN ? "merino_brown" : "merino_white";
            case SUFFOLK -> getColor() == DyeColor.BROWN ? "suffolk_brown" : null;
            case DORPER -> null;
        };
        return type == null ? new ItemStack(Items.WHITE_WOOL) : new ItemStack(ModItems.animaniaWool(type).get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.BUCKET) && role() == FarmAnimalRole.FEMALE && milkable && wellCaredFor() && !isBaby()) {
            if (!level().isClientSide()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                        new ItemStack(ModItems.milkBucket(MilkType.SHEEP).get())));
                com.animania.common.entity.LegacyAnimalNeeds.setWatered(this, false);
                milkable = false;
            }
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (stack.isEmpty() && player.isShiftKeyDown() && !level().isClientSide()) {
            player.displayClientMessage(Component.translatable(pregnant
                    ? "message.animania.pregnant" : milkable
                    ? "message.animania.milkable" : "message.animania.not_pregnant", gestation), true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    @Nullable
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        SheepBreed childBreed = otherParent instanceof AnimaniaSheep sheep && random.nextBoolean()
                ? sheep.breed() : breed();
        return ModEntities.sheep(FarmAnimalRole.YOUNG, childBreed).create(level);
    }

    @Override
    public ResourceKey<LootTable> getDefaultLootTable() {
        if (role() == FarmAnimalRole.YOUNG) return BuiltInLootTables.EMPTY;
        return breed().isPrime()
                ? ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("animania", "entities/sheep_prime"))
                : EntityType.SHEEP.getDefaultLootTable();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Pregnant", pregnant);
        tag.putBoolean("HasKids", milkable);
        tag.putInt("Gestation", gestation);
        tag.putInt("WoolRegrowth", woolRegrowth);
        if (mateBreed != null) tag.putString("MateBreed", mateBreed.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pregnant = tag.getBoolean("Pregnant");
        milkable = tag.getBoolean("HasKids");
        gestation = tag.getInt("Gestation");
        woolRegrowth = Math.max(0, tag.getInt("WoolRegrowth"));
        if (tag.contains("MateBreed")) mateBreed = SheepBreed.fromPath(tag.getString("MateBreed"));
    }

    private static final class RamButtGoal extends Goal {
        private final AnimaniaSheep ram;
        private AnimaniaSheep rival;
        private int cooldown = 800;
        private int fightTicks;
        private boolean struck;

        private RamButtGoal(AnimaniaSheep ram) {
            this.ram = ram;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (ram.role() != FarmAnimalRole.MALE || !ram.level().isDay() || ram.isBaby() || --cooldown > 0) {
                return false;
            }
            rival = ram.level().getEntitiesOfClass(AnimaniaSheep.class,
                            ram.getBoundingBox().inflate(10.0), sheep -> sheep != ram
                                    && sheep.role() == FarmAnimalRole.MALE && !sheep.isBaby() && sheep.isAlive())
                    .stream().min(Comparator.comparingDouble(ram::distanceToSqr)).orElse(null);
            return rival != null;
        }

        @Override
        public void start() {
            fightTicks = 140;
            struck = false;
            ram.getNavigation().moveTo(rival, 1.3);
        }

        @Override
        public boolean canContinueToUse() {
            return rival != null && rival.isAlive() && fightTicks-- > 0;
        }

        @Override
        public void tick() {
            ram.getLookControl().setLookAt(rival, 10.0F, ram.getMaxHeadXRot());
            ram.getNavigation().moveTo(rival, 1.3);
            if (!struck && ram.distanceToSqr(rival) < 3.0) {
                struck = true;
                rival.hurt(ram.damageSources().mobAttack(ram), 2.0F);
                rival.knockback(1.1, ram.getX() - rival.getX(), ram.getZ() - rival.getZ());
                ram.playSound(SoundEvents.GOAT_RAM_IMPACT, 0.8F, 0.9F + ram.random.nextFloat() * 0.2F);
            }
        }

        @Override
        public void stop() {
            ram.getNavigation().stop();
            rival = null;
            cooldown = 1_000 + ram.random.nextInt(500);
        }
    }
}
