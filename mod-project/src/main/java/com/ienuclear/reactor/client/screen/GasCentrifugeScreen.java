package com.ienuclear.reactor.client.screen;

import com.ienuclear.reactor.menu.GasCentrifugeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Екран газової центрифуги з індикатором інерції ротора (0–60 000 RPM).
 */
public class GasCentrifugeScreen extends AbstractContainerScreen<GasCentrifugeMenu> {
    public GasCentrifugeScreen(GasCentrifugeMenu menu, Inventory inv, Component title) {
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
        g.fill(x + 8, y + 16, x + imageWidth - 8, y + 48, 0xFF06160B);
        g.renderOutline(x + 8, y + 16, imageWidth - 16, 32, 0xFF39FF14);

        // Слоти
        g.fill(x + 25, y + 51, x + 43, y + 69, 0xFF141619);
        g.fill(x + 133, y + 51, x + 151, y + 69, 0xFF141619);

        // Полоска RPM
        int rpm = menu.getData().get(1);
        int fillW = (int) (108.0 * rpm / 60000.0);
        g.fill(x + 54, y + 56, x + 124, y + 64, 0xFF141619);
        g.fill(x + 54, y + 56, x + 54 + Math.min(70, (int) (70.0 * rpm / 60000.0)), y + 64,
                rpm >= 48000 ? 0xFF39FF14 : 0xFFFFAA00);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "GAS CENTRIFUGE", 8, 5, 0xFFE6A12E, false);
        var d = menu.getData();
        boolean valid = d.get(0) == 1;
        int rpm = d.get(1);
        int fe = d.get(2);
        int inMb = d.get(3);
        int outLight = d.get(4);
        int outHeavy = d.get(5);
        g.drawString(font, (valid ? "FRAME: OK" : "FRAME: INVALID") + " | RPM: " + rpm, 12, 20, valid ? 0xFF39FF14 : 0xFFFF4444, false);
        g.drawString(font, "UF6:" + inMb + " U235:" + outLight + " U238:" + outHeavy, 12, 32, 0xFF39FF14, false);
        g.drawString(font, "FE: " + fe, 54, 67, 0xFFCCCCCC, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
