package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.SteamTurbineBlock;
import com.ienuclear.reactor.menu.SteamTurbineMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Контролер мультиблокової Парової турбіни 3×3×5 (ширина 3, висота 3, довжина 5):
 * - 3× Порти входу гарячої пари (Steam Input Port)
 * - 3× Порти виходу енергії FE (Energy Output Port)
 * - Кожен підключений і активний вхід пари додає +1200 RPM до максимальної швидкості (1200 -> 2400 -> 3600 RPM)!
 */
public class SteamTurbineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_RPM = 3600;
    public static final int RPM_PER_INPUT = 1200;
    public static final int STEAM_PER_INPUT_PER_TICK = 400;

    private boolean multiblockValid = false;
    private int rpm = 0;
    private int lastFePerTick = 0;
    private int lastSteamUsed = 0;
    private int activeInputsCount = 0;

    private final List<BlockPos> cachedSteamInputs = new ArrayList<>();
    private final List<BlockPos> cachedEnergyOutputs = new ArrayList<>();
    private final FluidTank exhaustSteamTank = new FluidTank(32_000);
    private final IFluidHandler exhaustHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int t) { return exhaustSteamTank.getFluid(); }
        @Override public int getTankCapacity(int t) { return 32_000; }
        @Override public boolean isFluidValid(int t, FluidStack s) { return false; }
        @Override public int fill(FluidStack r, FluidAction a) { return 0; }
        @Override public FluidStack drain(FluidStack r, FluidAction a) { return exhaustSteamTank.drain(r, a); }
        @Override public FluidStack drain(int m, FluidAction a) { return exhaustSteamTank.drain(m, a); }
    };

    public IFluidHandler getExhaustHandler() { return exhaustHandler; }

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> rpm;
                case 1 -> lastFePerTick;
                case 2 -> lastSteamUsed;
                case 3 -> Math.min(32767, getSteamStored());
                case 4 -> Math.min(32767, getSteamCapacity());
                case 5 -> getEnergyStored() & 0xFFFF;
                case 6 -> (getEnergyStored() >> 16) & 0xFFFF;
                case 7 -> getEnergyCapacity() & 0xFFFF;
                case 8 -> (getEnergyCapacity() >> 16) & 0xFFFF;
                case 9 -> multiblockValid ? 1 : 0;
                case 10 -> getStatusCode();
                case 11 -> activeInputsCount;
                case 12 -> cachedEnergyOutputs.size();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() { return 13; }
    };

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_TURBINE.get(), pos, state);
    }

    public ContainerData getData() { return data; }

    private int getStatusCode() {
        if (!multiblockValid) return 0; // NO MULTIBLOCK
        if (rpm >= 2400 && lastSteamUsed > 0) return 3; // HIGH SPEED ONLINE
        if (lastSteamUsed > 0 || rpm > 100) return 2; // SPINNING UP / PARTIAL
        return 1; // IDLE
    }

    /**
     * Перевіряє мультиблок 3 (ширина) × 3 (висота) × 5 (довжина):
     * вимагає 3× Ротори всередині, щонайменше 3× Порти входу пари та 3× Порти виходу FE.
     */
    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(SteamTurbineBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();

        List<BlockPos> foundSteam = new ArrayList<>();
        List<BlockPos> foundEnergy = new ArrayList<>();

        for (int dz = 0; dz < 5; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    BlockPos p = worldPosition.relative(back, dz).relative(right, dx).above(dy);
                    boolean isInterior = (dx == 0 && dy == 0 && dz >= 1 && dz <= 3);
                    BlockState bs = level.getBlockState(p);

                    if (isInterior) {
                        if (!bs.is(ModBlocks.TURBINE_ROTOR.get())) {
                            multiblockValid = false;
                            return false;
                        }
                    } else {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (bs.is(ModBlocks.STEAM_INPUT_PORT.get())) {
                            foundSteam.add(p);
                        } else if (bs.is(ModBlocks.ENERGY_OUTPUT_PORT.get())) {
                            foundEnergy.add(p);
                        } else if (!bs.is(ModBlocks.TURBINE_CASING.get())
                                && !bs.is(ModBlocks.REACTOR_GLASS.get())) {
                            multiblockValid = false;
                            return false;
                        }
                    }
                }
            }
        }

        if (foundSteam.size() < 3 || foundEnergy.size() < 3) {
            multiblockValid = false;
            return false;
        }

        cachedSteamInputs.clear();
        cachedSteamInputs.addAll(foundSteam);
        cachedEnergyOutputs.clear();
        cachedEnergyOutputs.addAll(foundEnergy);
        multiblockValid = true;
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity turbine) {
        if (level.getGameTime() % 20 == 0) {
            turbine.validateMultiblock();
        }

        boolean spinning = false;
        if (turbine.multiblockValid) {
            int activeInputs = 0;
            int totalDrained = 0;

            for (BlockPos p : turbine.cachedSteamInputs) {
                if (level.getBlockEntity(p) instanceof SteamInputPortBlockEntity port) {
                    int drained = port.drainSteam(STEAM_PER_INPUT_PER_TICK);
                    if (drained > 0) {
                        activeInputs++;
                        totalDrained += drained;
                    }
                }
            }

            turbine.activeInputsCount = Math.min(3, activeInputs);
            turbine.lastSteamUsed = totalDrained;

            if (totalDrained > 0 && activeInputs > 0) {
                spinning = true;
                // З кожним підключеним входом максимальна швидкість зростає: 1 -> 1200, 2 -> 2400, 3 -> 3600 RPM
                int capRpm = Math.min(MAX_RPM, turbine.activeInputsCount * RPM_PER_INPUT);
                int targetRpm = Math.min(capRpm, Math.max(400, totalDrained * 3));
                int accel = 15 * turbine.activeInputsCount;

                if (turbine.rpm < targetRpm) {
                    turbine.rpm = Math.min(targetRpm, turbine.rpm + accel);
                } else {
                    turbine.rpm = Math.max(targetRpm, turbine.rpm - 20);
                }

                double rpmFactor = 0.35 + 0.65 * ((double) turbine.rpm / MAX_RPM);
                int feGenerated = (int) Math.round(totalDrained * 30.0 * rpmFactor);
                turbine.insertEnergyEvenly(feGenerated);
                turbine.lastFePerTick = feGenerated;

                // Замкнутий цикл пари: випуск відпрацьованої пари для Градирні!
                int exhaustAmt = Math.min(turbine.exhaustSteamTank.getSpace(), Math.max(1, totalDrained / 10));
                if (exhaustAmt > 0) {
                    turbine.exhaustSteamTank.fill(new FluidStack(ModFluids.EXHAUST_STEAM_SOURCE.get(), exhaustAmt), IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                turbine.lastFePerTick = 0;
                if (turbine.rpm > 0) {
                    turbine.rpm = Math.max(0, turbine.rpm - 25);
                }
            }
        } else {
            turbine.activeInputsCount = 0;
            turbine.lastSteamUsed = 0;
            turbine.lastFePerTick = 0;
            if (turbine.rpm > 0) {
                turbine.rpm = Math.max(0, turbine.rpm - 40);
            }
        }

        // Виштовхування відпрацьованої пари в сусідні труби / градирню
        if (!turbine.exhaustSteamTank.isEmpty()) {
            for (Direction d : Direction.values()) {
                IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(d), d.getOpposite());
                if (target != null) {
                    int amt = Math.min(turbine.exhaustSteamTank.getFluidAmount(), 1000);
                    int acc = target.fill(new FluidStack(ModFluids.EXHAUST_STEAM_SOURCE.get(), amt), IFluidHandler.FluidAction.EXECUTE);
                    if (acc > 0) turbine.exhaustSteamTank.drain(acc, IFluidHandler.FluidAction.EXECUTE);
                    if (turbine.exhaustSteamTank.isEmpty()) break;
                }
            }
        }

        boolean shouldBeActive = turbine.multiblockValid && (spinning || turbine.rpm > 100);
        if (state.hasProperty(SteamTurbineBlock.ACTIVE) && state.getValue(SteamTurbineBlock.ACTIVE) != shouldBeActive) {
            level.setBlock(pos, state.setValue(SteamTurbineBlock.ACTIVE, shouldBeActive), 3);
        }

        turbine.setChanged();
    }

    private int getSteamStored() {
        if (level == null) return 0;
        int sum = 0;
        for (BlockPos p : cachedSteamInputs) {
            if (level.getBlockEntity(p) instanceof SteamInputPortBlockEntity port) sum += port.getSteamAmount();
        }
        return sum;
    }

    private int getSteamCapacity() {
        return Math.max(1, cachedSteamInputs.size() * SteamInputPortBlockEntity.CAPACITY);
    }

    private int getEnergyStored() {
        if (level == null) return 0;
        int sum = 0;
        for (BlockPos p : cachedEnergyOutputs) {
            if (level.getBlockEntity(p) instanceof EnergyOutputPortBlockEntity port) sum += port.getEnergyStored();
        }
        return sum;
    }

    private int getEnergyCapacity() {
        return Math.max(1, cachedEnergyOutputs.size() * EnergyOutputPortBlockEntity.CAPACITY);
    }

    private void insertEnergyEvenly(int amount) {
        if (level == null || amount <= 0 || cachedEnergyOutputs.isEmpty()) return;
        int perPort = Math.max(1, amount / cachedEnergyOutputs.size());
        int remaining = amount;
        for (BlockPos p : cachedEnergyOutputs) {
            if (remaining <= 0) break;
            if (level.getBlockEntity(p) instanceof EnergyOutputPortBlockEntity port) {
                int toGive = Math.min(remaining, perPort + 1);
                remaining -= port.insertGeneratedEnergy(toGive);
            }
        }
        // Доливання залишку у будь-який вільний порт
        for (BlockPos p : cachedEnergyOutputs) {
            if (remaining <= 0) break;
            if (level.getBlockEntity(p) instanceof EnergyOutputPortBlockEntity port) {
                remaining -= port.insertGeneratedEnergy(remaining);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.steam_turbine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SteamTurbineMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Rpm", rpm);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rpm = tag.getInt("Rpm");
    }
}
