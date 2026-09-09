package com.animania;

import com.animania.common.config.AnimaniaConfig;
import com.animania.common.config.LegacyConfig;
import com.animania.common.registry.ModAttachments;
import com.animania.common.registry.ModBlocks;
import com.animania.common.registry.ModCreativeTabs;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModFluids;
import com.animania.common.registry.ModBlockEntities;
import com.animania.common.registry.ModVillagers;
import com.animania.common.registry.ModSounds;
import com.animania.common.registry.ModWorldgen;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Animania.MOD_ID)
public final class Animania {
    public static final String MOD_ID = "animania";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Animania() {
        IEventBus modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        var container = net.minecraftforge.fml.ModLoadingContext.get();
        ModFluids.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModEntities.register(modBus);
        ModSounds.register(modBus);
        ModItems.register(modBus);
        com.animania.common.registry.ModMealEffects.register(modBus);
        ModVillagers.register(modBus);
        ModCreativeTabs.register(modBus);
        ModAttachments.register(modBus);
        ModWorldgen.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, AnimaniaConfig.SPEC, "animania-modern-server.toml");
        LegacyConfig.register(container);

        LOGGER.info("Loading Animania Reborn for Forge 1.20.1");
    }
}
