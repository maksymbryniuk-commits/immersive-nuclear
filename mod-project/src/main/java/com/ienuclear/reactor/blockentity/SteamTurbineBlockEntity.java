package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.SteamTurbineBlock;
import com.ienuclear.reactor.menu.SteamTurbineMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Контролер мультиблокової Парової турбіни 3×3×5 (ширина 3, висота 3, довжина 5):
 * - Передня грань (центр): Контролер парової турбіни (Steam Turbine)
 * - Внутрішня вісь (3 блоки вглиб): 3× Ротор парової турбіни (Turbine Rotor)
 * - Зовнішня оболонка (41 блок): Корпус турбіни (Turbine Casing), Радіаційне скло (Reactor Glass),
 *   щонайменше 1× Порт входу пари (Steam Input Port) та 1× Порт виходу енергії FE (Energy Output Port).
 */
public class SteamTurbineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_RPM = 3600;
    public static final int MAX_STEAM_PER_TICK = 1200;

    private boolean multiblockValid = false;
    private int rpm = 0;
    private int lastFePerTick = 0;
    private int lastSteamUsed = 0;

    private final List<BlockPos> cachedSteamInputs = new ArrayList<>();
    private final List<BlockPos> cachedEnergyOutputs = new ArrayList<>();

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
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() { return 11; }
    };

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_TURBINE.get(), pos, state);
    }

    public ContainerData getData() { return data; }

    private int getStatusCode() {
        if (!multiblockValid) return 0; // NO MULTIBLOCK
        if (rpm >= 2400 && lastSteamUsed > 0) return 3; // OPTIMAL ONLINE
        if (lastSteamUsed > 0 || rpm > 100) return 2; // SPINNING UP / COASTING
        return 1; // IDLE
    }

    /**
     * Перевіряє мультиблок 3 (ширина) × 3 (висота) × 5 (довжина) позаду контролера.
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
                        if (dx == 0 && dy == 0 && dz == 0) continue; // сам контролер
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

        if (foundSteam.isEmpty() || foundEnergy.isEmpty()) {
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
            int drained = turbine.drainSteam(MAX_STEAM_PER_TICK);
            turbine.lastSteamUsed = drained;

            if (drained > 0) {
                spinning = true;
                int targetRpm = Math.min(MAX_RPM, Math.max(800, drained * 4));
                if (turbine.rpm < targetRpm) {
                    turbine.rpm = Math.min(targetRpm, turbine.rpm + 40);
                } else {
                    turbine.rpm = Math.max(targetRpm, turbine.rpm - 20);
                }

                // Ефективність до 30 FE за 1 mB гарячої пари на максимальних обертах (до 36 000 FE/t)
                double rpmFactor = 0.35 + 0.65 * ((double) turbine.rpm / MAX_RPM);
                int feGenerated = (int) Math.round(drained * 30.0 * rpmFactor);
                turbine.insertEnergy(feGenerated);
                turbine.lastFePerTick = feGenerated;
            } else {
                turbine.lastFePerTick = 0;
                if (turbine.rpm > 0) {
                    turbine.rpm = Math.max(0, turbine.rpm - 25);
                }
            }
        } else {
            turbine.lastSteamUsed = 0;
            turbine.lastFePerTick = 0;
            if (turbine.rpm > 0) {
                turbine.rpm = Math.max(0, turbine.rpm - 40);
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

    private int drainSteam(int amount) {
        if (level == null) return 0;
        int remaining = amount;
        for (BlockPos p : cachedSteamInputs) {
            if (remaining <= 0) break;
            if (level.getBlockEntity(p) instanceof SteamInputPortBlockEntity port) {
                remaining -= port.drainSteam(remaining);
            }
        }
        return amount - remaining;
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

    private void insertEnergy(int amount) {
        if (level == null || amount <= 0) return;
        int remaining = amount;
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
