package com.ienuclear.reactor.compat.jei;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.registry.ModBlocks;
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
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * JEI категорія для Складального верстата ТВЕЛів (Fuel Rod Assembler).
 */
public class FuelRodAssemblerCategory implements IRecipeCategory<FuelRodAssemblerCategory.AssemblerRecipe> {
    public static final RecipeType<AssemblerRecipe> RECIPE_TYPE =
            RecipeType.create(IENuclearMod.MODID, "fuel_rod_assembler", AssemblerRecipe.class);

    public record AssemblerRecipe(ItemStack in0, ItemStack in1, ItemStack in2, ItemStack out, String stats) {}

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public FuelRodAssemblerCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.FUEL_ROD_ASSEMBLER.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(50);
    }

    @Override public RecipeType<AssemblerRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.ienuclear.fuel_rod_assembler"); }
    @Override public int getWidth() { return 164; }
    @Override public int getHeight() { return 52; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblerRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10).setStandardSlotBackground()
                .addIngredient(VanillaTypes.ITEM_STACK, r.in0());
        builder.addSlot(RecipeIngredientRole.INPUT, 34, 10).setStandardSlotBackground()
                .addIngredient(VanillaTypes.ITEM_STACK, r.in1());
        if (!r.in2().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 58, 10).setStandardSlotBackground()
                    .addIngredient(VanillaTypes.ITEM_STACK, r.in2());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 10).setOutputSlotBackground()
                .addIngredient(VanillaTypes.ITEM_STACK, r.out());
    }

    @Override
    public void draw(AssemblerRecipe r, IRecipeSlotsView view, GuiGraphics gui, double mouseX, double mouseY) {
        arrow.draw(gui, 90, 10);
        gui.drawString(Minecraft.getInstance().font, r.stats(), 8, 36, 0xFF33FF66, false);
    }

    public static List<AssemblerRecipe> getRecipes() {
        return List.of(
                new AssemblerRecipe(
                        new ItemStack(ModItems.EMPTY_FUEL_ROD.get()),
                        new ItemStack(ModItems.YELLOWCAKE_URANIUM.get()),
                        ItemStack.EMPTY,
                        new ItemStack(ModItems.NUCLEAR_FUEL_ROD.get()),
                        "1x Power | 4 Heat | 2400 dur | 60 FE/t"
                ),
                new AssemblerRecipe(
                        new ItemStack(ModItems.EMPTY_FUEL_ROD.get()),
                        new ItemStack(ModItems.ENRICHED_URANIUM_PELLET.get()),
                        ItemStack.EMPTY,
                        new ItemStack(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get()),
                        "2x Power | 7 Heat | 4800 dur | 60 FE/t"
                ),
                new AssemblerRecipe(
                        new ItemStack(ModItems.EMPTY_FUEL_ROD.get()),
                        new ItemStack(ModItems.PLUTONIUM_DUST.get()),
                        new ItemStack(ModItems.YELLOWCAKE_URANIUM.get()),
                        new ItemStack(ModItems.MOX_FUEL_CAPSULE.get()),
                        "3x Power | 11 Heat | 3600 dur | 60 FE/t"
                ),
                new AssemblerRecipe(
                        new ItemStack(ModItems.EMPTY_FUEL_ROD.get()),
                        new ItemStack(ModItems.YELLOWCAKE_URANIUM.get()),
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(ModItems.THORIUM_FUEL_CAPSULE.get()),
                        "1.2x Power | 3 Heat (Safe) | 5400 dur"
                )
        );
    }
}
