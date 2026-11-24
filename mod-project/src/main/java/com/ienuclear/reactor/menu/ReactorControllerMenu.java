package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.core.BlockPos;
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

public class ReactorControllerMenu extends AbstractContainerMenu {
    private final ReactorControllerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ReactorControllerMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv,
                (ReactorControllerBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(14));
    }

    public ReactorControllerMenu(int containerId, Inventory playerInv,
                                 ReactorControllerBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.REACTOR_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInv.player.level();
        this.data = data;

        // Слот 0: Паливна капсула (вхід)
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), ReactorControllerBlockEntity.FUEL_SLOT, 64, 24));
        // Слот 1: Відпрацьована капсула (вихід)
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), ReactorControllerBlockEntity.DEPLETED_SLOT, 64, 58) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 152));
        }
        addDataSlots(data);
    }

    public BlockPos getBlockPos() { return blockEntity.getBlockPos(); }
    public int getTemperature() { return data.get(0); }
    public int getCoolantAmount() { return data.get(1); }
    public int getCoolantCapacity() { return Math.max(1, data.get(2)); }
    public int getWaterAmount() { return data.get(3); }
    public int getWaterCapacity() { return Math.max(1, data.get(4)); }
    public int getSteamAmount() { return data.get(5); }
    public int getSteamCapacity() { return Math.max(1, data.get(6)); }
    public int getSteamPerTick() { return data.get(7); }
    public boolean isActive() { return data.get(8) == 1; }
    public boolean isMultiblockValid() { return data.get(9) == 1; }
    public int getFuelTicksRemaining() { return data.get(10); }
    public int getMaxFuelTicks() { return data.get(11); }
    public int getControlRodInsertion() { return data.get(12); }
    public int getCoreCount() { return data.get(13); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.REACTOR_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < 2) {
            if (!this.moveItemStackTo(source, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
