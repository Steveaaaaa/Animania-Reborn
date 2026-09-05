package com.animania.farm.vehicle;

import com.animania.common.registry.ModSounds;

import com.animania.common.registry.ModItems;
import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/** A modern replacement for Animania's three CraftStudio-backed farm vehicles. */
public final class FarmVehicleEntity extends Entity implements MenuProvider {
    /** Farthest Tow7 vertex after the original wagon hierarchy is composed. */
    public static final double WAGON_SHAFT_REACH = 4.329062D;
    private static final EntityDataAccessor<Optional<UUID>> PULLER =
            SynchedEntityData.defineId(FarmVehicleEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    /** Runtime entity id lets the client predict the rigid drawbar every tick. */
    private static final EntityDataAccessor<Integer> PULLER_ID =
            SynchedEntityData.defineId(FarmVehicleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_CHEST =
            SynchedEntityData.defineId(FarmVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    private final Kind kind;
    private final SimpleContainer inventory;
    private float accumulatedDamage;

    public FarmVehicleEntity(EntityType<? extends FarmVehicleEntity> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.inventory = new SimpleContainer(kind.slots);
        entityData.set(HAS_CHEST, kind != Kind.CART);
        blocksBuilding = true;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PULLER, Optional.empty());
        builder.define(PULLER_ID, -1);
        builder.define(HAS_CHEST, false);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Entity puller = puller();
        if (puller != null) {
            double distance = distanceTo(puller);
            if (!level().isClientSide() && (!puller.isAlive() || distance > 12.0D)) {
                setPuller(null);
                return;
            }
            followDrawbar(puller);
            if (!level().isClientSide() && kind == Kind.TILLER && tickCount % 3 == 0) tillGround();
        } else if (!level().isClientSide()) {
            Vec3 motion = getDeltaMovement();
            if (!onGround()) motion = motion.add(0.0D, -0.08D, 0.0D);
            setDeltaMovement(motion.multiply(0.82D, 0.98D, 0.82D));
            move(MoverType.SELF, getDeltaMovement());
        }
        if (!level().isClientSide()) handleNearbyPassengers();
    }

    /**
     * Both logical sides run the drawbar constraint. The wagon's central pole
     * must end behind the animal, unlike the legacy 3.2-block centre spacing
     * which puts its 4.329-block pole through a single horse.
     */
    private void followDrawbar(Entity puller) {
        Vec3 towardAnimal = new Vec3(puller.getX() - getX(), 0.0D, puller.getZ() - getZ());
        if (towardAnimal.lengthSqr() < 1.0E-6D) towardAnimal = horizontalForward(puller.getYRot());
        else towardAnimal = towardAnimal.normalize();

        Vec3 target = puller.position().subtract(towardAnimal.scale(pullerDistance(puller)));
        Vec3 correction = new Vec3(target.x - getX(), puller.getY() - getY(), target.z - getZ());
        double horizontal = Math.sqrt(correction.x * correction.x + correction.z * correction.z);
        double scale = horizontal > 1.5D ? 1.5D / horizontal : 1.0D;
        Vec3 step = new Vec3(correction.x * scale, Mth.clamp(correction.y, -0.75D, 0.75D),
                correction.z * scale);
        setDeltaMovement(step);
        if (step.lengthSqr() > 1.0E-8D) move(MoverType.SELF, step);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-towardAnimal.x, towardAnimal.z));
        setYRot(targetYaw);
    }

    private static Vec3 horizontalForward(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
    }

    private double pullerDistance(Entity puller) {
        // The adult draft-horse body extends 19/16 * 0.85 blocks rearward;
        // allow its turning envelope plus a short gap ahead of the pole tip.
        return kind == Kind.WAGON
                ? WAGON_SHAFT_REACH + Math.max(1.15D, puller.getBbWidth() * 0.5D) + 0.35D
                : kind.centerDistance;
    }

