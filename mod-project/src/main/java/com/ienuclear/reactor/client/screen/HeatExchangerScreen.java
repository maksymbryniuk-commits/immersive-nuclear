package com.ienuclear.reactor.client.screen;

import com.ienuclear.reactor.menu.HeatExchangerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Екран теплообмінника (1-й та 2-й контури).
 */
public class HeatExchangerScreen extends AbstractContainerScreen<HeatExchangerMenu> {
    public HeatExchangerScreen(HeatExchangerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 190;
        this.imageHeight = 120;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1F2227);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2B2F36);
        g.fill(x + 8, y + 18, x + imageWidth - 8, y + imageHeight - 8, 0xFF06160B);
        g.renderOutline(x + 8, y + 18, imageWidth - 16, imageHeight - 26, 0xFF39FF14);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "HEAT EXCHANGER", 8, 6, 0xFFE6A12E, false);
        var d = menu.getData();
        boolean valid = d.get(0) == 1;
        boolean rupt = d.get(1) == 1;
        g.drawString(font, valid ? "STRUCTURE: OK" : "STRUCTURE: INCOMPLETE", 14, 24, valid ? 0xFF39FF14 : 0xFFFF4444, false);
        g.drawString(font, "TEMP: " + d.get(2) + " C" + (rupt ? " [RUPTURE!]" : ""), 14, 40, rupt ? 0xFFFF2222 : 0xFF39FF14, false);
        g.drawString(font, "PRIMARY: " + d.get(3) + " mB", 14, 56, 0xFFFFAA00, false);
        g.drawString(font, "SEC. WATER: " + d.get(4) + " mB", 14, 72, 0xFF39C5FF, false);
        g.drawString(font, "CLEAN STEAM: " + d.get(5) + " mB", 14, 88, 0xFFEEEEEE, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
