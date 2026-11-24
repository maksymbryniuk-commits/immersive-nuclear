package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.menu.ControlDeskMenu;
import com.ienuclear.reactor.menu.FuelAccessHatchMenu;
import com.ienuclear.reactor.menu.FuelRodAssemblerMenu;
import com.ienuclear.reactor.menu.GasCentrifugeMenu;
import com.ienuclear.reactor.menu.HeatExchangerMenu;
import com.ienuclear.reactor.menu.PrimitiveReactorMenu;
import com.ienuclear.reactor.menu.ReactorControllerMenu;
import com.ienuclear.reactor.menu.SpentFuelPoolMenu;
import com.ienuclear.reactor.menu.SteamTurbineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IENuclearMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorControllerMenu>> REACTOR_CONTROLLER_MENU =
            MENUS.register("reactor_controller_menu", () ->
                    IMenuTypeExtension.create(ReactorControllerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PrimitiveReactorMenu>> PRIMITIVE_REACTOR_MENU =
            MENUS.register("primitive_reactor_menu", () ->
                    IMenuTypeExtension.create(PrimitiveReactorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SteamTurbineMenu>> STEAM_TURBINE_MENU =
            MENUS.register("steam_turbine_menu", () ->
                    IMenuTypeExtension.create(SteamTurbineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FuelAccessHatchMenu>> FUEL_ACCESS_HATCH_MENU =
            MENUS.register("fuel_access_hatch_menu", () ->
                    IMenuTypeExtension.create(FuelAccessHatchMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ControlDeskMenu>> CONTROL_DESK_MENU =
            MENUS.register("control_desk_menu", () ->
                    IMenuTypeExtension.create(ControlDeskMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GasCentrifugeMenu>> GAS_CENTRIFUGE_MENU =
            MENUS.register("gas_centrifuge_menu", () ->
                    IMenuTypeExtension.create(GasCentrifugeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FuelRodAssemblerMenu>> FUEL_ROD_ASSEMBLER_MENU =
            MENUS.register("fuel_rod_assembler_menu", () ->
                    IMenuTypeExtension.create(FuelRodAssemblerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SpentFuelPoolMenu>> SPENT_FUEL_POOL_MENU =
            MENUS.register("spent_fuel_pool_menu", () ->
                    IMenuTypeExtension.create(SpentFuelPoolMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_MENU =
            MENUS.register("heat_exchanger_menu", () ->
                    IMenuTypeExtension.create(HeatExchangerMenu::new));
}
