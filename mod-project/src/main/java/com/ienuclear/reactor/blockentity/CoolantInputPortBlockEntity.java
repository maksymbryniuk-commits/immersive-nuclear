package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CoolantInputPortBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16000;

    private final FluidTank fluidTank = new FluidTank(CAPACITY, stack ->
            stack.getFluid().isSame(ModFluids.HEAVY_COOLANT_SOURCE.get())
                    || stack.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public CoolantInputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOLANT_INPUT_PORT.get(), pos, state);
    }

    public IFluidHandler getFluidTank() {
        return fluidTank;
    }

    public int getFluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public boolean isHeavyCoolant() {
        FluidStack stack = fluidTank.getFluid();
        return !stack.isEmpty() && stack.getFluid().isSame(ModFluids.HEAVY_COOLANT_SOURCE.get());
    }

    public int drainCoolant(int maxDrain) {
        FluidStack drained = fluidTank.drain(maxDrain, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        fluidTank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidTank.readFromNBT(registries, tag);
    }
}
