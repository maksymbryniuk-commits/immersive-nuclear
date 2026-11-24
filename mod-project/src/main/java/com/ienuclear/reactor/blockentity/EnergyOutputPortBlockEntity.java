package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyOutputPortBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_EXTRACT = 16_384;

    private final ReactorEnergyStorage energyStorage = new ReactorEnergyStorage(CAPACITY, MAX_EXTRACT, MAX_EXTRACT);

    public EnergyOutputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_OUTPUT_PORT.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int insertGeneratedEnergy(int amount) {
        int added = energyStorage.addInternalEnergy(amount);
        if (added > 0) setChanged();
        return added;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyOutputPortBlockEntity port) {
        if (port.energyStorage.getEnergyStored() <= 0) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, dir.getOpposite());
            if (target != null && target.canReceive()) {
                int toSend = Math.min(port.energyStorage.getEnergyStored(), MAX_EXTRACT);
                int accepted = target.receiveEnergy(toSend, false);
                if (accepted > 0) {
                    port.energyStorage.extractEnergy(accepted, false);
                    port.setChanged();
                }
                if (port.energyStorage.getEnergyStored() <= 0) break;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setEnergy(tag.getInt("Energy"));
    }

    private static class ReactorEnergyStorage extends EnergyStorage {
        public ReactorEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        @Override
        public boolean canReceive() {
            return false; // Зовнішні дроти не можуть заряджати вихідний порт
        }

        public int addInternalEnergy(int amount) {
            int accepted = Math.min(capacity - energy, amount);
            energy += accepted;
            return accepted;
        }

        public void setEnergy(int value) {
            this.energy = Math.max(0, Math.min(capacity, value));
        }
    }
}
