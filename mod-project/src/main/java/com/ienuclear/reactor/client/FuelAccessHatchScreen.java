package com.ienuclear.reactor.client;

import com.ienuclear.reactor.menu.FuelAccessHatchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Ретро-ПК інтерфейс (CRT) верхнього люку палива з 6×3 сіткою у стилі IndustrialCraft 2.
 */
public class FuelAccessHatchScreen extends AbstractContainerScreen<FuelAccessHatchMenu> {

    public FuelAccessHatchScreen(FuelAccessHatchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 176;
        this.inventoryLabelY = 83;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Бежевий ретро-корпус старого ПК
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF524E44);
        gui.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFB8B2A2);
        gui.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF7B7668);
        gui.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF9C9686);

        // ЕПТ-екран для сітки 6×3
        int crtX1 = x + 6, crtY1 = y + 14, crtX2 = x + imageWidth - 6, crtY2 = y + 81;
        gui.fill(crtX1 - 1, crtY1 - 1, crtX2 + 1, crtY2 + 1, 0xFF3E3B33);
        gui.fill(crtX1, crtY1, crtX2, crtY2, 0xFF06150B);

        // 18 слотів (6×3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                drawCrtSlot(gui, x + 34 + col * 18, y + 21 + row * 18);
            }
        }

        for (int sy = crtY1; sy < crtY2; sy += 2) {
            gui.fill(crtX1, sy, crtX2, sy + 1, 0x22000000);
        }

        // Інвентар гравця
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) drawRetroSlot(gui, x + 7 + col * 18, y + 93 + row * 18);
        }
        for (int col = 0; col < 9; col++) drawRetroSlot(gui, x + 7 + col * 18, y + 151);
    }

    private void drawCrtSlot(GuiGraphics gui, int sx, int sy) {
        gui.fill(sx, sy, sx + 18, sy + 18, 0xFF1E5C33);
        gui.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF081C0E);
    }

    private void drawRetroSlot(GuiGraphics gui, int sx, int sy) {
        gui.fill(sx, sy, sx + 18, sy + 18, 0xFF666156);
        gui.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFC5BFAE);
        gui.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF837E70);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, "IC2 FUEL BAY 6x3", 8, 4, 0xFF2B2822, false);
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF2B2822, false);

        gui.drawString(this.font, "ROD:" + menu.getActiveCapsules(), 10, 22, 0xFF33FF66, false);
        gui.drawString(this.font, "VNT:" + menu.getVentsCount(), 10, 34, 0xFF22EEFF, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
