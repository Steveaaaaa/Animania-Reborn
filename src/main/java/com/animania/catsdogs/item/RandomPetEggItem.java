package com.animania.catsdogs.item;

import com.animania.catsdogs.cat.CatBreed;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import com.animania.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class RandomPetEggItem extends Item {
    private final Kind kind;

    public RandomPetEggItem(Kind kind, Properties properties) {
        super(properties.stacksTo(16));
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        EntityType<? extends Mob> type;
        if (kind == Kind.CAT) {
            CatBreed[] breeds = CatBreed.values();
            CatRole[] roles = CatRole.values();
            type = ModEntities.cat(roles[level.random.nextInt(roles.length)], breeds[level.random.nextInt(breeds.length)]);
        } else {
            DogBreed[] breeds = DogBreed.values();
            DogRole[] roles = DogRole.values();
            type = ModEntities.dog(roles[level.random.nextInt(roles.length)], breeds[level.random.nextInt(breeds.length)]);
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ItemStack stack = context.getItemInHand();
        Mob spawned = type.spawn(server, stack, context.getPlayer(), pos, MobSpawnType.SPAWN_EGG, true, false);
        if (spawned == null) return InteractionResult.FAIL;
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    public enum Kind { CAT, DOG }
}