    private void handleNearbyPassengers() {
        if (!LegacyConfig.ALLOW_MOB_RIDING.get()) {
            for (Entity passenger : List.copyOf(getPassengers())) {
                if (passenger instanceof Animal) passenger.stopRiding();
            }
            return;
        }
        // In 1.12 the cart and tiller collected small, player-leashed animals
        // which touched their collision box, with two seats available.
        if (kind == Kind.WAGON || getPassengers().size() >= 2) return;
        for (Animal animal : level().getEntitiesOfClass(Animal.class,
                getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
            if (animal == puller() || animal.isPassenger() || animal.getBbWidth() >= getBbWidth()) continue;
            if (animal.isLeashed() && animal.getLeashHolder() instanceof Player
                    && animal.startRiding(this, true) && getPassengers().size() >= 2) return;
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < 2;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        int index = Math.max(0, getPassengers().indexOf(passenger));
        double foreAft = index == 0 ? 0.25D : -0.55D;
        if (kind == Kind.WAGON) foreAft += 0.35D;
        double height = kind == Kind.WAGON ? 1.05D : 0.72D;
        return new Vec3(0.0D, height, foreAft).yRot(-getYRot() * ((float) Math.PI / 180.0F));
    }

    private void tillGround() {
        BlockPos center = blockPosition().below();
        float radians = (float) Math.toRadians(getYRot());
        int sideX = Math.round((float) Math.cos(radians));
        int sideZ = Math.round((float) Math.sin(radians));
        boolean changed = false;
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos pos = center.offset(sideX * offset, 0, sideZ * offset);
            BlockState state = level().getBlockState(pos);
            if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH))
                    && level().getBlockState(pos.above()).isAir()) {
                level().setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 11);
                plantFromInventory(pos.above());
                changed = true;
            }
        }
        if (changed) level().playSound(null, blockPosition(), SoundEvents.HOE_TILL,
                SoundSource.BLOCKS, 0.7F, 1.0F);
    }

    private void plantFromInventory(BlockPos pos) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            BlockState crop = stack.is(Items.WHEAT_SEEDS) ? Blocks.WHEAT.defaultBlockState()
                    : stack.is(Items.BEETROOT_SEEDS) ? Blocks.BEETROOTS.defaultBlockState()
                    : stack.is(Items.CARROT) ? Blocks.CARROTS.defaultBlockState()
                    : stack.is(Items.POTATO) ? Blocks.POTATOES.defaultBlockState() : null;
            if (crop != null && level().getBlockState(pos).isAir()) {
                level().setBlock(pos, crop, 3);
                stack.shrink(1);
                inventory.setChanged();
                return;
            }
        }
    }

    @Nullable
    private Entity puller() {
        if (level() instanceof ServerLevel server) {
            Optional<UUID> uuid = entityData.get(PULLER);
            Entity entity = uuid.map(server::getEntity).orElse(null);
            if (entity != null && entityData.get(PULLER_ID) != entity.getId()) {
                entityData.set(PULLER_ID, entity.getId());
            }
            return entity;
        }
        int id = entityData.get(PULLER_ID);
        Entity entity = id < 0 ? null : level().getEntity(id);
        Optional<UUID> uuid = entityData.get(PULLER);
        return entity != null && uuid.isPresent() && uuid.get().equals(entity.getUUID()) ? entity : null;
    }

    public boolean isPulledBy(Entity entity) {
        return entity != null && entity == puller();
    }

    /** Client-visible puller used for aligned pitch and harness rendering. */
    @Nullable
    public Entity pullerForRendering() {
        return puller();
    }

    public boolean hasChest() {
        return entityData.get(HAS_CHEST);
    }

    private void setPuller(@Nullable Entity entity) {
        entityData.set(PULLER, entity == null ? Optional.empty() : Optional.of(entity.getUUID()));
        entityData.set(PULLER_ID, entity == null ? -1 : entity.getId());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (kind != Kind.CART || hasChest()) {
                player.openMenu(this);
            } else if (player.getVehicle() == null) {
                player.startRiding(this);
            }
            return InteractionResult.CONSUME;
        }

        if (kind == Kind.CART && !hasChest() && held.is(Blocks.CHEST.asItem()) && getPassengers().isEmpty()) {
            entityData.set(HAS_CHEST, true);
            if (!player.isCreative()) held.shrink(1);
            level().playSound(null, blockPosition(), SoundEvents.CHEST_LOCKED,
                    SoundSource.PLAYERS, 0.7F, 1.0F);
            return InteractionResult.CONSUME;
        }

        if (kind == Kind.WAGON && LegacyConfig.SLEEP_ALLOWED_WAGON.get()
                && held.isEmpty() && player instanceof ServerPlayer serverPlayer
                && !level().isDay() && entityData.get(PULLER).isEmpty()) {
            if (!level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(8.0D, 5.0D, 8.0D)).isEmpty()) {
                player.displayClientMessage(Component.translatable("block.minecraft.bed.not_safe"), true);
                return InteractionResult.CONSUME;
            }
            serverPlayer.teleportTo(getX(), getY() + 0.8D, getZ());
            serverPlayer.startSleeping(blockPosition());
            serverPlayer.setRespawnPosition(level().dimension(), blockPosition(), getYRot(), true, false);
            return InteractionResult.CONSUME;
        }

        Entity ridden = player.getVehicle();
        if (ridden != null && ridden != this && isAllowedPuller(ridden)) {
            if (isPulledBy(ridden)) {
                unhitch(player);
            } else if (!pullerAlreadyInUse(ridden)) {
                hitch(ridden, player);
            }
            return InteractionResult.CONSUME;
        }

        Animal leashed = nearbyLeashedPuller(player);
        if ((held.isEmpty() || held.is(Items.LEAD)) && leashed != null) {
            hitch(leashed, player);
            leashed.dropLeash(true, false);
            if (!player.isCreative()) player.getInventory().add(new ItemStack(Items.LEAD));
            return InteractionResult.CONSUME;
        }

        if (entityData.get(PULLER).isPresent()) {
            unhitch(player);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.animania.vehicle_needs_puller"), true);
        return InteractionResult.CONSUME;
    }

    private void hitch(Entity entity, Player actor) {
        if (!isAllowedPuller(entity)) return;
        setPuller(entity);
        alignBehind(entity);
        level().playSound(null, blockPosition(), ModSounds.VEHICLE_HITCH.get(),
                SoundSource.BLOCKS, 0.8F, 1.0F);
        actor.displayClientMessage(Component.translatable("message.animania.vehicle_hitched"), true);
    }

    private void unhitch(Player actor) {
        setPuller(null);
        setDeltaMovement(Vec3.ZERO);
        level().playSound(null, blockPosition(), ModSounds.VEHICLE_UNHITCH.get(),
                SoundSource.BLOCKS, 0.8F, 1.0F);
        actor.displayClientMessage(Component.translatable("message.animania.vehicle_unhitched"), true);
    }

    private boolean pullerAlreadyInUse(Entity candidate) {
        return !level().getEntitiesOfClass(FarmVehicleEntity.class,
                candidate.getBoundingBox().inflate(16.0D), other -> other != this && other.isPulledBy(candidate)).isEmpty();
    }

    @Nullable
    private Animal nearbyLeashedPuller(Player player) {
        return level().getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(3.0D),
                        animal -> animal.isLeashed() && animal.getLeashHolder() == player && isAllowedPuller(animal))
                .stream().findFirst().orElse(null);
    }

    private boolean isAllowedPuller(Entity entity) {
        if (entity instanceof AbstractHorse horse && (horse.isBaby() || !horse.isTamed())) return false;
        if (entity instanceof Animal animal && animal.isBaby()) return false;
        return switch (kind) {
            case CART -> entity instanceof AbstractHorse || entity instanceof com.animania.farm.livestock.AnimaniaPig;
            case WAGON -> entity instanceof AbstractHorse;
            case TILLER -> entity instanceof AbstractHorse || entity instanceof com.animania.farm.livestock.AnimaniaCow;
        };
    }

    private void alignBehind(Entity puller) {
        Vec3 forward = horizontalForward(puller.getYRot());
        Vec3 target = puller.position().subtract(forward.scale(pullerDistance(puller)));
        moveTo(target.x, puller.getY(), target.z, puller.getYRot(), 0.0F);
        setOldPosAndRot();
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || level().isClientSide()) return false;
        accumulatedDamage += amount;
        Entity attacker = source.getEntity();
        if (accumulatedDamage >= 6.0F || attacker instanceof Player player && player.isCreative()) {
            if (kind != Kind.CART || hasChest()) Containers.dropContents(level(), this, inventory);
            if (kind == Kind.CART && hasChest()) spawnAtLocation(Items.CHEST);
            if (!(attacker instanceof Player player && player.isCreative())) spawnAtLocation(kind.item());
            discard();
        } else {
            markHurt();
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.animania." + kind.id);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return switch (kind) {
            case CART -> new ChestMenu(MenuType.GENERIC_9x4, containerId, playerInventory, inventory, 4);
            case WAGON -> ChestMenu.sixRows(containerId, playerInventory, inventory);
            case TILLER -> new ChestMenu(MenuType.GENERIC_9x1, containerId, playerInventory, inventory, 1);
        };
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        HolderLookup.Provider registries = level().registryAccess();
        tag.put("Items", inventory.createTag(registries));
        tag.putBoolean("HasChest", hasChest());
        entityData.get(PULLER).ifPresent(uuid -> tag.putUUID("Puller", uuid));
        tag.putFloat("Damage", accumulatedDamage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        inventory.fromTag(tag.getList("Items", Tag.TAG_COMPOUND), level().registryAccess());
        entityData.set(HAS_CHEST, kind != Kind.CART || tag.getBoolean("HasChest"));
        setPuller(tag.hasUUID("Puller") ? level() instanceof ServerLevel server
                ? server.getEntity(tag.getUUID("Puller")) : null : null);
        if (tag.hasUUID("Puller")) entityData.set(PULLER, Optional.of(tag.getUUID("Puller")));
        entityData.set(PULLER_ID, -1);
        accumulatedDamage = tag.getFloat("Damage");
    }

    public enum Kind {
        CART("cart", 36, 2.5D), WAGON("wagon", 54, 3.2D), TILLER("tiller", 9, 2.5D);

        private final String id;
        private final int slots;
        private final double centerDistance;

        Kind(String id, int slots, double centerDistance) {
            this.id = id;
            this.slots = slots;
            this.centerDistance = centerDistance;
        }

        public String id() {
            return id;
        }

        public double centerDistance() {
            return centerDistance;
        }

        public Item item() {
            return switch (this) {
                case CART -> ModItems.CART.get();
                case WAGON -> ModItems.WAGON.get();
                case TILLER -> ModItems.TILLER.get();
            };
        }
    }
}
