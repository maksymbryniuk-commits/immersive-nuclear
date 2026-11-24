package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * РІТЕГ (RTG): виробляє пасивну енергію (128 FE/t з тритієм, 64 FE/t з плутонієм) та віддає її сусідам.
 */
public class RtgGeneratorBlockEntity extends BlockEntity {
    private int outputFePerTick = 0;
    private final EnergyStorage energy = new EnergyStorage(100_000, 0, 1024);

    public RtgGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RTG_GENERATOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public int getOutputFePerTick() { return outputFePerTick; }

    public boolean insertFuel(ItemStack stack) {
        if (outputFePerTick == 0) {
            if (stack.is(ModItems.TRITIUM_CELL.get())) outputFePerTick = 128;
            else if (stack.is(ModItems.PLUTONIUM_DUST.get())) outputFePerTick = 64;
            else return false;
            stack.shrink(1);
            setChanged();
            return true;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RtgGeneratorBlockEntity be) {
        if (be.outputFePerTick > 0) {
            be.energy.receiveEnergy(be.outputFePerTick, false);
        }
        if (be.energy.getEnergyStored() > 0) {
            for (Direction d : Direction.values()) {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(d), d.getOpposite());
                if (target != null && target.canReceive()) {
                    int sent = target.receiveEnergy(Math.min(512, be.energy.getEnergyStored()), false);
                    be.energy.extractEnergy(sent, false);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("Rate", outputFePerTick);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        outputFePerTick = tag.getInt("Rate");
        energy.receiveEnergy(tag.getInt("Energy"), false);
    }
}
