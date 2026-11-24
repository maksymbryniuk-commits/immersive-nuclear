package com.ienuclear.reactor.client;

import com.ienuclear.reactor.menu.PrimitiveReactorMenu;
import com.ienuclear.reactor.network.ToggleReactorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Ретро-термінал старого ПК для примітивного 1x1 ядерного реактора.
 */
public class PrimitiveReactorScreen extends AbstractContainerScreen<PrimitiveReactorMenu> {
    private Button toggleButton;

    public PrimitiveReactorScreen(PrimitiveReactorMenu menu, Inventory playerInventory, Component title) {
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
                        .bounds(leftPos + 86, topPos + 58, 58, 18)
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

        // Бежевий ретро-корпус старого ПК
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF524E44);
        gui.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFB8B2A2);
        gui.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF7B7668);
        gui.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF9C9686);

        int ledCol = menu.isActive() ? 0xFF22FF55 : 0xFFFFAA00;
        gui.fill(x + imageWidth - 10, y + 5, x + imageWidth - 6, y + 9, 0xFF222222);
        gui.fill(x + imageWidth - 9, y + 6, x + imageWidth - 7, y + 8, ledCol);

        // ЕПТ-екран
        int crtX1 = x + 6, crtY1 = y + 14, crtX2 = x + imageWidth - 6, crtY2 = y + 81;
        gui.fill(crtX1 - 1, crtY1 - 1, crtX2 + 1, crtY2 + 1, 0xFF3E3B33);
        gui.fill(crtX1, crtY1, crtX2, crtY2, 0xFF06150B);

        drawCrtSlot(gui, x + 63, y + 23);
        drawCrtSlot(gui, x + 63, y + 57);

        gui.fill(x + 69, y + 42, x + 75, y + 55, 0xFF030B06);
        if (menu.getMaxFuelTicks() > 0 && menu.getFuelTicksRemaining() > 0) {
            int h = Math.max(1, (int) (13.0f * (1.0f - (float) menu.getFuelTicksRemaining() / menu.getMaxFuelTicks())));
            gui.fill(x + 69, y + 42, x + 75, y + 42 + h, 0xFF33FF66);
        }

        int barY = y + 20;
        int barH = 56;

        float tempRatio = Math.min(1.0f, menu.getTemperature() / 1200.0f);
        int tempCol = menu.getTemperature() >= 950 ? 0xFFFF3333 : (menu.getTemperature() >= 550 ? 0xFFFFCC00 : 0xFF33FF66);
        drawSegmentedBar(gui, x + 12, barY, 12, barH, tempRatio, tempCol);

        float coolRatio = Math.min(1.0f, (float) menu.getCoolantAmount() / menu.getCoolantCapacity());
        drawSegmentedBar(gui, x + 34, barY, 14, barH, coolRatio, 0xFF22EEFF);

        float feRatio = Math.min(1.0f, (float) menu.getEnergyStored() / menu.getMaxEnergyStored());
        drawSegmentedBar(gui, x + 152, barY, 12, barH, feRatio, 0xFFFFB000);

        for (int sy = crtY1; sy < crtY2; sy += 2) {
            gui.fill(crtX1, sy, crtX2, sy + 1, 0x26000000);
        }

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
        gui.drawString(this.font, "MINI-NUC PC 1x1", 8, 4, 0xFF2B2822, false);
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF2B2822, false);
        gui.drawString(this.font, ">" + menu.getTemperature() + " C", 86, 20, 0xFF33FF66, false);
        gui.drawString(this.font, "+" + menu.getFePerTick() + " FE", 86, 31, 0xFFFFB000, false);
        gui.drawString(this.font, "OUT: TOP", 86, 43, 0xFF22EEFF, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.toggleButton.setMessage(getButtonLabel());
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
        int relX = mouseX - leftPos;
        int relY = mouseY - topPos;
        if (relX >= 12 && relX <= 24 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font, Component.literal("Температура: " + menu.getTemperature() + " °C / 1200 °C"), mouseX, mouseY);
        } else if (relX >= 34 && relX <= 48 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font, Component.literal("Охолоджувач (ПКМ відром): " + menu.getCoolantAmount() + " / " + menu.getCoolantCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 152 && relX <= 164 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font, Component.literal("Енергія (вихід ЗВЕРХУ): " + menu.getEnergyStored() + " / " + menu.getMaxEnergyStored() + " FE"), mouseX, mouseY);
        }
    }
}
