package com.shieldstatuses;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(ShieldStatuses.MODID)
public class ShieldStatuses {

    public static final String MODID = "shieldstatuses";
    public static final Logger LOGGER = LogManager.getLogger("ShieldStatuses");

    public ShieldStatuses() {
        // client-only mod: do not require anything on the server
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ShieldStatuses::initClient);
    }

    private static void initClient() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ShieldConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ShieldStatuses::clientSetup);
        MinecraftForge.EVENT_BUS.register(ShieldTracker.class);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ShieldRendererHook::install);
    }
}
