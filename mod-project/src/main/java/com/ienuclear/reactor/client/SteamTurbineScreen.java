package com.ienuclear.reactor.client;

import com.ienuclear.reactor.blockentity.SteamTurbineBlockEntity;
import com.ienuclear.reactor.menu.SteamTurbineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Ретро-монітор старого ПК (CRT термінал) для мультиблокової Парової турбіни 3×3×5:
 * Відображає Оберти (RPM), Статус, Вихід енергії (FE/t + буфер) та Гарячу пару.
 */
public class SteamTurbineScreen extends AbstractContainerScreen<SteamTurbineMenu> {

    public SteamTurbineScreen(SteamTurbineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 204;
        this.imageHeight = 122;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. Бежевий корпус старого ПК / CRT монітора
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF4F4B42);
        gui.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFBEB8A8);
        gui.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF7B7668);
        gui.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF9E9888);

        // Вентиляційні прорізи на корпусі монітора зверху справа
        for (int i = 0; i < 4; i++) {
            gui.fill(x + imageWidth - 34 + i * 4, y + 5, x + imageWidth - 32 + i * 4, y + 10, 0xFF5A554A);
        }
        // Світлодіод живлення
        int ledCol = menu.getStatusCode() == 3 ? 0xFF22FF55 : (menu.getStatusCode() == 0 ? 0xFFFF3333 : 0xFFFFAA00);
        gui.fill(x + imageWidth - 12, y + 5, x + imageWidth - 8, y + 9, 0xFF222222);
        gui.fill(x + imageWidth - 11, y + 6, x + imageWidth - 9, y + 8, ledCol);

        // 2. ЕПТ-екран (глибокий зелений фосфорний дисплей)
        int crtX1 = x + 8, crtY1 = y + 16, crtX2 = x + imageWidth - 8, crtY2 = y + imageHeight - 10;
        gui.fill(crtX1 - 2, crtY1 - 2, crtX2 + 2, crtY2 + 2, 0xFF3E3B33);
        gui.fill(crtX1 - 1, crtY1 - 1, crtX2 + 1, crtY2 + 1, 0xFF020704);
        gui.fill(crtX1, crtY1, crtX2, crtY2, 0xFF06150B);

        // Фосфорна рамка всередині CRT
        gui.fill(crtX1 + 2, crtY1 + 2, crtX2 - 2, crtY1 + 3, 0xFF1A6334);
        gui.fill(crtX1 + 2, crtY2 - 3, crtX2 - 2, crtY2 - 2, 0xFF1A6334);

        int barY = y + 24;
        int barH = 68;

        // 1. Тахометр обертів (RPM 0..3600)
        float rpmRatio = Math.min(1.0f, (float) menu.getRpm() / SteamTurbineBlockEntity.MAX_RPM);
        int rpmCol = menu.getRpm() >= 2400 ? 0xFF33FF66 : (menu.getRpm() >= 800 ? 0xFFFFCC00 : 0xFF22EEFF);
        drawSegmentedBar(gui, x + 16, barY, 12, barH, rpmRatio, rpmCol);

        // 2. Гаряча пара
        float steamRatio = Math.min(1.0f, (float) menu.getSteamStored() / menu.getSteamCapacity());
        drawSegmentedBar(gui, x + 36, barY, 12, barH, steamRatio, 0xFFDCFCE7);

        // 3. Буфер FE енергії (ретро-янтарний)
        float feRatio = Math.min(1.0f, (float) menu.getEnergyStored() / menu.getEnergyCapacity());
        drawSegmentedBar(gui, x + 174, barY, 12, barH, feRatio, 0xFFFFB000);

        // Горизонтальні лінії розгортки ЕПТ (CRT scanlines)
        for (int sy = crtY1; sy < crtY2; sy += 2) {
            gui.fill(crtX1, sy, crtX2, sy + 1, 0x28000000);
        }
    }

    private void drawSegmentedBar(GuiGraphics gui, int bx, int by, int bw, int bh, float ratio, int color) {
        gui.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF154727);
        gui.fill(bx, by, bx + bw, by + bh, 0xFF030B05);
        int fillPx = (int) (bh * ratio);
        for (int offset = 0; offset < fillPx; offset += 3) {
            int segTop = Math.max(by, by + bh - offset - 2);
            int segBot = by + bh - offset;
            gui.fill(bx + 1, segTop, bx + bw - 1, segBot, color);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, "TURBINE-OS 3x3x5", 8, 5, 0xFF2B2822, false);

        boolean cursor = (System.currentTimeMillis() / 450L) % 2L == 0L;
        String statusText = switch (menu.getStatusCode()) {
            case 3 -> "ONLINE" + (cursor ? "_" : "");
            case 2 -> "SPINUP" + (cursor ? "_" : "");
            case 1 -> "IDLE";
            default -> "ERR: NO 3x3x5";
        };
        int statusCol = switch (menu.getStatusCode()) {
            case 3 -> 0xFF33FF66;
            case 2 -> 0xFFFFCC00;
            case 1 -> 0xFF86EFAC;
            default -> 0xFFFF4444;
        };

        gui.drawString(this.font, "C:\\TURBINE>", 56, 22, 0xFF1F8A44, false);
        gui.drawString(this.font, "SYS: " + statusText, 56, 35, statusCol, false);
        gui.drawString(this.font, "RPM: " + menu.getRpm(), 56, 50, 0xFF22EEFF, false);
        gui.drawString(this.font, "OUT: +" + menu.getFePerTick() + " FE/t", 56, 65, 0xFFFFB000, false);
        gui.drawString(this.font, "STM: -" + menu.getSteamPerTick() + " mB/t", 56, 80, 0xFFBBF7D0, false);

        gui.drawString(this.font, "RPM", 13, 96, 0xFF33FF66, false);
        gui.drawString(this.font, "STM", 33, 96, 0xFF86EFAC, false);
        gui.drawString(this.font, "FE", 175, 96, 0xFFFFB000, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);

        int relX = mouseX - leftPos;
        int relY = mouseY - topPos;
        if (relX >= 16 && relX <= 28 && relY >= 24 && relY <= 92) {
            gui.renderTooltip(this.font,
                    Component.literal("Оберти ротора: " + menu.getRpm() + " / " + SteamTurbineBlockEntity.MAX_RPM + " RPM"),
                    mouseX, mouseY);
        } else if (relX >= 36 && relX <= 48 && relY >= 24 && relY <= 92) {
            gui.renderTooltip(this.font,
                    Component.literal("Гаряча пара: " + menu.getSteamStored() + " / " + menu.getSteamCapacity() + " mB"),
                    mouseX, mouseY);
        } else if (relX >= 174 && relX <= 186 && relY >= 24 && relY <= 92) {
            gui.renderTooltip(this.font,
                    Component.literal("Накопичена енергія: " + menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"),
                    mouseX, mouseY);
        }
    }
}
