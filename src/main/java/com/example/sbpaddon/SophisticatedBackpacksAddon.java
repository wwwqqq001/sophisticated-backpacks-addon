package com.example.sbpaddon;

import com.example.sbpaddon.client.ClientHandler;
import com.example.sbpaddon.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("sbpaddon")
public class SophisticatedBackpacksAddon {
    public static final String MODID = "sbpaddon";

    public SophisticatedBackpacksAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.register(ClientHandler.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketHandler.register();
    }
}
