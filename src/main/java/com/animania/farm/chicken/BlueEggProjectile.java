package com.animania.farm.chicken;

import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** A thrown cold-chicken egg keeps its breed when it hatches. */
public final class BlueEggProjectile extends ThrownEgg {
    public BlueEggProjectile(EntityType<? extends ThrownEgg> type, Level level) { super(type, level); }

    @Override protected Item getDefaultItem() { return ModItems.BLUE_EGG.get(); }

    @Override protected void onHit(HitResult hit) {
        if (hit instanceof net.minecraft.world.phys.EntityHitResult entityHit) onHitEntity(entityHit);
        else if (hit instanceof net.minecraft.world.phys.BlockHitResult blockHit) onHitBlock(blockHit);
        if (level().isClientSide()) return;
        if (random.nextInt(8) == 0) {
            int count = random.nextInt(32) == 0 ? 4 : 1;
            for (int i = 0; i < count; i++) {
                var chick = ModEntities.chicken(ChickenRole.CHICK, ChickenBreed.COLD).create(level());
                if (chick != null) {
                    chick.moveTo(getX(), getY(), getZ(), getYRot(), 0);
                    level().addFreshEntity(chick);
                }
            }
        }
        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }
}
