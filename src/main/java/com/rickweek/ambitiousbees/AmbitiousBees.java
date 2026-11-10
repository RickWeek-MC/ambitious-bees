package com.rickweek.ambitiousbees;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import org.slf4j.Logger;

@Mod(AmbitiousBees.MOD_ID)
public class AmbitiousBees {
    public static final String MOD_ID = "ambitious_bees";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AmbitiousBees(IEventBus modEventBus, ModContainer modContainer) {
        ModRegistries.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, AmbitiousBeesConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ambitious Bees: bee a good worker!");
    }
}
