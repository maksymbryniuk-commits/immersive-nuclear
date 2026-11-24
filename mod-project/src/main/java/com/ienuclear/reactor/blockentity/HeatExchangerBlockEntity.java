package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.HeatExchangerBlock;
import com.ienuclear.reactor.menu.HeatExchangerMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * Промисловий теплообмінник (3×3×5):
 * Первинний контур (Hot Coolant або Liquid Sodium — 2x) гріє вторинний контур (Distilled Water -> Hot Steam).
 * При роботі насухо за високого тепла можливий розрив трубок і радіоактивне забруднення пари!
 */
public class HeatExchangerBlockEntity extends BlockEntity implements MenuProvider {
    private boolean multiblockValid = false;
    private boolean ruptured = false;
    private int heat = 20;

    private final FluidTank primaryTank = new FluidTank(16_000, fs ->
            fs.getFluid() == ModFluids.HOT_COOLANT_SOURCE.get()
                    || fs.getFluid() == ModFluids.LIQUID_SODIUM_SOURCE.get()
                    || fs.getFluid() == ModFluids.HEAVY_COOLANT_SOURCE.get());

    private final FluidTank secondaryWaterTank = new FluidTank(16_000, fs ->
            fs.getFluid() == ModFluids.DISTILLED_WATER_SOURCE.get()
                    || fs.getFluid() == Fluids.WATER);

    private final FluidTank secondarySteamTank = new FluidTank(16_000);

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 3; }
        @Override public FluidStack getFluidInTank(int t) {
            return switch (t) {
                case 0 -> primaryTank.getFluid();
                case 1 -> secondaryWaterTank.getFluid();
                default -> secondarySteamTank.getFluid();
            };
        }
        @Override public int getTankCapacity(int t) { return 16_000; }
        @Override public boolean isFluidValid(int t, FluidStack s) {
            return (t == 0 && primaryTank.isFluidValid(s)) || (t == 1 && secondaryWaterTank.isFluidValid(s));
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (primaryTank.isFluidValid(resource)) return primaryTank.fill(resource, action);
            if (secondaryWaterTank.isFluidValid(resource)) return secondaryWaterTank.fill(resource, action);
            return 0;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return secondarySteamTank.drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return secondarySteamTank.drain(maxDrain, action); }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> multiblockValid ? 1 : 0;
                case 1 -> ruptured ? 1 : 0;
                case 2 -> heat;
                case 3 -> primaryTank.getFluidAmount();
                case 4 -> secondaryWaterTank.getFluidAmount();
                case 5 -> secondarySteamTank.getFluidAmount();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 6; }
    };

    public HeatExchangerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_EXCHANGER_CONTROLLER.get(), pos, state);
    }

    public IFluidHandler getFluidHandler() { return fluidHandler; }
    public ContainerData getData() { return data; }
    public boolean isRuptured() { return ruptured; }

    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(HeatExchangerBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();
        Block sheet = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "sheetmetal_steel"));
        Block pipe = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "fluid_pipe"));
        Block scaffold = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "steel_scaffolding_standard"));

        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos p = worldPosition.relative(right, dx).above(dy).relative(back, dz);
                    BlockState bs = level.getBlockState(p);
                    if (bs.is(sheet) || bs.is(pipe) || bs.is(scaffold)) count++;
                }
            }
        }
        multiblockValid = count >= 20;
        return multiblockValid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeatExchangerBlockEntity be) {
        if (level.getGameTime() % 20 == 0) {
            be.validateMultiblock();
        }
        if (be.multiblockValid && be.primaryTank.getFluidAmount() >= 10) {
            boolean isSodium = be.primaryTank.getFluid().getFluid() == ModFluids.LIQUID_SODIUM_SOURCE.get();
            int rate = isSodium ? 20 : 10;
            if (be.secondaryWaterTank.getFluidAmount() >= rate && be.secondarySteamTank.getSpace() >= rate * 10) {
                be.primaryTank.drain(10, IFluidHandler.FluidAction.EXECUTE);
                be.secondaryWaterTank.drain(rate, IFluidHandler.FluidAction.EXECUTE);
                be.secondarySteamTank.fill(new FluidStack(ModFluids.HOT_STEAM_SOURCE.get(), rate * 10), IFluidHandler.FluidAction.EXECUTE);
                be.heat = Math.min(950, be.heat + 5);
            } else {
                // Без води трубки перегріваються -> розрив!
                be.heat = Math.min(1100, be.heat + 8);
                if (be.heat >= 1000) {
                    be.ruptured = true;
                }
            }
        } else if (be.heat > 20 && level.getGameTime() % 4 == 0) {
            be.heat--;
        }
        // Автовиштовхування чистої пари в сусідні труби / порти турбіни
        if (!be.secondarySteamTank.isEmpty()) {
            for (Direction d : Direction.values()) {
                IFluidHandler target = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, pos.relative(d), d.getOpposite());
                if (target != null) {
                    int amt = Math.min(be.secondarySteamTank.getFluidAmount(), 1000);
                    int acc = target.fill(new FluidStack(ModFluids.HOT_STEAM_SOURCE.get(), amt), IFluidHandler.FluidAction.EXECUTE);
                    if (acc > 0) be.secondarySteamTank.drain(acc, IFluidHandler.FluidAction.EXECUTE);
                    if (be.secondarySteamTank.isEmpty()) break;
                }
            }
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putBoolean("Ruptured", ruptured);
        tag.putInt("Heat", heat);
        tag.put("Primary", primaryTank.writeToNBT(reg, new CompoundTag()));
        tag.put("Water", secondaryWaterTank.writeToNBT(reg, new CompoundTag()));
        tag.put("Steam", secondarySteamTank.writeToNBT(reg, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        ruptured = tag.getBoolean("Ruptured");
        heat = tag.getInt("Heat");
        primaryTank.readFromNBT(reg, tag.getCompound("Primary"));
        secondaryWaterTank.readFromNBT(reg, tag.getCompound("Water"));
        secondarySteamTank.readFromNBT(reg, tag.getCompound("Steam"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.heat_exchanger_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new HeatExchangerMenu(id, inv, this, data);
    }
}
