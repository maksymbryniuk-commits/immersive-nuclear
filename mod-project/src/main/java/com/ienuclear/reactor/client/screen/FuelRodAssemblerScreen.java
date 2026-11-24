package com.ienuclear.reactor.client.screen;

import com.ienuclear.reactor.menu.FuelRodAssemblerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Екран складального верстата ТВЕЛів.
 */
public class FuelRodAssemblerScreen extends AbstractContainerScreen<FuelRodAssemblerMenu> {
    public FuelRodAssemblerScreen(FuelRodAssemblerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1F2227);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2B2F36);

        int[] sx = {25, 51, 77, 133};
        for (int s : sx) {
            g.fill(x + s, y + 35, x + s + 18, y + 53, 0xFF141619);
        }

        int prog = menu.getData().get(0);
        g.fill(x + 100, y + 40, x + 128, y + 48, 0xFF141619);
        g.fill(x + 100, y + 40, x + 100 + (int) (28.0 * prog / 100.0), y + 48, 0xFF39FF14);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "FUEL ROD ASSEMBLER", 8, 6, 0xFFE6A12E, false);
        g.drawString(font, "FE: " + menu.getData().get(1), 8, 66, 0xFF39FF14, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
