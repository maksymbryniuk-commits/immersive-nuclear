package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.menu.SpentFuelPoolMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Охолоджувальний басейн на 9 слотів: охолоджує відпрацьовані стрижні у воді перед переробкою.
 */
public class SpentFuelPoolBlockEntity extends BlockEntity implements MenuProvider {
    private int coolProgress = 0;
    public static final int COOL_TIME = 200;

    private final FluidTank waterTank = new FluidTank(10_000, fs ->
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
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 2; }
    };

    public SpentFuelPoolBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPENT_FUEL_POOL.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public FluidTank getWaterTank() { return waterTank; }
    public ContainerData getData() { return data; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpentFuelPoolBlockEntity be) {
        // Автоматичний забір води з сусідніх джерел води
        if (level.getGameTime() % 20 == 0 && be.waterTank.getSpace() >= 500) {
            for (Direction d : Direction.values()) {
                if (level.getFluidState(pos.relative(d)).is(Fluids.WATER)) {
                    be.waterTank.fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE);
                    break;
                }
            }
        }

        int hotSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack st = be.inventory.getStackInSlot(i);
            if (st.is(ModItems.DEPLETED_FUEL_ROD.get())) {
                hotSlot = i;
                break;
            }
        }

        if (hotSlot >= 0) {
            if (be.waterTank.getFluidAmount() >= 5) {
                be.waterTank.drain(5, IFluidHandler.FluidAction.EXECUTE);
                be.coolProgress++;
                if (be.coolProgress >= COOL_TIME) {
                    be.coolProgress = 0;
                    be.inventory.setStackInSlot(hotSlot, new ItemStack(ModItems.DEPLETED_URANIUM_INGOT.get()));
                }
            } else if (level instanceof ServerLevel sl && level.getGameTime() % 10 == 0) {
                sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.02);
            }
        } else {
            be.coolProgress = 0;
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
