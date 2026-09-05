package com.animania.farm.vehicle;

import com.animania.common.config.LegacyConfig;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.function.Supplier;

public final class VehicleItem extends Item {
    private final Supplier<? extends EntityType<? extends FarmVehicleEntity>> vehicleType;

    public VehicleItem(Supplier<? extends EntityType<? extends FarmVehicleEntity>> vehicleType, Properties properties) {
        super(properties);
        this.vehicleType = vehicleType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (LegacyConfig.DISABLE_ROLLING_VEHICLES.get()) return InteractionResult.FAIL;
        if (!context.getLevel().isClientSide()) {
            FarmVehicleEntity vehicle = vehicleType.get().create(context.getLevel());
            if (vehicle == null) return InteractionResult.FAIL;
            vehicle.moveTo(context.getClickLocation().add(0.0D, 0.15D, 0.0D));
            vehicle.setYRot(context.getRotation() + 180.0F);
            if (!context.getLevel().noCollision(vehicle, vehicle.getBoundingBox())) return InteractionResult.FAIL;
            context.getLevel().addFreshEntity(vehicle);
            Player player = context.getPlayer();
            if (player == null || !player.isCreative()) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
