package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * BlockEntity системи аварійного охолодження (ECCS): 16 000 mB холодної води / азоту.
 */
public class EccsUnitBlockEntity extends BlockEntity {
    private final FluidTank tank = new FluidTank(16_000, fs ->
            fs.getFluid() == Fluids.WATER
                    || fs.getFluid() == ModFluids.DISTILLED_WATER_SOURCE.get()
                    || fs.getFluid() == ModFluids.HEAVY_COOLANT_SOURCE.get());

    public EccsUnitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECCS_UNIT.get(), pos, state);
    }

    public FluidTank getTank() { return tank; }

    public boolean consumeEmergencyCoolant() {
        if (tank.getFluidAmount() >= 4000) {
            tank.drain(4000, IFluidHandler.FluidAction.EXECUTE);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.put("Tank", tank.writeToNBT(reg, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        tank.readFromNBT(reg, tag.getCompound("Tank"));
    }
}
