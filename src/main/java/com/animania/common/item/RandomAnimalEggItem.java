package com.animania.common.item;

import com.animania.catsdogs.cat.CatBreed;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import com.animania.common.registry.ModEntities;
import com.animania.extra.peafowl.PeafowlBreed;
import com.animania.extra.peafowl.PeafowlRole;
import com.animania.extra.rabbit.RabbitBreed;
import com.animania.extra.rabbit.RabbitRole;
import com.animania.farm.chicken.ChickenBreed;
import com.animania.farm.chicken.ChickenRole;
import com.animania.farm.livestock.CowBreed;
import com.animania.farm.livestock.FarmAnimalRole;
import com.animania.farm.livestock.GoatBreed;
import com.animania.farm.livestock.PigBreed;
import com.animania.farm.livestock.SheepBreed;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class RandomAnimalEggItem extends Item {
    /** Every spawnable family that participated in the original global random egg. */
    public enum Kind {
        ALL, CHICKEN, COW, GOAT, PIG, SHEEP, HORSE, RABBIT, PEAFOWL, CAT, DOG, RODENT, AMPHIBIAN
    }
    private final Kind kind;

    public RandomAnimalEggItem(Kind kind, Properties properties) {
        super(properties.stacksTo(16));
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Kind selected = kind == Kind.ALL ? Kind.values()[1 + level.random.nextInt(Kind.values().length - 1)] : kind;
        EntityType<? extends Mob> type = select(selected, level);
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ItemStack stack = context.getItemInHand();
        Mob spawned = type.spawn(level, stack, context.getPlayer(), pos, MobSpawnType.SPAWN_EGG, true, false);
        if (spawned == null) return InteractionResult.FAIL;
        if (context.getPlayer() == null || !context.getPlayer().hasInfiniteMaterials()) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private static EntityType<? extends Mob> select(Kind kind, ServerLevel level) {
        return switch (kind) {
            case CHICKEN -> ModEntities.chicken(random(ChickenRole.values(), level), random(ChickenBreed.values(), level));
            case COW -> ModEntities.cow(random(FarmAnimalRole.values(), level), random(CowBreed.values(), level));
            case GOAT -> ModEntities.goat(random(FarmAnimalRole.values(), level), random(GoatBreed.values(), level));
            case PIG -> ModEntities.pig(random(FarmAnimalRole.values(), level), random(PigBreed.values(), level));
            case SHEEP -> ModEntities.sheep(random(FarmAnimalRole.values(), level), random(SheepBreed.values(), level));
            case HORSE -> ModEntities.horse(random(FarmAnimalRole.values(), level));
            case RABBIT -> ModEntities.rabbit(random(RabbitRole.values(), level), random(RabbitBreed.values(), level));
            case PEAFOWL -> ModEntities.peafowl(random(PeafowlRole.values(), level), random(PeafowlBreed.values(), level));
            case CAT -> ModEntities.cat(random(CatRole.values(), level), random(CatBreed.values(), level));
            case DOG -> ModEntities.dog(random(DogRole.values(), level), random(DogBreed.values(), level));
            case RODENT -> randomRegistered(ModEntities.ALL_RODENTS, level);
            case AMPHIBIAN -> randomRegistered(ModEntities.ALL_AMPHIBIANS, level);
            case ALL -> throw new IllegalStateException("ALL must be resolved before entity selection");
        };
    }

    private static <T extends Mob> EntityType<? extends Mob> randomRegistered(
            java.util.Map<String, ? extends net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>, EntityType<T>>> values,
            ServerLevel level) {
        var entries = new java.util.ArrayList<>(values.values());
        return entries.get(level.random.nextInt(entries.size())).get();
    }

    private static <T> T random(T[] values, ServerLevel level) {
        return values[level.random.nextInt(values.length)];
    }
}
