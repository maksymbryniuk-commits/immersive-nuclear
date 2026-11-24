package com.ienuclear.reactor.registry;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.menu.FuelAccessHatchMenu;
import com.ienuclear.reactor.menu.PrimitiveReactorMenu;
import com.ienuclear.reactor.menu.ReactorControllerMenu;
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
}
