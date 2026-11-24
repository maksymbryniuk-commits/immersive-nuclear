package com.ienuclear.reactor.client.screen;

import com.ienuclear.reactor.menu.SpentFuelPoolMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Екран охолоджувального басейну.
 */
public class SpentFuelPoolScreen extends AbstractContainerScreen<SpentFuelPoolMenu> {
    public SpentFuelPoolScreen(SpentFuelPoolMenu menu, Inventory inv, Component title) {
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
        for (int i = 0; i < 9; i++) {
            g.fill(x + 7 + i * 18, y + 35, x + 25 + i * 18, y + 53, 0xFF0A2438);
        }
        int prog = menu.getData().get(1);
        g.fill(x + 8, y + 60, x + 168, y + 66, 0xFF141619);
        g.fill(x + 8, y + 60, x + 8 + (int) (160.0 * prog / 200.0), y + 66, 0xFF39C5FF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "SPENT FUEL POOL", 8, 6, 0xFFE6A12E, false);
        g.drawString(font, "WATER: " + menu.getData().get(0) + " mB", 8, 20, 0xFF39C5FF, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
