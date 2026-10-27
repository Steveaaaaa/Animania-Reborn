package com.animania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A writable deed that carries a single tamed pet.
 *
 * <p>The contract holds the pet as its full saved entity tag, so a transfer survives logging out, walking
 * across the world and changing dimensions. Only a player already recognised as the pet's owner can record
 * it, and the player who pours the pet back out becomes the new owner -- that is the whole point of the item:
 * the original mod let one player tame a pet and never gave a way to hand it to somebody else.
 *
 * <p>Saved tags keep the animal's data attachments, because NeoForge writes serialisable attachments into
 * {@code neoforge:attachments} inside the entity tag, so gender, pedigree and husbandry state travel too.
 * The identifier, position, motion and leash are dropped: a fresh identifier avoids duplicating a pet when a
 * creative copy of the contract is used, and the position is replaced by wherever the pet is released.
 */
public final class PetContractItem extends Item {
    private static final String PET = "Pet";
    private static final String PET_NAME = "PetName";
    private static final String OWNER = "Owner";

    public PetContractItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        String name = recordedPetName(stack);
        return name == null
                ? super.getName(stack)
                : Component.translatable("item.animania.pet_contract.filled", name);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof TamableAnimal pet) || !pet.isTame()) {
            return InteractionResult.PASS;
        }
        if (!pet.isOwnedBy(player)) {
            if (!player.level().isClientSide() && !isRecorded(stack)) {
                message(player, "message.animania.pet_contract.not_owner");
            }
            return InteractionResult.PASS;
        }
        if (isRecorded(stack)) {
            if (!player.level().isClientSide()) {
                message(player, "message.animania.pet_contract.busy");
            }
            return InteractionResult.PASS;
        }
        if (!target.isAlive() || target.isPassenger() || target.isVehicle()) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide()) {
            record(stack, player, pet);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Vec3 spot = Vec3.atBottomCenterOf(context.getClickedPos().relative(context.getClickedFace()));
        return release(context.getLevel(), player, context.getItemInHand(), spot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 spot = new Vec3(player.getX(), player.getY(), player.getZ()).add(player.getLookAngle().scale(1.2));
        InteractionResult result = release(level, player, stack, spot);
        return new InteractionResultHolder<>(result, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                               TooltipFlag flag) {
        String name = recordedPetName(stack);
        if (name == null) {
            tooltip.add(Component.translatable("item.animania.pet_contract.tooltip.empty")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("item.animania.pet_contract.tooltip.recorded", name)
                .withStyle(ChatFormatting.GOLD));
        String owner = recordedOwner(stack);
        if (owner != null) {
            tooltip.add(Component.translatable("item.animania.pet_contract.tooltip.owner", owner)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("item.animania.pet_contract.tooltip.release")
                .withStyle(ChatFormatting.GRAY));
    }

    public static boolean isRecorded(ItemStack stack) {
        return recordedPetName(stack) != null;
    }

    @Nullable
    private static String recordedPetName(ItemStack stack) {
        CompoundTag data = data(stack);
        return data != null && data.contains(PET) && data.contains(PET_NAME, 8) ? data.getString(PET_NAME) : null;
    }

    @Nullable
    private static String recordedOwner(ItemStack stack) {
        CompoundTag data = data(stack);
        return data != null && data.contains(OWNER, 8) ? data.getString(OWNER) : null;
    }

    @Nullable
    private static CompoundTag data(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        return custom == null ? null : custom.copyTag();
    }

    private static void record(ItemStack stack, Player player, TamableAnimal pet) {
        CompoundTag saved = new CompoundTag();
        pet.saveWithoutId(saved);
        saved.remove("UUID");
        saved.remove("Pos");
        saved.remove("Motion");
        saved.remove("FallDistance");
        saved.remove("Leash");

        CompoundTag data = new CompoundTag();
        data.put(PET, saved);
        data.putString(PET_NAME, pet.hasCustomName()
                ? pet.getCustomName().getString()
                : pet.getType().getDescription().getString());
        data.putString(OWNER, player.getGameProfile().getName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));

        Level level = pet.level();
        if (pet.isLeashed()) {
            pet.dropLeash(true, false);
        }
        pet.ejectPassengers();
        pet.discard();
        level.playSound(null, pet.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 1.2F);
        message(player, "message.animania.pet_contract.recorded", data.getString(PET_NAME));
    }

    private static InteractionResult release(Level level, Player player, ItemStack stack, Vec3 spot) {
        CompoundTag data = data(stack);
        if (data == null || !data.contains(PET)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        Optional<Entity> created = EntityType.create(data.getCompound(PET), server);
        if (created.isEmpty()) {
            message(player, "message.animania.pet_contract.broken");
            return InteractionResult.FAIL;
        }
        Entity pet = created.get();
        pet.moveTo(spot.x, spot.y, spot.z, player.getYRot() + 180.0F, 0.0F);
        if (!server.noCollision(pet)) {
            boolean moved = false;
            for (int lift = 1; lift <= 2 && !moved; lift++) {
                pet.moveTo(spot.x, spot.y + lift, spot.z, pet.getYRot(), 0.0F);
                moved = server.noCollision(pet);
            }
            if (!moved) {
                message(player, "message.animania.pet_contract.no_space");
                return InteractionResult.FAIL;
            }
        }
        if (pet instanceof TamableAnimal tame) {
            tame.setTame(true, false);
            tame.setOwnerUUID(player.getUUID());
            tame.setOrderedToSit(false);
        }
        if (pet instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (!server.addFreshEntity(pet)) {
            message(player, "message.animania.pet_contract.broken");
            return InteractionResult.FAIL;
        }
        stack.shrink(1);
        server.playSound(null, pet.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 0.8F);
        message(player, "message.animania.pet_contract.released", pet.getDisplayName().getString());
        return InteractionResult.SUCCESS;
    }

    private static void message(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }
}
