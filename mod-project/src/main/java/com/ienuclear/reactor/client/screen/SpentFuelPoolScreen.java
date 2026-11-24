package com.ienuclear.reactor.client.screen;

import com.ienuclear.reactor.menu.SpentFuelPoolMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Ретро-ПК термінал мультиблокового охолоджувального басейну 3×3×3.
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
        // Ретро-ПК бежево-сталева рамка
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF4E4A40);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF8C8778);
        // CRT дисплей статусу
        g.fill(x + 7, y + 14, x + 169, y + 38, 0xFF06150B);
        g.renderOutline(x + 7, y + 14, 162, 24, 0xFF1A6334);
        // 9 водяних слотів
        for (int i = 0; i < 9; i++) {
            g.fill(x + 7 + i * 18, y + 41, x + 25 + i * 18, y + 59, 0xFF0A2438);
            g.renderOutline(x + 7 + i * 18, y + 41, 18, 18, 0xFF1E5F8A);
        }
        int prog = menu.getData().get(1);
        g.fill(x + 8, y + 65, x + 168, y + 73, 0xFF141619);
        g.fill(x + 8, y + 65, x + 8 + Math.min(160, (int) (160.0 * prog / 200.0)), y + 73, 0xFF39C5FF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "SPENT FUEL POOL 3x3x3", 8, 4, 0xFF2B2822, false);
        boolean valid = menu.getData().get(2) == 1;
        g.drawString(font, valid ? "BASIN 3x3x3: ONLINE" : "ERR: BUILD 3x3x3 + WATER", 11, 17, valid ? 0xFF33FF66 : 0xFFFF4444, false);
        g.drawString(font, "WATER: " + menu.getData().get(0) + " mB", 11, 27, 0xFF39C5FF, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
