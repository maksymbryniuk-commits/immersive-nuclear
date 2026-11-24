package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.menu.FuelRodAssemblerMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Складальний верстат для ТВЕЛів: збирає уранові, збагачені, МОКС та торієві паливні капсули.
 */
public class FuelRodAssemblerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_PROGRESS = 100;
    private int progress = 0;

    private final EnergyStorage energy = new EnergyStorage(50_000, 1024, 0);
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> energy.getEnergyStored();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 2; }
    };

    public FuelRodAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUEL_ROD_ASSEMBLER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }

    private ItemStack computeOutput() {
        ItemStack s0 = inventory.getStackInSlot(0);
        ItemStack s1 = inventory.getStackInSlot(1);
        ItemStack s2 = inventory.getStackInSlot(2);
        if (!s0.is(ModItems.EMPTY_FUEL_ROD.get())) return ItemStack.EMPTY;

        if (s1.is(ModItems.PLUTONIUM_DUST.get()) && s2.is(ModItems.YELLOWCAKE_URANIUM.get())) {
            return new ItemStack(ModItems.MOX_FUEL_CAPSULE.get());
        }
        if (s1.is(ModItems.YELLOWCAKE_URANIUM.get()) && s2.is(Items.REDSTONE)) {
            return new ItemStack(ModItems.THORIUM_FUEL_CAPSULE.get());
        }
        if (s1.is(ModItems.ENRICHED_URANIUM_PELLET.get()) && s2.isEmpty()) {
            return new ItemStack(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get());
        }
        if (s1.is(ModItems.YELLOWCAKE_URANIUM.get()) && s2.isEmpty()) {
            return new ItemStack(ModItems.NUCLEAR_FUEL_ROD.get());
        }
        return ItemStack.EMPTY;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FuelRodAssemblerBlockEntity be) {
        ItemStack result = be.computeOutput();
        ItemStack out = be.inventory.getStackInSlot(3);
        if (!result.isEmpty() && out.isEmpty() && be.energy.getEnergyStored() >= 60) {
            be.energy.extractEnergy(60, false);
            be.progress++;
            if (be.progress >= MAX_PROGRESS) {
                be.progress = 0;
                be.inventory.getStackInSlot(0).shrink(1);
                be.inventory.getStackInSlot(1).shrink(1);
                if (!be.inventory.getStackInSlot(2).isEmpty()) be.inventory.getStackInSlot(2).shrink(1);
                be.inventory.setStackInSlot(3, result);
            }
            be.setChanged();
        } else if (be.progress > 0) {
            be.progress = 0;
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("Progress", progress);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Inv", inventory.serializeNBT(reg));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        progress = tag.getInt("Progress");
        energy.receiveEnergy(tag.getInt("Energy"), false);
        inventory.deserializeNBT(reg, tag.getCompound("Inv"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.fuel_rod_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new FuelRodAssemblerMenu(id, inv, this, data);
    }
}
