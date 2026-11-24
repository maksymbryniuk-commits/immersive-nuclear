package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.FuelAccessHatchBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FuelAccessHatchMenu extends AbstractContainerMenu {
    private final FuelAccessHatchBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public FuelAccessHatchMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv,
                (FuelAccessHatchBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(3));
    }

    public FuelAccessHatchMenu(int containerId, Inventory playerInv,
                               FuelAccessHatchBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.FUEL_ACCESS_HATCH_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInv.player.level();
        this.data = data;

        // 6x3 IC2-подібна сітка (18 слотів)
        for (int row = 0; row < FuelAccessHatchBlockEntity.ROWS; row++) {
            for (int col = 0; col < FuelAccessHatchBlockEntity.COLS; col++) {
                int slotIdx = row * FuelAccessHatchBlockEntity.COLS + col;
                this.addSlot(new SlotItemHandler(blockEntity.getInventory(), slotIdx, 35 + col * 18, 22 + row * 18));
            }
        }

        // Інвентар гравця
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 152));
        }

        addDataSlots(data);
    }

    public int getActiveCapsules() { return data.get(0); }
    public int getVentsCount() { return data.get(1); }
    public int getNetHeat() { return data.get(2); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.FUEL_ACCESS_HATCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        int hatchSlots = FuelAccessHatchBlockEntity.SLOTS;
        if (index < hatchSlots) {
            if (!this.moveItemStackTo(source, hatchSlots, hatchSlots + 36, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(source, 0, hatchSlots, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
