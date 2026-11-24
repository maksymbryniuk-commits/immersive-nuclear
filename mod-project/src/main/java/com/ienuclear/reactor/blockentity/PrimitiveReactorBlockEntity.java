package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.PrimitiveReactorBlock;
import com.ienuclear.reactor.menu.PrimitiveReactorMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PrimitiveReactorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int COOLANT_CAPACITY = 8000;
    public static final int ENERGY_CAPACITY = 250_000;
    public static final int MAX_EXTRACT = 4096;
    public static final int AMBIENT_TEMP = 20;
    public static final int MELTDOWN_TEMP = 1200;
    public static final int FUEL_SLOT = 0;
    public static final int DEPLETED_SLOT = 1;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == FUEL_SLOT) {
                return stack.is(ModItems.NUCLEAR_FUEL_ROD.get())
                        || stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get())
                        || stack.is(ModItems.MOX_FUEL_CAPSULE.get())
                        || stack.is(ModItems.THORIUM_FUEL_CAPSULE.get());
            }
            return false;
        }
    };

    private final FluidTank coolantTank = new FluidTank(COOLANT_CAPACITY, stack ->
            stack.getFluid().isSame(ModFluids.HEAVY_COOLANT_SOURCE.get())
                    || stack.getFluid().isSame(ModFluids.LIQUID_SODIUM_SOURCE.get())
                    || stack.getFluid().isSame(ModFluids.DISTILLED_WATER_SOURCE.get())
                    || stack.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final PrimitiveEnergyStorage energyStorage = new PrimitiveEnergyStorage(ENERGY_CAPACITY, MAX_EXTRACT, MAX_EXTRACT);

    private boolean active = false;
    private int temperature = AMBIENT_TEMP;
    private int fuelTicksRemaining = 0;
    private int maxFuelTicks = 0;
    private int currentFePerTick = 0;
    private int currentFuelMultiplier = 1;
    private boolean currentFuelIsThorium = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> temperature;
                case 1 -> coolantTank.getFluidAmount();
                case 2 -> COOLANT_CAPACITY;
                case 3 -> energyStorage.getEnergyStored() & 0xFFFF;
                case 4 -> (energyStorage.getEnergyStored() >> 16) & 0xFFFF;
                case 5 -> ENERGY_CAPACITY & 0xFFFF;
                case 6 -> (ENERGY_CAPACITY >> 16) & 0xFFFF;
                case 7 -> currentFePerTick;
                case 8 -> active ? 1 : 0;
                case 9 -> fuelTicksRemaining;
                case 10 -> maxFuelTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() { return 11; }
    };

    public PrimitiveReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMITIVE_REACTOR.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public FluidTank getCoolantTank() { return coolantTank; }
    public IEnergyStorage getEnergyStorage() { return energyStorage; }
    public ContainerData getData() { return data; }
    public int getCoolantAmount() { return coolantTank.getFluidAmount(); }

    public void toggleActive() {
        this.active = !this.active;
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition,
                    this.active ? SoundEvents.LEVER_CLICK : SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS, 0.7f, 1.0f);
            BlockState state = getBlockState();
            if (state.hasProperty(PrimitiveReactorBlock.ACTIVE)) {
                level.setBlock(worldPosition, state.setValue(PrimitiveReactorBlock.ACTIVE, this.active), 3);
            }
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrimitiveReactorBlockEntity reactor) {
        if (reactor.active) {
            if (reactor.fuelTicksRemaining <= 0) {
                reactor.tryConsumeFuel();
            }

            if (reactor.fuelTicksRemaining > 0) {
                reactor.fuelTicksRemaining--;
                boolean hasCoolant = reactor.coolantTank.getFluidAmount() > 0;

                if (hasCoolant) {
                    boolean sodium = reactor.coolantTank.getFluid().getFluid().isSame(ModFluids.LIQUID_SODIUM_SOURCE.get());
                    boolean heavy = reactor.coolantTank.getFluid().getFluid().isSame(ModFluids.HEAVY_COOLANT_SOURCE.get());
                    boolean dist = reactor.coolantTank.getFluid().getFluid().isSame(ModFluids.DISTILLED_WATER_SOURCE.get());
                    int cost = sodium ? 1 : (heavy ? 2 : (dist ? 4 : 6));
                    reactor.coolantTank.drain(cost, IFluidHandler.FluidAction.EXECUTE);
                    int targetTemp = sodium ? 340 : (heavy ? 420 : (dist ? 540 : 680));
                    if (reactor.temperature < targetTemp) {
                        reactor.temperature += 4;
                    } else if (reactor.temperature > targetTemp) {
                        reactor.temperature = Math.max(targetTemp, reactor.temperature - 5);
                    }
                } else {
                    // Без охолоджувача швидко перегрівається і вибухає!
                    reactor.temperature += (12 * reactor.currentFuelMultiplier);
                }

                if (reactor.temperature >= 850 && level.getGameTime() % 20 == 0) {
                    net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(pos).inflate(5.0);
                    for (Player p : level.getEntitiesOfClass(Player.class, box)) {
                        if (!p.isCreative() && !NuclearWasteCaskBlockEntity.hasFullHazmat(p)) {
                            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 60, 0));
                        }
                    }
                }

                if (reactor.temperature >= MELTDOWN_TEMP) {
                    level.destroyBlock(pos, false);
                    level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            6.0F, true, Level.ExplosionInteraction.BLOCK);
                    level.setBlock(pos.below(), ModBlocks.MOLTEN_CORIUM.get().defaultBlockState(), 3);
                    return;
                }

                int generated = reactor.currentFuelIsThorium ? 614 : (512 * reactor.currentFuelMultiplier);
                reactor.energyStorage.addInternalEnergy(generated);
                reactor.currentFePerTick = generated;

                if (reactor.fuelTicksRemaining == 0) {
                    ItemStack spent = reactor.currentFuelIsThorium
                            ? new ItemStack(ModItems.EMPTY_FUEL_ROD.get(), 1)
                            : new ItemStack(ModItems.DEPLETED_FUEL_ROD.get(), 1);
                    reactor.inventory.insertItem(DEPLETED_SLOT, spent, false);
                }
            } else {
                reactor.active = false;
                reactor.currentFePerTick = 0;
                if (state.hasProperty(PrimitiveReactorBlock.ACTIVE) && state.getValue(PrimitiveReactorBlock.ACTIVE)) {
                    level.setBlock(pos, state.setValue(PrimitiveReactorBlock.ACTIVE, false), 3);
                }
            }
        } else {
            reactor.currentFePerTick = 0;
            if (reactor.temperature > AMBIENT_TEMP && level.getGameTime() % 5 == 0) {
                reactor.temperature = Math.max(AMBIENT_TEMP, reactor.temperature - 2);
            }
        }

        // ВИДАЧА ЕНЕРГІЇ FE ВИКЛЮЧНО ЗВЕРХУ БЛОКУ (Direction.UP)!
        if (reactor.energyStorage.getEnergyStored() > 0) {
            BlockPos abovePos = pos.above();
            IEnergyStorage topTarget = level.getCapability(Capabilities.EnergyStorage.BLOCK, abovePos, Direction.DOWN);
            if (topTarget != null && topTarget.canReceive()) {
                int toSend = Math.min(reactor.energyStorage.getEnergyStored(), MAX_EXTRACT);
                int accepted = topTarget.receiveEnergy(toSend, false);
                if (accepted > 0) {
                    reactor.energyStorage.extractEnergy(accepted, false);
                }
            }
        }

        reactor.setChanged();
    }

    private void tryConsumeFuel() {
        ItemStack stack = inventory.getStackInSlot(FUEL_SLOT);
        if (stack.isEmpty()) return;
        ItemStack out = inventory.getStackInSlot(DEPLETED_SLOT);
        if (!out.isEmpty() && out.getCount() >= out.getMaxStackSize()) return;

        if (stack.is(ModItems.MOX_FUEL_CAPSULE.get())) {
            currentFuelMultiplier = 3;
            currentFuelIsThorium = false;
            maxFuelTicks = 3600;
            fuelTicksRemaining = 3600;
            stack.shrink(1);
        } else if (stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get())) {
            currentFuelMultiplier = 2;
            currentFuelIsThorium = false;
            maxFuelTicks = 4800;
            fuelTicksRemaining = 4800;
            stack.shrink(1);
        } else if (stack.is(ModItems.THORIUM_FUEL_CAPSULE.get())) {
            currentFuelMultiplier = 1;
            currentFuelIsThorium = true;
            maxFuelTicks = 5400;
            fuelTicksRemaining = 5400;
            stack.shrink(1);
        } else if (stack.is(ModItems.NUCLEAR_FUEL_ROD.get())) {
            currentFuelMultiplier = 1;
            currentFuelIsThorium = false;
            maxFuelTicks = 2400;
            fuelTicksRemaining = 2400;
            stack.shrink(1);
        }
    }

    public void dropInventory(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, container);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.primitive_reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PrimitiveReactorMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        coolantTank.writeToNBT(registries, tag);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putBoolean("Active", active);
        tag.putInt("Temperature", temperature);
        tag.putInt("FuelTicks", fuelTicksRemaining);
        tag.putInt("MaxFuelTicks", maxFuelTicks);
        tag.putInt("FuelMult", currentFuelMultiplier);
        tag.putBoolean("FuelThorium", currentFuelIsThorium);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        coolantTank.readFromNBT(registries, tag);
        energyStorage.setEnergy(tag.getInt("Energy"));
        active = tag.getBoolean("Active");
        temperature = tag.getInt("Temperature");
        fuelTicksRemaining = tag.getInt("FuelTicks");
        maxFuelTicks = tag.getInt("MaxFuelTicks");
        currentFuelMultiplier = Math.max(1, tag.getInt("FuelMult"));
        currentFuelIsThorium = tag.getBoolean("FuelThorium");
    }

    private static class PrimitiveEnergyStorage extends EnergyStorage {
        public PrimitiveEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        @Override
        public boolean canReceive() { return false; }

        public void addInternalEnergy(int amount) {
            this.energy = Math.min(capacity, this.energy + amount);
        }

        public void setEnergy(int value) {
            this.energy = Math.max(0, Math.min(capacity, value));
        }
    }
}
