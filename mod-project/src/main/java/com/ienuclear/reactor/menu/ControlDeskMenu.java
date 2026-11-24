package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.ControlDeskBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Меню реакторного пульта керування.
 */
public class ControlDeskMenu extends AbstractContainerMenu {
    private final ControlDeskBlockEntity blockEntity;
    private final ContainerData data;

    public ControlDeskMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(6));
    }

    public ControlDeskMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.CONTROL_DESK_MENU.get(), id);
        this.blockEntity = (ControlDeskBlockEntity) entity;
        this.data = data;
        addDataSlots(data);
    }

    public ContainerData getData() { return data; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity != null) {
            blockEntity.handleButton(id);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.CONTROL_DESK.get());
    }
}
