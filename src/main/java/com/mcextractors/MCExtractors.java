package com.mcextractors;

import com.mcextractors.init.ModBlocks;
import com.mcextractors.init.ModBlockEntities;
import com.mcextractors.init.ModMenuTypes;
import com.mcextractors.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MCExtractors.MOD_ID)
public class MCExtractors {
    public static final String MOD_ID = "mcextractors";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MCExtractors() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Register mod lifecycle listeners
        modEventBus.addListener(this::commonSetup);

        // Register to the Forge event bus
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("MCExtractors mod initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MCExtractors common setup complete!");
    }
}
