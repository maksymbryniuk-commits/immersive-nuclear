package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.SteamTurbineBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SteamTurbineMenu extends AbstractContainerMenu {
    private final SteamTurbineBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public SteamTurbineMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv,
                (SteamTurbineBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(11));
    }

    public SteamTurbineMenu(int containerId, Inventory playerInv,
                            SteamTurbineBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.STEAM_TURBINE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInv.player.level();
        this.data = data;
        addDataSlots(data);
    }

    public int getRpm() { return data.get(0); }
    public int getFePerTick() { return data.get(1); }
    public int getSteamPerTick() { return data.get(2); }
    public int getSteamStored() { return data.get(3); }
    public int getSteamCapacity() { return Math.max(1, data.get(4)); }
    public int getEnergyStored() { return (data.get(5) & 0xFFFF) | ((data.get(6) & 0xFFFF) << 16); }
    public int getEnergyCapacity() { return Math.max(1, (data.get(7) & 0xFFFF) | ((data.get(8) & 0xFFFF) << 16)); }
    public boolean isMultiblockValid() { return data.get(9) == 1; }
    public int getStatusCode() { return data.get(10); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.STEAM_TURBINE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
