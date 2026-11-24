package com.ienuclear.reactor;

import com.ienuclear.reactor.multiblock.NuclearReactor7x7Multiblock;
import com.ienuclear.reactor.multiblock.NuclearReactorMultiblock;
import com.ienuclear.reactor.multiblock.SteamTurbineMultiblock;
import com.ienuclear.reactor.network.AdjustControlRodPayload;
import com.ienuclear.reactor.network.ToggleReactorPayload;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(IENuclearMod.MODID)
public class IENuclearMod {
    public static final String MODID = "ienuclear";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NUCLEAR_TAB = CREATIVE_TABS.register(
            "nuclear_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ienuclear"))
                    .icon(() -> new ItemStack(ModItems.REACTOR_CONTROLLER_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.REACTOR_CONTROLLER_ITEM.get());
                        output.accept(ModItems.REACTOR_CASING_ITEM.get());
                        output.accept(ModItems.REACTOR_CORE_ROD_ITEM.get());
                        output.accept(ModItems.REACTOR_GLASS_ITEM.get());
                        output.accept(ModItems.COOLANT_INPUT_PORT_ITEM.get());
                        output.accept(ModItems.WATER_INPUT_PORT_ITEM.get());
                        output.accept(ModItems.STEAM_OUTPUT_PORT_ITEM.get());
                        output.accept(ModItems.FUEL_ACCESS_HATCH_ITEM.get());
                        output.accept(ModItems.STEAM_TURBINE_ITEM.get());
                        output.accept(ModItems.TURBINE_CASING_ITEM.get());
                        output.accept(ModItems.TURBINE_ROTOR_ITEM.get());
                        output.accept(ModItems.STEAM_INPUT_PORT_ITEM.get());
                        output.accept(ModItems.PRIMITIVE_REACTOR_ITEM.get());
                        output.accept(ModItems.ENERGY_OUTPUT_PORT_ITEM.get());
                        output.accept(ModItems.YELLOWCAKE_URANIUM.get());
                        output.accept(ModItems.ENRICHED_URANIUM_PELLET.get());
                        output.accept(ModItems.EMPTY_FUEL_ROD.get());
                        output.accept(ModItems.NUCLEAR_FUEL_ROD.get());
                        output.accept(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get());
                        output.accept(ModItems.MOX_FUEL_CAPSULE.get());
                        output.accept(ModItems.HEAT_VENT.get());
                        output.accept(ModItems.REACTOR_PLATING.get());
                        output.accept(ModItems.HEAT_EXCHANGER.get());
                        output.accept(ModItems.NEUTRON_REFLECTOR.get());
                        output.accept(ModItems.DEPLETED_FUEL_ROD.get());
                        output.accept(ModItems.PLUTONIUM_DUST.get());
                        output.accept(ModItems.GEIGER_COUNTER.get());
                        output.accept(ModItems.IODINE_TABLET.get());
                        output.accept(ModItems.MOLTEN_CORIUM_ITEM.get());
                        output.accept(ModItems.COOLANT_BUCKET.get());
                        output.accept(ModItems.URANIUM_HEXAFLUORIDE_BUCKET.get());
                        output.accept(ModItems.ENRICHED_URANIUM_BUCKET.get());
                        output.accept(ModItems.HOT_STEAM_BUCKET.get());
                    })
                    .build()
    );

    public IENuclearMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerPayloads);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NuclearReactorMultiblock.register();
            NuclearReactor7x7Multiblock.register();
            SteamTurbineMultiblock.register();
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Порт охолоджувача: дозволяє IE Fluid Pipes та іншим трубам подавати охолоджувач
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.COOLANT_INPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getFluidTank()
        );

        // Порт виводу енергії (для сумісності)
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ENERGY_OUTPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );

        // Порт входу води для генерації пари
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.WATER_INPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getFluidTank()
        );

        // Порт виходу гарячої пари
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.STEAM_OUTPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getExternalHandler()
        );

        // Порт входу гарячої пари для мультиблокової Парової турбіни 3x3x5
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.STEAM_INPUT_PORT.get(),
                (blockEntity, side) -> blockEntity.getSteamTank()
        );

        // Примітивний 1x1 реактор: охолоджувач з боків/знизу, ВИХІД ЕНЕРГІЇ ВИКЛЮЧНО ЗВЕРХУ (Direction.UP)
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.PRIMITIVE_REACTOR.get(),
                (blockEntity, side) -> side != Direction.UP ? blockEntity.getCoolantTank() : null
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.PRIMITIVE_REACTOR.get(),
                (blockEntity, side) -> (side == Direction.UP || side == null) ? blockEntity.getEnergyStorage() : null
        );
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ToggleReactorPayload.TYPE,
                ToggleReactorPayload.STREAM_CODEC,
                ToggleReactorPayload::handleServer
        );
        registrar.playToServer(
                AdjustControlRodPayload.TYPE,
                AdjustControlRodPayload.STREAM_CODEC,
                AdjustControlRodPayload::handleServer
        );
    }
}
