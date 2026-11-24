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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * JEI категорія для Газової центрифуги 3×4×2 (збагачення UF6 та каскадне виробництво Тритію).
 */
public class GasCentrifugeCategory implements IRecipeCategory<GasCentrifugeCategory.CentrifugeRecipe> {
    public static final RecipeType<CentrifugeRecipe> RECIPE_TYPE =
            RecipeType.create(IENuclearMod.MODID, "gas_centrifuge", CentrifugeRecipe.class);

    public record CentrifugeRecipe(
            Fluid inputFluid, int inputAmount,
            ItemStack optionalItem,
            Fluid lightFluid, int lightAmount,
            Fluid heavyFluid, int heavyAmount,
            ItemStack itemOutput,
            String note
    ) {}

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public GasCentrifugeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.GAS_CENTRIFUGE.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override public RecipeType<CentrifugeRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.ienuclear.gas_centrifuge"); }
    @Override public int getWidth() { return 164; }
    @Override public int getHeight() { return 62; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 12)
                .setStandardSlotBackground()
                .addFluidStack(r.inputFluid(), r.inputAmount())
                .setFluidRenderer(Math.max(1000, r.inputAmount()), false, 16, 16);

        if (!r.optionalItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 34, 12)
                    .setStandardSlotBackground()
                    .addIngredient(VanillaTypes.ITEM_STACK, r.optionalItem());
        }

        if (r.lightFluid() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 4)
                    .setOutputSlotBackground()
                    .addFluidStack(r.lightFluid(), r.lightAmount())
                    .setFluidRenderer(1000, false, 16, 16);
        }
        if (r.heavyFluid() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 4)
                    .setOutputSlotBackground()
                    .addFluidStack(r.heavyFluid(), r.heavyAmount())
                    .setFluidRenderer(1000, false, 16, 16);
        }
        if (!r.itemOutput().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 12)
                    .setOutputSlotBackground()
                    .addIngredient(VanillaTypes.ITEM_STACK, r.itemOutput());
        }
    }

    @Override
    public void draw(CentrifugeRecipe r, IRecipeSlotsView view, GuiGraphics gui, double mouseX, double mouseY) {
        arrow.draw(gui, 68, 12);
        var font = Minecraft.getInstance().font;
        gui.drawString(font, r.note(), 6, 46, 0xFF33FF66, false);
        gui.drawString(font, ">= 48,000 RPM (80%) | 1024 FE/t", 6, 36, 0xFFE6A12E, false);
    }

    public static List<CentrifugeRecipe> getRecipes() {
        return List.of(
                new CentrifugeRecipe(
                        ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get(), 1000,
                        new ItemStack(ModItems.YELLOWCAKE_URANIUM.get(), 4),
                        ModFluids.ENRICHED_URANIUM_SOURCE.get(), 250,
                        ModFluids.DEPLETED_UF6_SOURCE.get(), 750,
                        ItemStack.EMPTY,
                        "UF6 -> U-235 (25%) + Depleted UF6 (75%)"
                ),
                new CentrifugeRecipe(
                        ModFluids.ENRICHED_URANIUM_SOURCE.get(), 1000,
                        ItemStack.EMPTY,
                        null, 0,
                        null, 0,
                        new ItemStack(ModItems.TRITIUM_CELL.get(), 1),
                        "Cascade Enrichment -> Tritium Cell"
                )
        );
    }
}
