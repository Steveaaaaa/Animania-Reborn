package com.animania.common.event;
import com.animania.Animania;
import com.animania.common.config.LegacyConfig;
import com.animania.common.world.block.entity.TroughBlockEntity;
import com.animania.catsdogs.block.entity.PetBowlBlockEntity;
import com.animania.extra.world.block.entity.HamsterWheelBlockEntity;
import com.animania.farm.world.block.entity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import java.util.*;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class ModCapabilities {
    @SubscribeEvent public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity block = event.getObject();
        if (!(block instanceof HamsterWheelBlockEntity || block instanceof HiveBlockEntity
                || block instanceof NestBlockEntity || block instanceof CheeseMoldBlockEntity
                || block instanceof TroughBlockEntity || block instanceof PetBowlBlockEntity)) return;
        Provider provider = new Provider(block);
        event.addCapability(new ResourceLocation(Animania.MOD_ID, "automation"), provider);
        event.addListener(provider::invalidate);
    }
    private static final class Provider implements ICapabilityProvider {
        private final BlockEntity block;
        private final Map<Object, LazyOptional<?>> handlers = new IdentityHashMap<>();
        Provider(BlockEntity block) { this.block = block; }
        @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            Object handler = null;
            if (cap == ForgeCapabilities.ENERGY && block instanceof HamsterWheelBlockEntity wheel) handler = wheel.energy();
            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                if (block instanceof HamsterWheelBlockEntity wheel) handler = wheel.items();
                else if (block instanceof NestBlockEntity nest) handler = nest.items();
                else if (block instanceof CheeseMoldBlockEntity mold) handler = mold.items();
                else if (LegacyConfig.ALLOW_TROUGH_AUTOMATION.get()) {
                    if (block instanceof TroughBlockEntity trough && trough.water() == 0 && trough.slop() == 0) handler = trough.automationItems();
                    else if (block instanceof PetBowlBlockEntity bowl && bowl.water() == 0) handler = bowl.automationItems();
                }
            }
            if (cap == ForgeCapabilities.FLUID_HANDLER) {
                if (block instanceof HiveBlockEntity hive) handler = hive.tank();
                else if (block instanceof CheeseMoldBlockEntity mold && !mold.isReady()) handler = mold.fluids();
                else if (LegacyConfig.ALLOW_TROUGH_AUTOMATION.get()) {
                    if (block instanceof TroughBlockEntity trough && trough.feed().isEmpty()) handler = trough.automationFluids();
                    else if (block instanceof PetBowlBlockEntity bowl && bowl.food().isEmpty()) handler = bowl.automationFluids();
                }
            }
            if (handler == null) return LazyOptional.empty();
            return handlers.computeIfAbsent(handler, value -> LazyOptional.of(() -> value)).cast();
        }
        void invalidate() { handlers.values().forEach(LazyOptional::invalidate); handlers.clear(); }
    }
}
