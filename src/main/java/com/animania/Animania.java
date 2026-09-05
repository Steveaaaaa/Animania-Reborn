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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Animania.MOD_ID)
public final class Animania {
    public static final String MOD_ID = "animania";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Animania(IEventBus modBus, ModContainer container) {
        ModFluids.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModEntities.register(modBus);
        ModSounds.register(modBus);
        ModItems.register(modBus);
        ModVillagers.register(modBus);
        ModCreativeTabs.register(modBus);
        ModAttachments.register(modBus);
        ModWorldgen.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, AnimaniaConfig.SPEC, "animania-modern-server.toml");
        LegacyConfig.register(container);

        LOGGER.info("Loading Animania Reborn for NeoForge 1.21.1");
    }
}
