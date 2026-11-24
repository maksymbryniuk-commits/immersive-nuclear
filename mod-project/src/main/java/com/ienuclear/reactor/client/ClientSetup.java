package com.ienuclear.reactor.client;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = IENuclearMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NuclearManualIntegration::init);
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(NuclearManualIntegration::init);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.REACTOR_CONTROLLER_MENU.get(), ReactorControllerScreen::new);
        event.register(ModMenus.PRIMITIVE_REACTOR_MENU.get(), PrimitiveReactorScreen::new);
        event.register(ModMenus.STEAM_TURBINE_MENU.get(), SteamTurbineScreen::new);
    }

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFF00D8FF; }
        }, ModFluids.HEAVY_COOLANT_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFF84CC16; }
        }, ModFluids.URANIUM_HEXAFLUORIDE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFF10B981; }
        }, ModFluids.ENRICHED_URANIUM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFFE2E8F0; }
        }, ModFluids.HOT_STEAM_TYPE.get());
    }
}
