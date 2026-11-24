package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.HeatExchangerBlockEntity;
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
 * Меню теплообмінника.
 */
public class HeatExchangerMenu extends AbstractContainerMenu {
    private final HeatExchangerBlockEntity blockEntity;
    private final ContainerData data;

    public HeatExchangerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(6));
    }

    public HeatExchangerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.HEAT_EXCHANGER_MENU.get(), id);
        this.blockEntity = (HeatExchangerBlockEntity) entity;
        this.data = data;
        addDataSlots(data);
    }

    public ContainerData getData() { return data; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
    }
}
