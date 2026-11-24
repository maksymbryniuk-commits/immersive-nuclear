package com.ienuclear.reactor.compat.jei;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.client.screen.FuelRodAssemblerScreen;
import com.ienuclear.reactor.client.screen.GasCentrifugeScreen;
import com.ienuclear.reactor.client.screen.HeatExchangerScreen;
import com.ienuclear.reactor.client.screen.SpentFuelPoolScreen;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Повна опціональна інтеграція Immersive Nuclear у Just Enough Items (JEI).
 */
@JeiPlugin
public class IENuclearJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GasCentrifugeCategory(guiHelper),
                new FuelRodAssemblerCategory(guiHelper),
                new SpentFuelPoolCategory(guiHelper),
                new ThermodynamicsCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GasCentrifugeCategory.RECIPE_TYPE, GasCentrifugeCategory.getRecipes());
        registration.addRecipes(FuelRodAssemblerCategory.RECIPE_TYPE, FuelRodAssemblerCategory.getRecipes());
        registration.addRecipes(SpentFuelPoolCategory.RECIPE_TYPE, SpentFuelPoolCategory.getRecipes());
        registration.addRecipes(ThermodynamicsCategory.RECIPE_TYPE, ThermodynamicsCategory.getRecipes());

        // Довідкова інформація (JEI Info tab) про реактори, безпеку та мультиблоки
        registration.addIngredientInfo(ModBlocks.REACTOR_CONTROLLER.get(),
                Component.translatable("jei.ienuclear.info.reactor_controller"));
        registration.addIngredientInfo(ModBlocks.COOLING_LOOP.get(),
                Component.translatable("jei.ienuclear.info.cooling_loop"));
        registration.addIngredientInfo(ModBlocks.CONTROL_DESK.get(),
                Component.translatable("jei.ienuclear.info.control_desk"));
        registration.addIngredientInfo(ModBlocks.REMOTE_CONTROL_PORT.get(),
                Component.translatable("jei.ienuclear.info.control_desk"));
        registration.addIngredientInfo(ModBlocks.ECCS_UNIT.get(),
                Component.translatable("jei.ienuclear.info.eccs"));
        registration.addIngredientInfo(ModBlocks.NUCLEAR_WASTE_CASK.get(),
                Component.translatable("jei.ienuclear.info.waste_cask"));
        registration.addIngredientInfo(ModBlocks.RTG_GENERATOR.get(),
                Component.translatable("jei.ienuclear.info.rtg"));
        registration.addIngredientInfo(ModItems.HAZMAT_CHESTPLATE.get(),
                Component.translatable("jei.ienuclear.info.hazmat"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.GAS_CENTRIFUGE.get(), GasCentrifugeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.FUEL_ROD_ASSEMBLER.get(), FuelRodAssemblerCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.SPENT_FUEL_POOL.get(), SpentFuelPoolCategory.RECIPE_TYPE);

        registration.addRecipeCatalyst(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.COOLING_TOWER.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.STEAM_TURBINE.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.REACTOR_CONTROLLER.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.PRIMITIVE_REACTOR.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.COOLANT_INPUT_PORT.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.WATER_INPUT_PORT.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.STEAM_OUTPUT_PORT.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.STEAM_INPUT_PORT.get(), ThermodynamicsCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(ModBlocks.FUEL_ACCESS_HATCH.get(), FuelRodAssemblerCategory.RECIPE_TYPE, ThermodynamicsCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GasCentrifugeScreen.class, 52, 54, 74, 14, GasCentrifugeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(FuelRodAssemblerScreen.class, 98, 36, 32, 14, FuelRodAssemblerCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(SpentFuelPoolScreen.class, 8, 63, 160, 12, SpentFuelPoolCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(HeatExchangerScreen.class, 10, 20, 170, 88, ThermodynamicsCategory.RECIPE_TYPE);
    }
}
