package com.ienuclear.reactor.client.screen;

import com.ienuclear.reactor.menu.ControlDeskMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Retro-PC CRT екран дистанційного пульта керування реактором (з кнопкою AZ-5).
 */
public class ControlDeskScreen extends AbstractContainerScreen<ControlDeskMenu> {
    public ControlDeskScreen(ControlDeskMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 220;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;
        addRenderableWidget(Button.builder(Component.literal("START / STOP"), b -> sendBtn(0))
                .bounds(x + 12, y + 106, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal("RODS -10%"), b -> sendBtn(1))
                .bounds(x + 114, y + 106, 46, 20).build());
        addRenderableWidget(Button.builder(Component.literal("RODS +10%"), b -> sendBtn(2))
                .bounds(x + 162, y + 106, 46, 20).build());
        addRenderableWidget(Button.builder(Component.literal("AZ-5 SCRAM"), b -> sendBtn(3))
                .bounds(x + 12, y + 132, 92, 22).build());
        addRenderableWidget(Button.builder(Component.literal("ECCS FLOOD"), b -> sendBtn(4))
                .bounds(x + 114, y + 132, 94, 22).build());
    }

    private void sendBtn(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1F2227);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2B2F36);
        // CRT монітор
        g.fill(x + 10, y + 18, x + imageWidth - 10, y + 98, 0xFF06160B);
        g.renderOutline(x + 10, y + 18, imageWidth - 20, 80, 0xFF39FF14);

        // Червона рамка навколо AZ-5
        g.renderOutline(x + 11, y + 131, 94, 24, 0xFFFF2A2A);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "REMOTE CONTROL DESK", 12, 6, 0xFFE6A12E, false);
        var d = menu.getData();
        boolean linked = d.get(0) == 1;
        if (!linked) {
            g.drawString(font, "LINK: NO SIGNAL", 18, 28, 0xFFFF4444, false);
            g.drawString(font, "Connect Control Cable to", 18, 46, 0xFF88CC88, false);
            g.drawString(font, "Remote Control Port.", 18, 58, 0xFF88CC88, false);
            return;
        }
        boolean active = d.get(1) == 1;
        int temp = d.get(2);
        int steam = d.get(3);
        int rods = d.get(4);
        int caps = d.get(5);

        g.drawString(font, "STATUS: " + (active ? "ACTIVE" : "STANDBY"), 18, 25, active ? 0xFF39FF14 : 0xFFFFAA00, false);
        g.drawString(font, "TEMP: " + temp + " C", 18, 39, temp >= 1000 ? 0xFFFF3333 : 0xFF39FF14, false);
        g.drawString(font, "STEAM: " + steam + " mB/t", 18, 53, 0xFF39FF14, false);
        g.drawString(font, "RODS: " + rods + "%  |  RODS IN: " + caps, 18, 67, 0xFF39FF14, false);
        g.drawString(font, "CABLE LINK: OK", 18, 81, 0xFF55FFAA, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
