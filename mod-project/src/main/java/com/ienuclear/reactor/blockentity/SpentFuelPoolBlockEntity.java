package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.SpentFuelPoolBlock;
import com.ienuclear.reactor.menu.SpentFuelPoolMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Мультиблоковий охолоджувальний басейн 3×3×3 (Spent Fuel Pool) на 9 слотів:
 * - Вимагає корпус 3×3×3 (Reactor Casing / Reactor Glass / Leaded Concrete) та заповнений водою внутрішній басейн (1×2).
 * - Охолоджує відпрацьовані стрижні (DEPLETED_FUEL_ROD) -> Зливок збідненого урану + Порожня капсула.
 * - Дистильована вода прискорює охолодження вдвічі!
 */
public class SpentFuelPoolBlockEntity extends BlockEntity implements MenuProvider {
    private int coolProgress = 0;
    private boolean multiblockValid = false;
    public static final int COOL_TIME = 200;

    private final FluidTank waterTank = new FluidTank(16_000, fs ->
            fs.getFluid() == Fluids.WATER || fs.getFluid() == ModFluids.DISTILLED_WATER_SOURCE.get());

    private final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> waterTank.getFluidAmount();
                case 1 -> coolProgress;
                case 2 -> multiblockValid ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 3; }
    };

    public SpentFuelPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPENT_FUEL_POOL.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public FluidTank getWaterTank() { return waterTank; }
    public ContainerData getData() { return data; }
    public boolean isMultiblockValid() { return multiblockValid; }

    private static boolean isPoolWallBlock(BlockState bs) {
        Block leaded = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "concrete_leaded"));
        return bs.is(ModBlocks.REACTOR_CASING.get())
                || bs.is(ModBlocks.REACTOR_GLASS.get())
                || bs.is(ModBlocks.WATER_INPUT_PORT.get())
                || bs.is(leaded);
    }

    /**
     * Перевіряє мультиблок басейну 3×3×3:
     * - Контролер спереду в центрі середнього шару (dy = 0, dx = 0, dz = 0)
     * - Усі стінки та дно 3×3×3 з корпусу реактора / скла / свинцевого бетону
     * - Внутрішня шахта басейну (dx = 0, dz = 1, dy = 0 та dy = +1) заповнена водою!
     */
    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(SpentFuelPoolBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = 0; dz < 3; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    BlockPos p = worldPosition.above(dy).relative(back, dz).relative(right, dx);
                    if (dy == 0 && dx == 0 && dz == 0) continue;
                    if (dy >= 0 && dx == 0 && dz == 1) {
                        if (!level.getFluidState(p).is(Fluids.WATER)) {
                            multiblockValid = false;
                            return false;
                        }
                    } else {
                        if (!isPoolWallBlock(level.getBlockState(p))) {
                            multiblockValid = false;
                            return false;
                        }
                    }
                }
            }
        }
        multiblockValid = true;
        return true;
    }

    private void depositEmptyRod(int excludeSlot) {
        ItemStack empty = new ItemStack(ModItems.EMPTY_FUEL_ROD.get(), 1);
        for (int i = 0; i < 9; i++) {
            if (i == excludeSlot) continue;
            ItemStack st = inventory.getStackInSlot(i);
            if (st.is(ModItems.EMPTY_FUEL_ROD.get()) && st.getCount() < st.getMaxStackSize()) {
                st.grow(1);
                return;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i != excludeSlot && inventory.getStackInSlot(i).isEmpty()) {
                inventory.setStackInSlot(i, empty);
                return;
            }
        }
        if (level != null) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, empty);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpentFuelPoolBlockEntity be) {
        if (level.getGameTime() % 20 == 0) {
            be.validateMultiblock();
        }

        // Автоматичний забір води з внутрішнього басейну 3×3×3
        if (be.multiblockValid && level.getGameTime() % 10 == 0 && be.waterTank.getSpace() >= 250) {
            be.waterTank.fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
        }

        int hotSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack st = be.inventory.getStackInSlot(i);
            if (st.is(ModItems.DEPLETED_FUEL_ROD.get())) {
                hotSlot = i;
                break;
            }
        }

        boolean coolingActive = false;
        if (be.multiblockValid && hotSlot >= 0) {
            if (be.waterTank.getFluidAmount() >= 5) {
                boolean distilled = be.waterTank.getFluid().getFluid().isSame(ModFluids.DISTILLED_WATER_SOURCE.get());
                be.waterTank.drain(5, IFluidHandler.FluidAction.EXECUTE);
                be.coolProgress += distilled ? 2 : 1;
                coolingActive = true;
                if (level instanceof ServerLevel sl && level.getGameTime() % 5 == 0) {
                    Direction back = state.getValue(SpentFuelPoolBlock.FACING).getOpposite();
                    BlockPos waterCenter = pos.relative(back, 1).above(1);
                    sl.sendParticles(ParticleTypes.BUBBLE, waterCenter.getX() + 0.5, waterCenter.getY() + 0.4, waterCenter.getZ() + 0.5, 4, 0.25, 0.2, 0.25, 0.03);
                    sl.sendParticles(ParticleTypes.GLOW, waterCenter.getX() + 0.5, waterCenter.getY() + 0.7, waterCenter.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0.01);
                }
                if (be.coolProgress >= COOL_TIME) {
                    be.coolProgress = 0;
                    be.inventory.setStackInSlot(hotSlot, new ItemStack(ModItems.DEPLETED_URANIUM_INGOT.get()));
                    be.depositEmptyRod(hotSlot);
                }
            }
        } else {
            be.coolProgress = 0;
            if (!be.multiblockValid && hotSlot >= 0 && level instanceof ServerLevel sl && level.getGameTime() % 15 == 0) {
                sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
            }
        }

        if (state.hasProperty(SpentFuelPoolBlock.ACTIVE) && state.getValue(SpentFuelPoolBlock.ACTIVE) != coolingActive) {
            level.setBlock(pos, state.setValue(SpentFuelPoolBlock.ACTIVE, coolingActive), 3);
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("Cool", coolProgress);
        tag.put("Water", waterTank.writeToNBT(reg, new CompoundTag()));
        tag.put("Inv", inventory.serializeNBT(reg));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        coolProgress = tag.getInt("Cool");
        waterTank.readFromNBT(reg, tag.getCompound("Water"));
        inventory.deserializeNBT(reg, tag.getCompound("Inv"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.spent_fuel_pool");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new SpentFuelPoolMenu(id, inv, this, data);
    }
}
