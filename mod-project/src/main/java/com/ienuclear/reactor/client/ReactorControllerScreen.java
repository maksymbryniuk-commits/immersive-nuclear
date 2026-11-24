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
 */
public class ReactorControllerScreen extends AbstractContainerScreen<ReactorControllerMenu> {
    private Button toggleButton;

    public ReactorControllerScreen(ReactorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 176;
        this.inventoryLabelY = 83;
    }

    @Override
    protected void init() {
        super.init();
        this.toggleButton = this.addRenderableWidget(
                Button.builder(getButtonLabel(), btn ->
                                PacketDistributor.sendToServer(new ToggleReactorPayload(menu.getBlockPos())))
                        .bounds(leftPos + 86, topPos + 61, 58, 16)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.literal("-"), btn ->
                                PacketDistributor.sendToServer(new AdjustControlRodPayload(menu.getBlockPos(), -25)))
                        .bounds(leftPos + 86, topPos + 45, 14, 14)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.literal("+"), btn ->
                                PacketDistributor.sendToServer(new AdjustControlRodPayload(menu.getBlockPos(), 25)))
                        .bounds(leftPos + 130, topPos + 45, 14, 14)
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

        // 1. Бежевий ретро-корпус старого ПК / ЕПТ-монітора з 3D-фаскою
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF524E44);
        gui.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFB8B2A2); // верх/ліво світлий пластик
        gui.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF7B7668); // тінь
        gui.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF9C9686); // основний бежевий корпус

        // Індикатор живлення ПК (зелений/червоний світлодіод у верхньому правому куті корпусу)
        boolean blink = (System.currentTimeMillis() / 500L) % 2L == 0L;
        int ledColor = menu.getTemperature() >= 1150
                ? (blink ? 0xFFFF2222 : 0xFF660000)
                : (menu.isActive() ? 0xFF22FF55 : 0xFFFFAA00);
        gui.fill(x + imageWidth - 10, y + 5, x + imageWidth - 6, y + 9, 0xFF222222);
        gui.fill(x + imageWidth - 9, y + 6, x + imageWidth - 7, y + 8, ledColor);

        // 2. Заглиблений екран ЕПТ-монітора (верхня зона 6..80)
        int crtX1 = x + 6, crtY1 = y + 14, crtX2 = x + imageWidth - 6, crtY2 = y + 81;
        gui.fill(crtX1 - 1, crtY1 - 1, crtX2 + 1, crtY2 + 1, 0xFF3E3B33);
        gui.fill(crtX1, crtY1, crtX2, crtY2, 0xFF06150B); // темне фосфорне скло

        // Внутрішня рамка терміналу (фосфорна лінія)
        gui.fill(crtX1 + 1, crtY1 + 1, crtX2 - 1, crtY1 + 2, 0xFF195E31);
        gui.fill(crtX1 + 1, crtY2 - 2, crtX2 - 1, crtY2 - 1, 0xFF195E31);

        // Слоти паливних капсул (у стилі дисководу / CRT)
        drawCrtSlot(gui, x + 63, y + 23);
        drawCrtSlot(gui, x + 63, y + 57);

        // Індикатор вигоряння капсули
        gui.fill(x + 69, y + 42, x + 75, y + 55, 0xFF030B06);
        if (menu.getMaxFuelTicks() > 0 && menu.getFuelTicksRemaining() > 0) {
            int h = Math.max(1, (int) (13.0f * (1.0f - (float) menu.getFuelTicksRemaining() / menu.getMaxFuelTicks())));
            gui.fill(x + 69, y + 42, x + 75, y + 42 + h, 0xFF33FF66);
        }

        int barY = y + 20;
        int barH = 56;

        // 1. Температура (сегментована LED-шкала 0..1400°C)
        float tempRatio = Math.min(1.0f, Math.max(0.0f, menu.getTemperature() / 1400.0f));
        int tempColor = menu.getTemperature() >= 1150 ? 0xFFFF3333
                : (menu.getTemperature() >= 900 ? 0xFFFF9900
                : (menu.getTemperature() >= 550 ? 0xFFFFCC00 : 0xFF33FF66));
        drawSegmentedBar(gui, x + 10, barY, 10, barH, tempRatio, tempColor);

        // 2. Охолоджувач (фосфорний бірюзовий)
        float coolRatio = Math.min(1.0f, (float) menu.getCoolantAmount() / menu.getCoolantCapacity());
        drawSegmentedBar(gui, x + 26, barY, 10, barH, coolRatio, 0xFF22EEFF);

        // 3. Вода для пари (синій термінальний)
        float waterRatio = Math.min(1.0f, (float) menu.getWaterAmount() / menu.getWaterCapacity());
        drawSegmentedBar(gui, x + 42, barY, 10, barH, waterRatio, 0xFF3B82F6);

        // 4. Гаряча пара на виході (сріблясто-зелений пар)
        float steamRatio = Math.min(1.0f, (float) menu.getSteamAmount() / menu.getSteamCapacity());
        drawSegmentedBar(gui, x + 152, barY, 12, barH, steamRatio, 0xFFDCFCE7);

        // Горизонтальні лінії розгортки ЕПТ (CRT scanlines) поверх екрану
        for (int sy = crtY1; sy < crtY2; sy += 2) {
            gui.fill(crtX1, sy, crtX2, sy + 1, 0x26000000);
        }

        // Інвентар гравця (ретро-клавіатурний блок на бежевому корпусі ПК)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) drawRetroSlot(gui, x + 7 + col * 18, y + 93 + row * 18);
        }
        for (int col = 0; col < 9; col++) drawRetroSlot(gui, x + 7 + col * 18, y + 151);
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
        gui.drawString(this.font, "NUC-PC v2.2", 8, 4, 0xFF2B2822, false);
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF2B2822, false);

        int temp = menu.getTemperature();
        int tempCol = temp >= 1150 ? 0xFFFF3333 : (temp >= 900 ? 0xFFFFAA00 : 0xFF33FF66);
        gui.drawString(this.font, ">" + temp + "C x" + menu.getCoreCount(), 85, 18, tempCol, false);
        gui.drawString(this.font, "+" + menu.getSteamPerTick() + "mB", 85, 27, 0xFF86EFAC, false);

        boolean cursor = (System.currentTimeMillis() / 450L) % 2L == 0L;
        String status = !menu.isMultiblockValid() ? "ERR:MB"
                : (temp >= 1150 ? "!CRIT!"
                : (!menu.isActive() ? "HALT"
                : (menu.getWaterAmount() <= 0 ? "NO H2O!"
                : (temp < 80 ? "HEAT..."
                : "STEAM" + (cursor ? "_" : "")))));
        int statusCol = !menu.isMultiblockValid() || temp >= 1150 ? 0xFFFF3333
                : (!menu.isActive() ? 0xFFFFCC00
                : (menu.getWaterAmount() <= 0 ? 0xFFFF9900 : 0xFF33FF66));
        gui.drawString(this.font, status, 85, 36, statusCol, false);

        gui.drawString(this.font, menu.getControlRodInsertion() + "%", 104, 48, 0xFF33FF66, false);
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

        if (relX >= 10 && relX <= 20 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Температура: " + menu.getTemperature() + " °C / 1400 °C (Ядер: " + menu.getCoreCount() + ")"), mouseX, mouseY);
        } else if (relX >= 26 && relX <= 36 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Охолоджувач: " + menu.getCoolantAmount() + " / " + menu.getCoolantCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 42 && relX <= 52 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Вода для пари: " + menu.getWaterAmount() + " / " + menu.getWaterCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 152 && relX <= 164 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Гаряча пара: " + menu.getSteamAmount() + " / " + menu.getSteamCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 100 && relX <= 130 && relY >= 45 && relY <= 59) {
            gui.renderTooltip(this.font,
                    Component.literal("Керуючі стрижні: " + menu.getControlRodInsertion() + "% (Потужність: " + (100 - menu.getControlRodInsertion()) + "%)"),
                    mouseX, mouseY);
        }
    }
}
