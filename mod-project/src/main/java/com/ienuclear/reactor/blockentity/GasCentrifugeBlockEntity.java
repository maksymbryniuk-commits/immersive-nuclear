package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.GasCentrifugeBlock;
import com.ienuclear.reactor.menu.GasCentrifugeMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Газова центрифуга 3×4×2 з інерцією ротора (0–60 000 RPM) та каскадуванням.
 * Поки оберти < 80% (48 000 RPM), розділення не відбувається!
 */
public class GasCentrifugeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_RPM = 60000;
    public static final int MIN_WORK_RPM = 48000;
    public static final int FE_PER_TICK = 1024;

    private int rpm = 0;
    private boolean multiblockValid = false;

    private final EnergyStorage energy = new EnergyStorage(100_000, 4096, 0);

    private final FluidTank inputTank = new FluidTank(16_000, fs ->
            fs.getFluid() == ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get()
                    || fs.getFluid() == ModFluids.ENRICHED_URANIUM_SOURCE.get());

    private final FluidTank lightOutputTank = new FluidTank(16_000);
    private final FluidTank heavyOutputTank = new FluidTank(16_000);

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 3; }
        @Override public FluidStack getFluidInTank(int t) {
            return switch (t) {
                case 0 -> inputTank.getFluid();
                case 1 -> lightOutputTank.getFluid();
                default -> heavyOutputTank.getFluid();
            };
        }
        @Override public int getTankCapacity(int t) { return 16_000; }
        @Override public boolean isFluidValid(int t, FluidStack stack) {
            return t == 0 && inputTank.isFluidValid(stack);
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            return inputTank.fill(resource, action);
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack d = lightOutputTank.drain(resource, action);
            if (!d.isEmpty()) return d;
            return heavyOutputTank.drain(resource, action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack d = lightOutputTank.drain(maxDrain, action);
            if (!d.isEmpty()) return d;
            return heavyOutputTank.drain(maxDrain, action);
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> multiblockValid ? 1 : 0;
                case 1 -> rpm;
                case 2 -> energy.getEnergyStored();
                case 3 -> inputTank.getFluidAmount();
                case 4 -> lightOutputTank.getFluidAmount();
                case 5 -> heavyOutputTank.getFluidAmount();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 6; }
    };

    public GasCentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_CENTRIFUGE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public IFluidHandler getFluidHandler() { return fluidHandler; }
    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }

    private static boolean isIeOrCentrifugeBlock(BlockState bs, String ieName) {
        Block ie = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", ieName));
        return bs.is(ie);
    }

    /**
     * Перевіряє 3×4×2 раму центрифуги позаду контролера (допускає IE-блоки з профілю структури).
     */
    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(GasCentrifugeBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();

        int scaffold = 0, sheetmetal = 0, engineering = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz = 0; dz < 2; dz++) {
                    BlockPos p = worldPosition.relative(right, dx).above(dy).relative(back, dz);
                    if (p.equals(worldPosition)) continue;
                    BlockState bs = level.getBlockState(p);
                    if (isIeOrCentrifugeBlock(bs, "steel_scaffolding_standard")) scaffold++;
                    else if (isIeOrCentrifugeBlock(bs, "sheetmetal_steel")) sheetmetal++;
                    else if (isIeOrCentrifugeBlock(bs, "light_engineering")
                            || isIeOrCentrifugeBlock(bs, "heavy_engineering")
                            || isIeOrCentrifugeBlock(bs, "radiator")) engineering++;
                }
            }
        }
        multiblockValid = (scaffold >= 4 && sheetmetal >= 6 && engineering >= 4);
        return multiblockValid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasCentrifugeBlockEntity be) {
        if (level.getGameTime() % 20 == 0) {
            be.validateMultiblock();
        }

        // Ручна подача відром UF6 або жовтого кеку через слот 0
        ItemStack inStack = be.inventory.getStackInSlot(0);
        if (inStack.is(ModItems.URANIUM_HEXAFLUORIDE_BUCKET.get()) && be.inputTank.getSpace() >= 1000) {
            be.inputTank.fill(new FluidStack(ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
            be.inventory.setStackInSlot(0, new ItemStack(Items.BUCKET));
        } else if (inStack.is(ModItems.YELLOWCAKE_URANIUM.get()) && be.inputTank.getSpace() >= 250) {
            be.inputTank.fill(new FluidStack(ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get(), 250), IFluidHandler.FluidAction.EXECUTE);
            inStack.shrink(1);
        }

        if (be.multiblockValid && be.energy.getEnergyStored() >= FE_PER_TICK) {
            be.energy.extractEnergy(FE_PER_TICK, false);
            be.rpm = Math.min(MAX_RPM, be.rpm + 150);
        } else {
            be.rpm = Math.max(0, be.rpm - 35);
        }

        // Розділення газів лише при >= 80% RPM (48 000 RPM)
        if (be.multiblockValid && be.rpm >= MIN_WORK_RPM && be.inputTank.getFluidAmount() >= 20) {
            FluidStack inFluid = be.inputTank.getFluid();
            if (inFluid.getFluid() == ModFluids.URANIUM_HEXAFLUORIDE_SOURCE.get()) {
                if (be.lightOutputTank.getSpace() >= 5 && be.heavyOutputTank.getSpace() >= 15) {
                    be.inputTank.drain(20, IFluidHandler.FluidAction.EXECUTE);
                    be.lightOutputTank.fill(new FluidStack(ModFluids.ENRICHED_URANIUM_SOURCE.get(), 5), IFluidHandler.FluidAction.EXECUTE);
                    be.heavyOutputTank.fill(new FluidStack(ModFluids.DEPLETED_UF6_SOURCE.get(), 15), IFluidHandler.FluidAction.EXECUTE);
                }
            } else if (inFluid.getFluid() == ModFluids.ENRICHED_URANIUM_SOURCE.get() && level.getGameTime() % 10 == 0) {
                // Каскадне повторне збагачення -> тритієвий ізотоп!
                ItemStack out = be.inventory.getStackInSlot(1);
                if (out.isEmpty() || (out.is(ModItems.TRITIUM_CELL.get()) && out.getCount() < out.getMaxStackSize())) {
                    be.inputTank.drain(20, IFluidHandler.FluidAction.EXECUTE);
                    if (out.isEmpty()) {
                        be.inventory.setStackInSlot(1, new ItemStack(ModItems.TRITIUM_CELL.get(), 1));
                    } else {
                        out.grow(1);
                    }
                }
            }
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("Rpm", rpm);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("InputTank", inputTank.writeToNBT(reg, new CompoundTag()));
        tag.put("LightTank", lightOutputTank.writeToNBT(reg, new CompoundTag()));
        tag.put("HeavyTank", heavyOutputTank.writeToNBT(reg, new CompoundTag()));
        tag.put("Inv", inventory.serializeNBT(reg));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        rpm = tag.getInt("Rpm");
        energy.receiveEnergy(tag.getInt("Energy"), false);
        inputTank.readFromNBT(reg, tag.getCompound("InputTank"));
        lightOutputTank.readFromNBT(reg, tag.getCompound("LightTank"));
        heavyOutputTank.readFromNBT(reg, tag.getCompound("HeavyTank"));
        inventory.deserializeNBT(reg, tag.getCompound("Inv"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.gas_centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new GasCentrifugeMenu(id, inv, this, data);
    }
}
