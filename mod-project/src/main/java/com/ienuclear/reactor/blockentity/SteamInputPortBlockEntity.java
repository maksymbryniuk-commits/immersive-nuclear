package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamInputPortBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32000;

    private final FluidTank steamTank = new FluidTank(CAPACITY,
            stack -> stack.getFluid().isSame(ModFluids.HOT_STEAM_SOURCE.get())) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public SteamInputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_INPUT_PORT.get(), pos, state);
    }

    public FluidTank getSteamTank() {
        return steamTank;
    }

    public int getSteamAmount() {
        return steamTank.getFluidAmount();
    }

    public int drainSteam(int amount) {
        FluidStack drained = steamTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        steamTank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        steamTank.readFromNBT(registries, tag);
    }
}
