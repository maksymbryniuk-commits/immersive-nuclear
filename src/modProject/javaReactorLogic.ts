import type { ModFileEntry } from './gradleAndConfig';

export const JAVA_REACTOR_FILES: ModFileEntry[] = [
  {
    path: 'src/main/java/com/ienuclear/reactor/block/ReactorControllerBlock.java',
    language: 'java',
    category: 'java',
    description: 'Блок контролера реактора: відкриває GUI, відображає стан активності, тікає мультиблок',
    content: `package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ReactorControllerBlock extends BaseEntityBlock {
    public static final MapCodec<ReactorControllerBlock> CODEC = simpleCodec(ReactorControllerBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ReactorControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReactorControllerBlockEntity controller) {
                if (!controller.validateMultiblock()) {
                    player.displayClientMessage(
                            Component.translatable("message.ienuclear.multiblock_incomplete"), true);
                }
                serverPlayer.openMenu(controller, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.REACTOR_CONTROLLER.get(),
                ReactorControllerBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReactorControllerBlockEntity controller) {
                controller.dropInventory(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/block/CoolantInputPortBlock.java',
    language: 'java',
    category: 'java',
    description: 'Вхідний порт охолоджувальної рідини (підтримує заливку відром і підключення труб Immersive Engineering)',
    content: `package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.CoolantInputPortBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class CoolantInputPortBlock extends BaseEntityBlock {
    public static final MapCodec<CoolantInputPortBlock> CODEC = simpleCodec(CoolantInputPortBlock::new);

    public CoolantInputPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (!level.isClientSide && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection())) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoolantInputPortBlockEntity(pos, state);
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/block/EnergyOutputPortBlock.java',
    language: 'java',
    category: 'java',
    description: 'Порт виходу FE енергії для підключення конекторів та дротів Immersive Engineering',
    content: `package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.EnergyOutputPortBlockEntity;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyOutputPortBlock extends BaseEntityBlock {
    public static final MapCodec<EnergyOutputPortBlock> CODEC = simpleCodec(EnergyOutputPortBlock::new);

    public EnergyOutputPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyOutputPortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.ENERGY_OUTPUT_PORT.get(),
                EnergyOutputPortBlockEntity::serverTick);
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/blockentity/CoolantInputPortBlockEntity.java',
    language: 'java',
    category: 'java',
    description: 'Резервуар охолоджувача на 16,000 mB із підтримкою Heavy Coolant та води',
    content: `package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CoolantInputPortBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16000;

    private final FluidTank fluidTank = new FluidTank(CAPACITY, stack ->
            stack.getFluid().isSame(ModFluids.HEAVY_COOLANT_SOURCE.get())
                    || stack.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public CoolantInputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOLANT_INPUT_PORT.get(), pos, state);
    }

    public IFluidHandler getFluidTank() {
        return fluidTank;
    }

    public int getFluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public boolean isHeavyCoolant() {
        FluidStack stack = fluidTank.getFluid();
        return !stack.isEmpty() && stack.getFluid().isSame(ModFluids.HEAVY_COOLANT_SOURCE.get());
    }

    public int drainCoolant(int maxDrain) {
        FluidStack drained = fluidTank.drain(maxDrain, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        fluidTank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidTank.readFromNBT(registries, tag);
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/blockentity/EnergyOutputPortBlockEntity.java',
    language: 'java',
    category: 'java',
    description: 'Буфер 1,000,000 FE з активною передачею до сусідніх конекторів та споживачів IE',
    content: `package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyOutputPortBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_EXTRACT = 16_384;

    private final ReactorEnergyStorage energyStorage = new ReactorEnergyStorage(CAPACITY, MAX_EXTRACT, MAX_EXTRACT);

    public EnergyOutputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_OUTPUT_PORT.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int insertGeneratedEnergy(int amount) {
        int added = energyStorage.addInternalEnergy(amount);
        if (added > 0) setChanged();
        return added;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyOutputPortBlockEntity port) {
        if (port.energyStorage.getEnergyStored() <= 0) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, dir.getOpposite());
            if (target != null && target.canReceive()) {
                int toSend = Math.min(port.energyStorage.getEnergyStored(), MAX_EXTRACT);
                int accepted = target.receiveEnergy(toSend, false);
                if (accepted > 0) {
                    port.energyStorage.extractEnergy(accepted, false);
                    port.setChanged();
                }
                if (port.energyStorage.getEnergyStored() <= 0) break;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setEnergy(tag.getInt("Energy"));
    }

    private static class ReactorEnergyStorage extends EnergyStorage {
        public ReactorEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        @Override
        public boolean canReceive() {
            return false; // Зовнішні дроти не можуть заряджати вихідний порт
        }

        public int addInternalEnergy(int amount) {
            int accepted = Math.min(capacity - energy, amount);
            energy += accepted;
            return accepted;
        }

        public void setEnergy(int value) {
            this.energy = Math.max(0, Math.min(capacity, value));
        }
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/blockentity/ReactorControllerBlockEntity.java',
    language: 'java',
    category: 'java',
    description: 'Серце мультиблоку 3×3×3: сканер структури, термодинаміка (20–1200°C), споживання палива/охолоджувача, генерація FE',
    content: `package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.ReactorControllerBlock;
import com.ienuclear.reactor.menu.ReactorControllerMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ReactorControllerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int AMBIENT_TEMP = 20;
    public static final int OPTIMAL_TEMP = 750;
    public static final int WARNING_TEMP = 1000;
    public static final int SCRAM_TEMP = 1150;
    public static final int MELTDOWN_TEMP = 1400;
    public static final int MAX_TEMP = 1600;
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
                        || stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get());
            }
            return false;
        }
    };

    private boolean active = false;
    private boolean multiblockValid = false;
    private int temperature = AMBIENT_TEMP;
    private int fuelTicksRemaining = 0;
    private int maxFuelTicks = 0;
    private int currentFePerTick = 0;
    private int currentFuelMultiplier = 1;

    @Nullable
    private BlockPos cachedCoolantPos = null;
    @Nullable
    private BlockPos cachedEnergyPos = null;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> temperature;
                case 1 -> getCoolantStored();
                case 2 -> CoolantInputPortBlockEntity.CAPACITY;
                case 3 -> getEnergyStored() & 0xFFFF;
                case 4 -> (getEnergyStored() >> 16) & 0xFFFF;
                case 5 -> EnergyOutputPortBlockEntity.CAPACITY & 0xFFFF;
                case 6 -> (EnergyOutputPortBlockEntity.CAPACITY >> 16) & 0xFFFF;
                case 7 -> currentFePerTick;
                case 8 -> active ? 1 : 0;
                case 9 -> multiblockValid ? 1 : 0;
                case 10 -> fuelTicksRemaining;
                case 11 -> maxFuelTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Синхронізація лише server -> client
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    public ReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROLLER.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public void toggleActive() {
        if (!validateMultiblock()) {
            this.active = false;
        } else {
            this.active = !this.active;
        }
        updateBlockState();
        setChanged();
    }

    private void updateBlockState() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ReactorControllerBlock.ACTIVE)
                    && state.getValue(ReactorControllerBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(ReactorControllerBlock.ACTIVE, active), 3);
            }
        }
    }

    /**
     * Перевіряє 3x3x3 мультиблок позаду лицьової грані контролера:
     * - У центрі (1 блок вглиб): Reactor Core Rod
     * - На стінках: Reactor Casing, Reactor Glass, 1+ Coolant Input Port, 1+ Energy Output Port
     */
    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(ReactorControllerBlock.FACING);
        BlockPos center = worldPosition.relative(facing.getOpposite());

        if (!level.getBlockState(center).is(ModBlocks.REACTOR_CORE_ROD.get())) {
            multiblockValid = false;
            return false;
        }

        BlockPos foundCoolant = null;
        BlockPos foundEnergy = null;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (checkPos.equals(worldPosition)) continue;

                    BlockState bs = level.getBlockState(checkPos);
                    if (bs.is(ModBlocks.COOLANT_INPUT_PORT.get())) {
                        foundCoolant = checkPos;
                    } else if (bs.is(ModBlocks.ENERGY_OUTPUT_PORT.get())) {
                        foundEnergy = checkPos;
                    } else if (!bs.is(ModBlocks.REACTOR_CASING.get())
                            && !bs.is(ModBlocks.REACTOR_GLASS.get())) {
                        multiblockValid = false;
                        return false;
                    }
                }
            }
        }

        this.cachedCoolantPos = foundCoolant;
        this.cachedEnergyPos = foundEnergy;
        this.multiblockValid = (foundCoolant != null && foundEnergy != null);
        return this.multiblockValid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ReactorControllerBlockEntity reactor) {
        if (level.getGameTime() % 20 == 0) {
            reactor.validateMultiblock();
            if (!reactor.multiblockValid && reactor.active) {
                reactor.active = false;
                reactor.updateBlockState();
            }
        }

        CoolantInputPortBlockEntity coolantPort = reactor.getCoolantPort();
        EnergyOutputPortBlockEntity energyPort = reactor.getEnergyPort();

        if (reactor.active && reactor.multiblockValid && energyPort != null) {
            // Завантаження нового паливного стрижня якщо активний завершився
            if (reactor.fuelTicksRemaining <= 0) {
                reactor.tryConsumeNextFuelRod();
            }

            if (reactor.fuelTicksRemaining > 0) {
                reactor.fuelTicksRemaining--;

                boolean hasCoolant = (coolantPort != null && coolantPort.getFluidAmount() > 0);

                if (hasCoolant) {
                    boolean heavy = coolantPort.isHeavyCoolant();
                    int coolantCost = heavy ? 4 : 10;
                    int drained = coolantPort.drainCoolant(coolantCost);
                    int coolingPower = heavy ? 14 : 6;
                    int normalHeat = reactor.currentFuelMultiplier == 2 ? 10 : 6;

                    if (drained > 0) {
                        reactor.temperature = Math.max(AMBIENT_TEMP + 80, reactor.temperature + normalHeat - coolingPower);
                    } else {
                        // Витік охолоджувача — стрімкий тепловий розгін
                        reactor.temperature += (reactor.currentFuelMultiplier == 2 ? 30 : 20);
                    }
                } else {
                    // РЕАЛІСТИЧНИЙ ТЕПЛОВИЙ РОЗГІН: БЕЗ ОХОЛОДЖУВАЧА РЕАКТОР СТРІМКО НАБИРАЄ ТЕМПЕРАТУРУ!
                    int runawayHeat = reactor.currentFuelMultiplier == 2 ? 35 : 22;
                    reactor.temperature = Math.min(MAX_TEMP, reactor.temperature + runawayHeat);
                }

                // КАТАСТРОФІЧНИЙ ВИБУХ (MELTDOWN) ПРИ 1400°C!
                if (reactor.temperature >= MELTDOWN_TEMP) {
                    Direction facing = state.getValue(ReactorControllerBlock.FACING);
                    BlockPos center = pos.relative(facing.getOpposite());
                    reactor.triggerNuclearMeltdown(level, pos, center);
                    return;
                }

                // Автоматичний захисний SCRAM при 1150°C, якщо охолоджувач все ще циркулює
                if (reactor.temperature >= SCRAM_TEMP && hasCoolant) {
                    reactor.active = false;
                    reactor.currentFePerTick = 0;
                    reactor.updateBlockState();
                } else {
                    int baseFe = 2048 * reactor.currentFuelMultiplier;
                    double tempEfficiency = Math.min(1.6, 0.5 + (reactor.temperature / 650.0));
                    int generated = (int) (baseFe * tempEfficiency);
                    energyPort.insertGeneratedEnergy(generated);
                    reactor.currentFePerTick = generated;
                }

                if (reactor.fuelTicksRemaining == 0) {
                    reactor.outputDepletedRod();
                }
            } else {
                // Немає палива — зупиняємо реакцію
                reactor.active = false;
                reactor.currentFePerTick = 0;
                reactor.updateBlockState();
            }
        } else {
            reactor.currentFePerTick = 0;
            if (reactor.temperature > AMBIENT_TEMP && level.getGameTime() % 5 == 0) {
                reactor.temperature = Math.max(AMBIENT_TEMP, reactor.temperature - 1);
            }
        }

        reactor.setChanged();
    }

    private void tryConsumeNextFuelRod() {
        ItemStack fuelStack = inventory.getStackInSlot(FUEL_SLOT);
        if (fuelStack.isEmpty()) return;

        ItemStack depletedOut = inventory.getStackInSlot(DEPLETED_SLOT);
        if (!depletedOut.isEmpty() && depletedOut.getCount() >= depletedOut.getMaxStackSize()) {
            return; // Вихідний слот заповнений
        }

        if (fuelStack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get())) {
            this.currentFuelMultiplier = 2;
            this.maxFuelTicks = 4800; // 4 хв роботи
            this.fuelTicksRemaining = 4800;
            fuelStack.shrink(1);
        } else if (fuelStack.is(ModItems.NUCLEAR_FUEL_ROD.get())) {
            this.currentFuelMultiplier = 1;
            this.maxFuelTicks = 2400; // 2 хв роботи
            this.fuelTicksRemaining = 2400;
            fuelStack.shrink(1);
        }
    }

    private void outputDepletedRod() {
        ItemStack depleted = new ItemStack(ModItems.DEPLETED_FUEL_ROD.get(), 1);
        inventory.insertItem(DEPLETED_SLOT, depleted, false);
    }

    /**
     * Катастрофічний ядерний розгін (Meltdown) при досягненні 1400°C:
     * - Знищує мультиблок 3x3x3
     * - Створює потужний вибух силою 14.0F
     * - Випалює навколишню територію
     */
    private void triggerNuclearMeltdown(Level level, BlockPos pos, BlockPos center) {
        this.active = false;
        this.temperature = MAX_TEMP;
        updateBlockState();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    level.destroyBlock(p, false);
                }
            }
        }

        level.explode(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                14.0F, true, Level.ExplosionInteraction.BLOCK);
    }

    @Nullable
    private CoolantInputPortBlockEntity getCoolantPort() {
        if (level == null || cachedCoolantPos == null) return null;
        return level.getBlockEntity(cachedCoolantPos) instanceof CoolantInputPortBlockEntity port ? port : null;
    }

    @Nullable
    private EnergyOutputPortBlockEntity getEnergyPort() {
        if (level == null || cachedEnergyPos == null) return null;
        return level.getBlockEntity(cachedEnergyPos) instanceof EnergyOutputPortBlockEntity port ? port : null;
    }

    private int getCoolantStored() {
        CoolantInputPortBlockEntity port = getCoolantPort();
        return port != null ? port.getFluidAmount() : 0;
    }

    private int getEnergyStored() {
        EnergyOutputPortBlockEntity port = getEnergyPort();
        return port != null ? port.getEnergyStored() : 0;
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
        return Component.translatable("block.ienuclear.reactor_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReactorControllerMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putBoolean("Active", active);
        tag.putInt("Temperature", temperature);
        tag.putInt("FuelTicks", fuelTicksRemaining);
        tag.putInt("MaxFuelTicks", maxFuelTicks);
        tag.putInt("FuelMultiplier", currentFuelMultiplier);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        active = tag.getBoolean("Active");
        temperature = tag.getInt("Temperature");
        fuelTicksRemaining = tag.getInt("FuelTicks");
        maxFuelTicks = tag.getInt("MaxFuelTicks");
        currentFuelMultiplier = Math.max(1, tag.getInt("FuelMultiplier"));
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/menu/ReactorControllerMenu.java',
    language: 'java',
    category: 'java',
    description: 'Контейнер GUI зі слотами палива/відходів та синхронізацією температури, охолоджувача й FE енергії',
    content: `package com.ienuclear.reactor.menu;

import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ReactorControllerMenu extends AbstractContainerMenu {
    private final ReactorControllerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ReactorControllerMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv,
                (ReactorControllerBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(12));
    }

    public ReactorControllerMenu(int containerId, Inventory playerInv,
                                 ReactorControllerBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.REACTOR_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInv.player.level();
        this.data = data;

        // Слот 0: Паливний стрижень (вхід)
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), ReactorControllerBlockEntity.FUEL_SLOT, 64, 24));
        // Слот 1: Відпрацьований стрижень (вихід)
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), ReactorControllerBlockEntity.DEPLETED_SLOT, 64, 58) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
        addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public int getTemperature() { return data.get(0); }
    public int getCoolantAmount() { return data.get(1); }
    public int getCoolantCapacity() { return Math.max(1, data.get(2)); }

    public int getEnergyStored() {
        return (data.get(3) & 0xFFFF) | ((data.get(4) & 0xFFFF) << 16);
    }

    public int getMaxEnergyStored() {
        return Math.max(1, (data.get(5) & 0xFFFF) | ((data.get(6) & 0xFFFF) << 16));
    }

    public int getFePerTick() { return data.get(7); }
    public boolean isActive() { return data.get(8) == 1; }
    public boolean isMultiblockValid() { return data.get(9) == 1; }
    public int getFuelTicksRemaining() { return data.get(10); }
    public int getMaxFuelTicks() { return data.get(11); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.REACTOR_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < 2) {
            if (!this.moveItemStackTo(source, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 152));
        }
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/client/ReactorControllerScreen.java',
    language: 'java',
    category: 'java',
    description: 'Екран контролера реактора: візуалізація температури, рівня охолоджувача, FE буфера, слотів палива і перемикача Start/Stop',
    content: `package com.ienuclear.reactor.client;

import com.ienuclear.reactor.menu.ReactorControllerMenu;
import com.ienuclear.reactor.network.ToggleReactorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ReactorControllerScreen extends AbstractContainerScreen<ReactorControllerMenu> {
    private Button toggleButton;

    public ReactorControllerScreen(ReactorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 176;
        this.inventoryLabelY = 82;
    }

    @Override
    protected void init() {
        super.init();
        this.toggleButton = this.addRenderableWidget(
                Button.builder(getButtonLabel(), btn ->
                                PacketDistributor.sendToServer(new ToggleReactorPayload(menu.getBlockPos())))
                        .bounds(leftPos + 92, topPos + 54, 72, 20)
                        .build()
        );
    }

    private Component getButtonLabel() {
        return menu.isActive()
                ? Component.translatable("gui.ienuclear.stop")
                : Component.translatable("gui.ienuclear.start");
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Основна панель (індустріальний стиль IE)
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1F242D);
        gui.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2B313C);

        // Слоти палива
        drawSlotFrame(gui, x + 63, y + 23);
        drawSlotFrame(gui, x + 63, y + 57);

        // Стрілка вигоряння палива
        gui.fill(x + 69, y + 42, x + 75, y + 54, 0xFF161920);
        if (menu.getMaxFuelTicks() > 0 && menu.getFuelTicksRemaining() > 0) {
            int h = Math.max(1, (int) (12.0f * (1.0f - (float) menu.getFuelTicksRemaining() / menu.getMaxFuelTicks())));
            gui.fill(x + 69, y + 42, x + 75, y + 42 + h, 0xFF22C55E);
        }

        // 1. Шкала температури (лівий стовпець)
        int tempX = x + 14;
        int barY = y + 20;
        int barH = 56;
        gui.fill(tempX - 1, barY - 1, tempX + 13, barY + barH + 1, 0xFF11141A);
        float tempRatio = Math.min(1.0f, Math.max(0.0f, menu.getTemperature() / 1400.0f));
        int tempFill = (int) (barH * tempRatio);
        int tempColor = menu.getTemperature() >= 900 ? 0xFFEF4444
                : (menu.getTemperature() >= 550 ? 0xFFF59E0B : 0xFF10B981);
        if (tempFill > 0) {
            gui.fill(tempX, barY + barH - tempFill, tempX + 12, barY + barH, tempColor);
        }

        // 2. Рівень охолоджувальної рідини (другий стовпець)
        int coolX = x + 36;
        gui.fill(coolX - 1, barY - 1, coolX + 15, barY + barH + 1, 0xFF11141A);
        float coolRatio = Math.min(1.0f, (float) menu.getCoolantAmount() / menu.getCoolantCapacity());
        int coolFill = (int) (barH * coolRatio);
        if (coolFill > 0) {
            gui.fill(coolX, barY + barH - coolFill, coolX + 14, barY + barH, 0xFF06B6D4);
        }

        // 3. Буфер накопиченої енергії FE (правий стовпець)
        int feX = x + 150;
        gui.fill(feX - 1, barY - 1, feX + 13, barY + barH + 1, 0xFF11141A);
        float feRatio = Math.min(1.0f, (float) menu.getEnergyStored() / menu.getMaxEnergyStored());
        int feFill = (int) (barH * feRatio);
        if (feFill > 0) {
            gui.fill(feX, barY + barH - feFill, feX + 12, barY + barH, 0xFFF59E0B);
        }

        // Інвентар гравця (рамки слотів)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(gui, x + 7 + col * 18, y + 93 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(gui, x + 7 + col * 18, y + 151);
        }
    }

    private void drawSlotFrame(GuiGraphics gui, int x, int y) {
        gui.fill(x, y, x + 18, y + 18, 0xFF141820);
        gui.fill(x + 1, y + 1, x + 17, y + 17, 0xFF373F4D);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, this.title, 8, 6, 0xFFF59E0B, false);
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFFCBD5E1, false);

        String tempText = menu.getTemperature() + " C";
        String feRate = "+" + menu.getFePerTick() + " FE/t";
        String status = menu.isMultiblockValid()
                ? (menu.isActive() ? "ONLINE" : "READY")
                : "NO MULTIBLOCK";

        gui.drawString(this.font, tempText, 92, 22, 0xFFE2E8F0, false);
        gui.drawString(this.font, feRate, 92, 33, 0xFFFBBF24, false);
        gui.drawString(this.font, status, 92, 43, menu.isMultiblockValid() ? 0xFF34D399 : 0xFFF87171, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.toggleButton.setMessage(getButtonLabel());
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
        renderGaugeTooltips(gui, mouseX, mouseY);
    }

    private void renderGaugeTooltips(GuiGraphics gui, int mouseX, int mouseY) {
        int relX = mouseX - leftPos;
        int relY = mouseY - topPos;

        if (relX >= 14 && relX <= 26 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Температура: " + menu.getTemperature() + " °C / 1400 °C (Meltdown)"), mouseX, mouseY);
        } else if (relX >= 36 && relX <= 50 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Охолоджувач: " + menu.getCoolantAmount() + " / " + menu.getCoolantCapacity() + " mB"), mouseX, mouseY);
        } else if (relX >= 150 && relX <= 162 && relY >= 20 && relY <= 76) {
            gui.renderTooltip(this.font,
                    Component.literal("Енергія: " + menu.getEnergyStored() + " / " + menu.getMaxEnergyStored() + " FE"), mouseX, mouseY);
        }
    }
}
`
  },
  {
    path: 'src/main/java/com/ienuclear/reactor/client/ClientSetup.java',
    language: 'java',
    category: 'java',
    description: 'Клієнтська реєстрація екрана GUI та кольорів/текстур рідин',
    content: `package com.ienuclear.reactor.client;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = IENuclearMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NuclearManualIntegration::init);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.REACTOR_CONTROLLER_MENU.get(), ReactorControllerScreen::new);
    }

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFF00D8FF; }
        }, ModFluids.HEAVY_COOLANT_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFF84CC16; }
        }, ModFluids.URANIUM_HEXAFLUORIDE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
            @Override
            public int getTintColor() { return 0xFF10B981; }
        }, ModFluids.ENRICHED_URANIUM_TYPE.get());
    }
}
`
  }
];
