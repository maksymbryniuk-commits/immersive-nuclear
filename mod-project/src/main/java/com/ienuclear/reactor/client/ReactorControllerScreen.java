package com.ienuclear.reactor.client;

import com.ienuclear.reactor.menu.ReactorControllerMenu;
import com.ienuclear.reactor.network.AdjustControlRodPayload;
import com.ienuclear.reactor.network.ToggleReactorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Ретро-термінал старого ПК (CRT монітор 1980-х) для контролера ядерного реактора.
 * Паливо завантажується окремо у верхній Люк палива (Fuel Access Hatch).
 */
public class ReactorControllerScreen extends AbstractContainerScreen<ReactorControllerMenu> {
    private Button toggleButton;

    public ReactorControllerScreen(ReactorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 204;
        this.imageHeight = 130;
    }

    @Override
    protected void init() {
        super.init();
        this.toggleButton = this.addRenderableWidget(
                Button.builder(getButtonLabel(), btn ->
                                PacketDistributor.sendToServer(new ToggleReactorPayload(menu.getBlockPos())))
                        .bounds(leftPos + 72, topPos + 88, 84, 18)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.literal("-"), btn ->
                                PacketDistributor.sendToServer(new AdjustControlRodPayload(menu.getBlockPos(), -25)))
                        .bounds(leftPos + 72, topPos + 68, 18, 16)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.literal("+"), btn ->
                                PacketDistributor.sendToServer(new AdjustControlRodPayload(menu.getBlockPos(), 25)))
                        .bounds(leftPos + 138, topPos + 68, 18, 16)
                        .build()
        );
    }

    private Component getButtonLabel() {
        return menu.isActive()
                ? Component.translatable("gui.ienuclear.stop")
                : Component.translatable("gui.ienuclear.start");
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Бежевий ретро-корпус старого ПК / ЕПТ-монітора
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF524E44);
        gui.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFB8B2A2);
        gui.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF7B7668);
        gui.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF9C9686);

        boolean blink = (System.currentTimeMillis() / 500L) % 2L == 0L;
        int ledColor = menu.getTemperature() >= 1150
                ? (blink ? 0xFFFF2222 : 0xFF660000)
                : (menu.isActive() ? 0xFF22FF55 : 0xFFFFAA00);
        gui.fill(x + imageWidth - 12, y + 5, x + imageWidth - 8, y + 9, 0xFF222222);
        gui.fill(x + imageWidth - 11, y + 6, x + imageWidth - 9, y + 8, ledColor);

        // Заглиблений екран ЕПТ-монітора
        int crtX1 = x + 8, crtY1 = y + 16, crtX2 = x + imageWidth - 8, crtY2 = y + imageHeight - 10;
        gui.fill(crtX1 - 2, crtY1 - 2, crtX2 + 2, crtY2 + 2, 0xFF3E3B33);
        gui.fill(crtX1 - 1, crtY1 - 1, crtX2 + 1, crtY2 + 1, 0xFF020704);
        gui.fill(crtX1, crtY1, crtX2, crtY2, 0xFF06150B);

        gui.fill(crtX1 + 2, crtY1 + 2, crtX2 - 2, crtY1 + 3, 0xFF1A6334);
        gui.fill(crtX1 + 2, crtY2 - 3, crtX2 - 2, crtY2 - 2, 0xFF1A6334);

        int barY = y + 24;
        int barH = 74;

        // 1. Температура (0..1400°C)
        float tempRatio = Math.min(1.0f, Math.max(0.0f, menu.getTemperature() / 1400.0f));
        int tempColor = menu.getTemperature() >= 1150 ? 0xFFFF3333
                : (menu.getTemperature() >= 900 ? 0xFFFF9900
                : (menu.getTemperature() >= 550 ? 0xFFFFCC00 : 0xFF33FF66));
        drawSegmentedBar(gui, x + 14, barY, 12, barH, tempRatio, tempColor);

        // 2. Охолоджувач
        float coolRatio = Math.min(1.0f, (float) menu.getCoolantAmount() / menu.getCoolantCapacity());
        drawSegmentedBar(gui, x + 32, barY, 12, barH, coolRatio, 0xFF22EEFF);

        // 3. Вода для пари
        float waterRatio = Math.min(1.0f, (float) menu.getWaterAmount() / menu.getWaterCapacity());
        drawSegmentedBar(gui, x + 50, barY, 12, barH, waterRatio, 0xFF3B82F6);

        // 4. Гаряча пара на виході
        float steamRatio = Math.min(1.0f, (float) menu.getSteamAmount() / menu.getSteamCapacity());
        drawSegmentedBar(gui, x + 174, barY, 12, barH, steamRatio, 0xFFDCFCE7);

        // Горизонтальні лінії розгортки ЕПТ
        for (int sy = crtY1; sy < crtY2; sy += 2) {
            gui.fill(crtX1, sy, crtX2, sy + 1, 0x26000000);
        }
    }

    private void drawSegmentedBar(GuiGraphics gui, int bx, int by, int bw, int bh, float ratio, int color) {
        gui.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF153D22);
        gui.fill(bx, by, bx + bw, by + bh, 0xFF030C06);
        int fillPx = (int) (bh * ratio);
        for (int offset = 0; offset < fillPx; offset += 3) {
            int segTop = Math.max(by, by + bh - offset - 2);
            int segBot = by + bh - offset;
            gui.fill(bx + 1, segTop, bx + bw - 1, segBot, color);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, "NUC-PC TERMINAL", 8, 5, 0xFF2B2822, false);

        int temp = menu.getTemperature();
        int tempCol = temp >= 1150 ? 0xFFFF3333 : (temp >= 900 ? 0xFFFFAA00 : 0xFF33FF66);
        boolean cursor = (System.currentTimeMillis() / 450L) % 2L == 0L;

        String status = !menu.isMultiblockValid() ? "ERR: MULTIBLOCK"
                : (temp >= 1150 ? "!MELTDOWN CRIT!"
                : (!menu.isActive() ? "STANDBY"
                : (menu.getWaterAmount() <= 0 ? "NO H2O!"
                : (temp < 80 ? "HEATING..."
                : "STEAMING" + (cursor ? "_" : "")))));
        int statusCol = !menu.isMultiblockValid() || temp >= 1150 ? 0xFFFF3333
                : (!menu.isActive() ? 0xFFFFCC00
                : (menu.getWaterAmount() <= 0 ? 0xFFFF9900 : 0xFF33FF66));

        gui.drawString(this.font, status, 72, 23, statusCol, false);
        gui.drawString(this.font, "TEMP: " + temp + "C", 72, 34, tempCol, false);
        gui.drawString(this.font, "STM: +" + menu.getSteamPerTick() + "mB/t", 72, 45, 0xFF86EFAC, false);
        gui.drawString(this.font, "ROD:" + menu.getActiveCapsulesCount() + " COR:" + menu.getCoreCount() + " LP:" + menu.getCoolingLoopCount(), 72, 56, 0xFF22EEFF, false);
        gui.drawString(this.font, "RODS " + menu.getControlRodInsertion() + "%", 93, 72, menu.isEccsReady() ? 0xFF33FF66 : 0xFF86EFAC, false);

        gui.drawString(this.font, "T", 17, 102, 0xFF33FF66, false);
        gui.drawString(this.font, "C", 35, 102, 0xFF22EEFF, false);
        gui.drawString(this.font, "W", 53, 102, 0xFF3B82F6, false);
        gui.drawString(this.font, "S", 177, 102, 0xFFDCFCE7, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.toggleButton.setMessage(getButtonLabel());
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
        renderGaugeTooltips(gui, mouseX, mouseY);
    }

    private void renderGaugeTooltips(GuiGraphics gui, int mouseX, int mouseY) {
        int relX = mouseX - leftPos;
        int relY = mouseY - topPos;

        if (relX >= 14 && relX <= 26 && relY >= 24 && relY <= 98) {
            gui.renderTooltip(this.font,
                    Component.literal("Температура: " + menu.getTemperature() + " °C / 1400 °C"), mouseX, mouseY);
        } else if (relX >= 32 && relX <= 44 && relY >= 24 && relY <= 98) {
            gui.renderTooltip(this.font,
                    Component.literal("Охолоджувач: " + menu.getCoolantAmount() + " / " + menu.getCoolantCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 50 && relX <= 62 && relY >= 24 && relY <= 98) {
            gui.renderTooltip(this.font,
                    Component.literal("Вода для пари: " + menu.getWaterAmount() + " / " + menu.getWaterCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 174 && relX <= 186 && relY >= 24 && relY <= 98) {
            gui.renderTooltip(this.font,
                    Component.literal("Гаряча пара: " + menu.getSteamAmount() + " / " + menu.getSteamCapacity() + " mB"), mouseX, mouseY);
        }
    }
}
