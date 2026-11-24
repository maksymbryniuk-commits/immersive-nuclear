package com.ienuclear.reactor.client;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.client.screen.ControlDeskScreen;
import com.ienuclear.reactor.client.screen.FuelRodAssemblerScreen;
import com.ienuclear.reactor.client.screen.GasCentrifugeScreen;
import com.ienuclear.reactor.client.screen.HeatExchangerScreen;
import com.ienuclear.reactor.client.screen.SpentFuelPoolScreen;
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
        event.register(ModMenus.FUEL_ACCESS_HATCH_MENU.get(), FuelAccessHatchScreen::new);
        event.register(ModMenus.CONTROL_DESK_MENU.get(), ControlDeskScreen::new);
        event.register(ModMenus.GAS_CENTRIFUGE_MENU.get(), GasCentrifugeScreen::new);
        event.register(ModMenus.FUEL_ROD_ASSEMBLER_MENU.get(), FuelRodAssemblerScreen::new);
        event.register(ModMenus.SPENT_FUEL_POOL_MENU.get(), SpentFuelPoolScreen::new);
        event.register(ModMenus.HEAT_EXCHANGER_MENU.get(), HeatExchangerScreen::new);
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

        int[] tints = {0xFF4D6B3C, 0xFFF2C94C, 0xFFFF6B00, 0xFF70D6FF, 0xFFD0D8E0};
        net.neoforged.neoforge.fluids.FluidType[] types = {
                ModFluids.DEPLETED_UF6_TYPE.get(),
                ModFluids.LIQUID_SODIUM_TYPE.get(),
                ModFluids.HOT_COOLANT_TYPE.get(),
                ModFluids.DISTILLED_WATER_TYPE.get(),
                ModFluids.EXHAUST_STEAM_TYPE.get()
        };
        for (int i = 0; i < types.length; i++) {
            final int c = tints[i];
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override public ResourceLocation getStillTexture() { return WATER_STILL; }
                @Override public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
                @Override public int getTintColor() { return c; }
            }, types[i]);
        }
    }
}
