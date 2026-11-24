package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.FuelRodAssemblerBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Меню складального верстата ТВЕЛів.
 */
public class FuelRodAssemblerMenu extends AbstractContainerMenu {
    private final FuelRodAssemblerBlockEntity blockEntity;
    private final ContainerData data;

    public FuelRodAssemblerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(2));
    }

    public FuelRodAssemblerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.FUEL_ROD_ASSEMBLER_MENU.get(), id);
        this.blockEntity = (FuelRodAssemblerBlockEntity) entity;
        this.data = data;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 26, 36));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 52, 36));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), 2, 78, 36));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), 3, 134, 36));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
            }
        }
        for (int c = 0; c < 9; c++) {
            addSlot(new Slot(inv, c, 8 + c * 18, 142));
        }
        addDataSlots(data);
    }

    public ContainerData getData() { return data; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, 40, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 3, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.FUEL_ROD_ASSEMBLER.get());
    }
}
