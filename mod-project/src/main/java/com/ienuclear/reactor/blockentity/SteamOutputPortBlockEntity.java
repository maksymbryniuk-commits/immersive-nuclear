package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamOutputPortBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32000;
    public static final int MAX_PUSH_PER_TICK = 2000;

    private final FluidTank steamTank = new FluidTank(CAPACITY,
            stack -> stack.getFluid().isSame(ModFluids.HOT_STEAM_SOURCE.get())) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    // Обгортка для зовнішніх труб: дозволяє лише забирати пару (drain), а не закачувати назад
    private final IFluidHandler externalHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return steamTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return steamTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return steamTank.drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return steamTank.drain(maxDrain, action); }
    };

    public SteamOutputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_OUTPUT_PORT.get(), pos, state);
    }

    public IFluidHandler getExternalHandler() {
        return externalHandler;
    }

    public int getSteamStored() {
        return steamTank.getFluidAmount();
    }

    public int insertGeneratedSteam(int amount) {
        if (amount <= 0) return 0;
        return steamTank.fill(new FluidStack(ModFluids.HOT_STEAM_SOURCE.get(), amount), IFluidHandler.FluidAction.EXECUTE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SteamOutputPortBlockEntity port) {
        if (port.steamTank.isEmpty()) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());
            if (target != null) {
                int toSend = Math.min(port.steamTank.getFluidAmount(), MAX_PUSH_PER_TICK);
                FluidStack stackToSend = new FluidStack(ModFluids.HOT_STEAM_SOURCE.get(), toSend);
                int accepted = target.fill(stackToSend, IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0) {
                    port.steamTank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                }
                if (port.steamTank.isEmpty()) break;
            }
        }
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
