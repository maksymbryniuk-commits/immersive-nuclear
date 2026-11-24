package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.CoolingTowerBlock;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Градирня (7×7×10): конденсує відпрацьовану пару назад у дистильовану воду (замкнутий цикл).
 */
public class CoolingTowerBlockEntity extends BlockEntity {
    private boolean multiblockValid = false;
    private final FluidTank steamTank = new FluidTank(32_000, fs ->
            fs.getFluid() == ModFluids.EXHAUST_STEAM_SOURCE.get()
                    || fs.getFluid() == ModFluids.HOT_STEAM_SOURCE.get());
    private final FluidTank waterTank = new FluidTank(32_000);

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }
        @Override public FluidStack getFluidInTank(int t) { return t == 0 ? steamTank.getFluid() : waterTank.getFluid(); }
        @Override public int getTankCapacity(int t) { return 32_000; }
        @Override public boolean isFluidValid(int t, FluidStack s) { return t == 0 && steamTank.isFluidValid(s); }
        @Override public int fill(FluidStack resource, FluidAction action) { return steamTank.fill(resource, action); }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return waterTank.drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return waterTank.drain(maxDrain, action); }
    };

    public CoolingTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOLING_TOWER.get(), pos, state);
    }

    public IFluidHandler getFluidHandler() { return fluidHandler; }
    public boolean isMultiblockValid() { return multiblockValid; }
    public int getSteamAmount() { return steamTank.getFluidAmount(); }
    public int getWaterAmount() { return waterTank.getFluidAmount(); }

    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(CoolingTowerBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();
        Block concrete = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "concrete"));
        Block leaded = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "concrete_leaded"));
        Block scaffold = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "steel_scaffolding_standard"));

        int shellCount = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 0; dy < 10; dy++) {
                for (int dz = 0; dz < 7; dz++) {
                    boolean edge = Math.abs(dx) == 3 || dz == 0 || dz == 6;
                    if (!edge) continue;
                    BlockPos p = worldPosition.relative(right, dx).above(dy).relative(back, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.is(concrete) || bs.is(leaded) || bs.is(scaffold)) shellCount++;
                }
            }
        }
        multiblockValid = shellCount >= 80;
        return multiblockValid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoolingTowerBlockEntity be) {
        if (level.getGameTime() % 40 == 0) {
            be.validateMultiblock();
        }
        if (be.multiblockValid && be.steamTank.getFluidAmount() > 0 && be.waterTank.getSpace() > 0) {
            int amt = Math.min(200, Math.min(be.steamTank.getFluidAmount(), be.waterTank.getSpace()));
            be.steamTank.drain(amt, IFluidHandler.FluidAction.EXECUTE);
            be.waterTank.fill(new FluidStack(ModFluids.DISTILLED_WATER_SOURCE.get(), amt), IFluidHandler.FluidAction.EXECUTE);
            if (level instanceof ServerLevel sl && level.getGameTime() % 4 == 0) {
                Direction back = state.getValue(CoolingTowerBlock.FACING).getOpposite();
                BlockPos top = pos.relative(back, 3).above(10);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, 3, 0.8, 0.3, 0.8, 0.03);
            }
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.put("Steam", steamTank.writeToNBT(reg, new CompoundTag()));
        tag.put("Water", waterTank.writeToNBT(reg, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        steamTank.readFromNBT(reg, tag.getCompound("Steam"));
        waterTank.readFromNBT(reg, tag.getCompound("Water"));
    }
}
