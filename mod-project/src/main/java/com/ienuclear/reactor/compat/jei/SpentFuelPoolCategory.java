package com.ienuclear.reactor.compat.jei;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

/**
 * JEI категорія для мультиблокового Охолоджувального басейну 3×3×3 (Spent Fuel Pool).
 */
public class SpentFuelPoolCategory implements IRecipeCategory<SpentFuelPoolCategory.PoolRecipe> {
    public static final RecipeType<PoolRecipe> RECIPE_TYPE =
            RecipeType.create(IENuclearMod.MODID, "spent_fuel_pool", PoolRecipe.class);

    public record PoolRecipe(ItemStack input, Fluid coolant, int mb, ItemStack outIngot, ItemStack outRod, String label) {}

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public SpentFuelPoolCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.SPENT_FUEL_POOL.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(80);
    }

    @Override public RecipeType<PoolRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.ienuclear.spent_fuel_pool"); }
    @Override public int getWidth() { return 164; }
    @Override public int getHeight() { return 54; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PoolRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10).setStandardSlotBackground()
                .addIngredient(VanillaTypes.ITEM_STACK, r.input());
        builder.addSlot(RecipeIngredientRole.INPUT, 34, 10).setStandardSlotBackground()
                .addFluidStack(r.coolant(), r.mb())
                .setFluidRenderer(1000, false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 10).setOutputSlotBackground()
                .addIngredient(VanillaTypes.ITEM_STACK, r.outIngot());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 10).setOutputSlotBackground()
                .addIngredient(VanillaTypes.ITEM_STACK, r.outRod());
    }

    @Override
    public void draw(PoolRecipe r, IRecipeSlotsView view, GuiGraphics gui, double mouseX, double mouseY) {
        arrow.draw(gui, 66, 10);
        gui.drawString(Minecraft.getInstance().font, r.label(), 8, 36, 0xFF39C5FF, false);
    }

    public static List<PoolRecipe> getRecipes() {
        return List.of(
                new PoolRecipe(
                        new ItemStack(ModItems.DEPLETED_FUEL_ROD.get()),
                        Fluids.WATER, 1000,
                        new ItemStack(ModItems.DEPLETED_URANIUM_INGOT.get()),
                        new ItemStack(ModItems.EMPTY_FUEL_ROD.get()),
                        "3x3x3 Pool + Water (10s cooling)"
                ),
                new PoolRecipe(
                        new ItemStack(ModItems.DEPLETED_FUEL_ROD.get()),
                        ModFluids.DISTILLED_WATER_SOURCE.get(), 500,
                        new ItemStack(ModItems.DEPLETED_URANIUM_INGOT.get()),
                        new ItemStack(ModItems.EMPTY_FUEL_ROD.get()),
                        "3x3x3 Pool + Distilled Water (2x speed!)"
                )
        );
    }
}
