package com.ienuclear.reactor.compat.jei;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

/**
 * JEI категорія замкнутого пароводяного та натрієвого термодинамічного циклу (Градирня, Теплообмінник, Турбіна, Реактор).
 */
public class ThermodynamicsCategory implements IRecipeCategory<ThermodynamicsCategory.FluidCycleRecipe> {
    public static final RecipeType<FluidCycleRecipe> RECIPE_TYPE =
            RecipeType.create(IENuclearMod.MODID, "thermodynamics", FluidCycleRecipe.class);

    public record FluidCycleRecipe(
            Fluid in1, int mb1,
            Fluid in2, int mb2,
            Fluid out1, int outMb1,
            String machineTitle,
            String detail
    ) {}

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public ThermodynamicsCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override public RecipeType<FluidCycleRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.ienuclear.thermodynamics"); }
    @Override public int getWidth() { return 168; }
    @Override public int getHeight() { return 58; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidCycleRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 12).setStandardSlotBackground()
                .addFluidStack(r.in1(), r.mb1())
                .setFluidRenderer(Math.max(1000, r.mb1()), false, 16, 16);

        if (r.in2() != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 34, 12).setStandardSlotBackground()
                    .addFluidStack(r.in2(), r.mb2())
                    .setFluidRenderer(Math.max(1000, r.mb2()), false, 16, 16);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 12).setOutputSlotBackground()
                .addFluidStack(r.out1(), r.outMb1())
                .setFluidRenderer(Math.max(1000, r.outMb1()), false, 16, 16);
    }

    @Override
    public void draw(FluidCycleRecipe r, IRecipeSlotsView view, GuiGraphics gui, double mouseX, double mouseY) {
        arrow.draw(gui, 72, 12);
        var font = Minecraft.getInstance().font;
        gui.drawString(font, r.machineTitle(), 6, 33, 0xFFE6A12E, false);
        gui.drawString(font, r.detail(), 6, 45, 0xFF33FF66, false);
    }

    public static List<FluidCycleRecipe> getRecipes() {
        return List.of(
                new FluidCycleRecipe(
                        ModFluids.HEAVY_COOLANT_SOURCE.get(), 500,
                        null, 0,
                        ModFluids.HOT_COOLANT_SOURCE.get(), 500,
                        "Reactor Controller (3x3..7x7)",
                        "Primary Cooling -> Hot Coolant output"
                ),
                new FluidCycleRecipe(
                        ModFluids.HOT_COOLANT_SOURCE.get(), 500,
                        ModFluids.DISTILLED_WATER_SOURCE.get(), 500,
                        ModFluids.HOT_STEAM_SOURCE.get(), 5000,
                        "Heat Exchanger (3x3x5)",
                        "Clean Secondary Steam Loop"
                ),
                new FluidCycleRecipe(
                        ModFluids.LIQUID_SODIUM_SOURCE.get(), 500,
                        ModFluids.DISTILLED_WATER_SOURCE.get(), 1000,
                        ModFluids.HOT_STEAM_SOURCE.get(), 10000,
                        "Heat Exchanger (Sodium)",
                        "2x Heat Transfer! Don't run dry!"
                ),
                new FluidCycleRecipe(
                        ModFluids.DISTILLED_WATER_SOURCE.get(), 1000,
                        null, 0,
                        ModFluids.HOT_STEAM_SOURCE.get(), 12500,
                        "Reactor Water Input Port",
                        "+25% Steam vs normal Water"
                ),
                new FluidCycleRecipe(
                        ModFluids.HOT_STEAM_SOURCE.get(), 4000,
                        null, 0,
                        ModFluids.EXHAUST_STEAM_SOURCE.get(), 400,
                        "Steam Turbine (3x3x5)",
                        "Up to 36,000 FE/t + Exhaust Steam"
                ),
                new FluidCycleRecipe(
                        ModFluids.EXHAUST_STEAM_SOURCE.get(), 1000,
                        null, 0,
                        ModFluids.DISTILLED_WATER_SOURCE.get(), 1000,
                        "Cooling Tower (7x10x7)",
                        "Closed Water Loop (1:1 Condense)"
                ),
                new FluidCycleRecipe(
                        Fluids.WATER, 1000,
                        null, 0,
                        ModFluids.HOT_STEAM_SOURCE.get(), 10000,
                        "Reactor Water Input Port",
                        "Standard Water Boiling (1 -> 10 mB)"
                )
        );
    }
}
