package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.block.ReactorControllerBlock;
import com.ienuclear.reactor.menu.ReactorControllerMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Контролер масштабованого мультиблокового ядерного реактора (від 3x3x3 до 7x7x7):
 * - Вхід 1: Охолоджувач (Coolant Input Port) — стримує температуру активної зони
 * - Вхід 2: Вода (Water Input Port) — випаровується у гарячу пару
 * - Вихід: Гаряча пара (Steam Output Port) — подається по трубах у Парову турбіну
 * - Чим більше блоків Reactor Core Rod всередині (1..125), тим вища паропродуктивність!
 */
public class ReactorControllerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int AMBIENT_TEMP = 20;
    public static final int OPTIMAL_TEMP = 750;
    public static final int WARNING_TEMP = 950;
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
                        || stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get())
                        || stack.is(ModItems.MOX_FUEL_CAPSULE.get());
            }
            return false;
        }
    };

    private boolean active = false;
    private boolean multiblockValid = false;
    private int temperature = AMBIENT_TEMP;
    private int fuelTicksRemaining = 0;
    private int maxFuelTicks = 0;
    private int currentSteamPerTick = 0;
    private int currentFuelMultiplier = 1;
    private int controlRodInsertion = 0;
    private int coreCount = 0;

    private final List<BlockPos> cachedCoolantPorts = new ArrayList<>();
    private final List<BlockPos> cachedWaterPorts = new ArrayList<>();
    private final List<BlockPos> cachedSteamPorts = new ArrayList<>();
    @Nullable
    private BlockPos cachedCenterPos = null;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> temperature;
                case 1 -> Math.min(32767, getCoolantStored());
                case 2 -> Math.min(32767, getCoolantCapacity());
                case 3 -> Math.min(32767, getWaterStored());
                case 4 -> Math.min(32767, getWaterCapacity());
                case 5 -> Math.min(32767, getSteamStored());
                case 6 -> Math.min(32767, getSteamCapacity());
                case 7 -> currentSteamPerTick;
                case 8 -> active ? 1 : 0;
                case 9 -> multiblockValid ? 1 : 0;
                case 10 -> fuelTicksRemaining;
                case 11 -> maxFuelTicks;
                case 12 -> controlRodInsertion;
                case 13 -> coreCount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 14;
        }
    };

    public ReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROLLER.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }
    public int getTemperature() { return temperature; }
    public int getControlRodInsertion() { return controlRodInsertion; }
    public int getCurrentSteamPerTick() { return currentSteamPerTick; }
    public int getCoreCount() { return coreCount; }

    public double getRadiationLevel() {
        if (!active && temperature < 300) return 0.1;
        double base = active ? (1.2 * currentFuelMultiplier * Math.max(1, coreCount) * 0.4 * (1.0 - controlRodInsertion / 100.0)) : 0.2;
        if (temperature >= WARNING_TEMP) {
            base += (temperature - WARNING_TEMP) * 0.08;
        }
        return base;
    }

    public void adjustControlRods(int delta) {
        this.controlRodInsertion = Math.max(0, Math.min(100, this.controlRodInsertion + delta));
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5f, 1.3f);
        }
        setChanged();
    }

    public void triggerRedstoneScram() {
        if (this.active) {
            this.active = false;
            this.controlRodInsertion = 100;
            this.currentSteamPerTick = 0;
            updateBlockState();
            if (level != null && !level.isClientSide) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.9f, 0.8f);
            }
            setChanged();
        }
    }

    public void toggleActive() {
        if (!validateMultiblock()) {
            this.active = false;
        } else {
            this.active = !this.active;
            if (level != null && !level.isClientSide) {
                level.playSound(null, worldPosition,
                        this.active ? SoundEvents.LEVER_CLICK : SoundEvents.BEACON_DEACTIVATE,
                        SoundSource.BLOCKS, 0.8f, this.active ? 1.1f : 0.8f);
            }
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

    private boolean isShellBlock(BlockState bs) {
        return bs.is(ModBlocks.REACTOR_CASING.get())
                || bs.is(ModBlocks.REACTOR_GLASS.get())
                || bs.is(ModBlocks.COOLANT_INPUT_PORT.get())
                || bs.is(ModBlocks.WATER_INPUT_PORT.get())
                || bs.is(ModBlocks.STEAM_OUTPUT_PORT.get())
                || bs.is(ModBlocks.REACTOR_CONTROLLER.get());
    }

    /**
     * Динамічний сканер масштабованого мультиблоку від 3x3x3 до 7x7x7:
     * - Оболонка: Reactor Casing, Reactor Glass, 1+ Coolant Port, 1+ Water Port, 1+ Steam Port, 1 Controller
     * - Всередині: 1+ Reactor Core Rod (або повітря)
     */
    public boolean validateMultiblock() {
        if (level == null) return false;
        Direction facing = getBlockState().getValue(ReactorControllerBlock.FACING);
        Direction back = facing.getOpposite();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        int leftDist = 0;
        while (leftDist < 6 && isShellBlock(level.getBlockState(worldPosition.relative(left, leftDist + 1)))) {
            leftDist++;
        }
        int rightDist = 0;
        while (rightDist < 6 && isShellBlock(level.getBlockState(worldPosition.relative(right, rightDist + 1)))) {
            rightDist++;
        }
        int downDist = 0;
        while (downDist < 6 && isShellBlock(level.getBlockState(worldPosition.below(downDist + 1)))) {
            downDist++;
        }
        int upDist = 0;
        while (upDist < 6 && isShellBlock(level.getBlockState(worldPosition.above(upDist + 1)))) {
            upDist++;
        }

        BlockPos frontCorner = worldPosition.relative(left, leftDist).below(downDist);
        int backDist = 0;
        while (backDist < 6 && isShellBlock(level.getBlockState(frontCorner.relative(back, backDist + 1)))) {
            backDist++;
        }

        int width = leftDist + rightDist + 1;
        int height = downDist + upDist + 1;
        int depth = backDist + 1;

        if (width < 3 || width > 7 || height < 3 || height > 7 || depth < 3 || depth > 7) {
            multiblockValid = false;
            return false;
        }

        BlockPos cornerA = frontCorner;
        BlockPos cornerB = worldPosition.relative(right, rightDist).above(upDist).relative(back, backDist);

        int minX = Math.min(cornerA.getX(), cornerB.getX());
        int maxX = Math.max(cornerA.getX(), cornerB.getX());
        int minY = Math.min(cornerA.getY(), cornerB.getY());
        int maxY = Math.max(cornerA.getY(), cornerB.getY());
        int minZ = Math.min(cornerA.getZ(), cornerB.getZ());
        int maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

        List<BlockPos> foundCoolant = new ArrayList<>();
        List<BlockPos> foundWater = new ArrayList<>();
        List<BlockPos> foundSteam = new ArrayList<>();
        int foundCores = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    boolean onShell = (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ);
                    BlockState bs = level.getBlockState(p);

                    if (onShell) {
                        if (p.equals(worldPosition)) continue;
                        if (bs.is(ModBlocks.REACTOR_CONTROLLER.get())) {
                            // Тільки один контролер на мультиблок
                            multiblockValid = false;
                            return false;
                        } else if (bs.is(ModBlocks.COOLANT_INPUT_PORT.get())) {
                            foundCoolant.add(p);
                        } else if (bs.is(ModBlocks.WATER_INPUT_PORT.get())) {
                            foundWater.add(p);
                        } else if (bs.is(ModBlocks.STEAM_OUTPUT_PORT.get())) {
                            foundSteam.add(p);
                        } else if (!bs.is(ModBlocks.REACTOR_CASING.get()) && !bs.is(ModBlocks.REACTOR_GLASS.get())) {
                            multiblockValid = false;
                            return false;
                        }
                    } else {
                        if (bs.is(ModBlocks.REACTOR_CORE_ROD.get())) {
                            foundCores++;
                        } else if (!bs.isAir()) {
                            multiblockValid = false;
                            return false;
                        }
                    }
                }
            }
        }

        if (foundCores < 1 || foundCoolant.isEmpty() || foundWater.isEmpty() || foundSteam.isEmpty()) {
            multiblockValid = false;
            return false;
        }

        this.cachedCoolantPorts.clear();
        this.cachedCoolantPorts.addAll(foundCoolant);
        this.cachedWaterPorts.clear();
        this.cachedWaterPorts.addAll(foundWater);
        this.cachedSteamPorts.clear();
        this.cachedSteamPorts.addAll(foundSteam);
        this.coreCount = foundCores;
        this.cachedCenterPos = new BlockPos((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        this.multiblockValid = true;
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ReactorControllerBlockEntity reactor) {
        long gameTime = level.getGameTime();
        if (gameTime % 20 == 0) {
            reactor.validateMultiblock();
            if (!reactor.multiblockValid && reactor.active) {
                reactor.active = false;
                reactor.updateBlockState();
            }
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        Direction facing = state.getValue(ReactorControllerBlock.FACING);
        BlockPos center = reactor.cachedCenterPos != null ? reactor.cachedCenterPos : pos.relative(facing.getOpposite());
        double powerFactor = (100 - reactor.controlRodInsertion) / 100.0;
        double coreScale = 1.0 + Math.max(0, reactor.coreCount - 1) * 0.5;

        if (reactor.active && reactor.multiblockValid) {
            if (reactor.fuelTicksRemaining <= 0) {
                reactor.tryConsumeNextFuelRod();
            }

            if (reactor.fuelTicksRemaining > 0) {
                if (powerFactor > 0.0 && (powerFactor >= 0.75 || gameTime % 2 == 0)) {
                    reactor.fuelTicksRemaining--;
                }

                int totalCoolant = reactor.getCoolantStored();
                boolean hasCoolant = totalCoolant > 0;

                if (powerFactor <= 0.0) {
                    reactor.currentSteamPerTick = 0;
                    if (reactor.temperature > AMBIENT_TEMP && gameTime % 3 == 0) {
                        reactor.temperature = Math.max(AMBIENT_TEMP, reactor.temperature - 2);
                    }
                } else if (hasCoolant) {
                    boolean heavy = reactor.hasHeavyCoolant();
                    int baseCost = heavy ? 4 : 10;
                    int coolantCost = Math.max(1, (int) Math.round(baseCost * Math.sqrt(coreScale) * powerFactor));
                    int drained = reactor.drainCoolant(coolantCost);
                    int coolingPower = heavy ? 16 : 6;
                    int baseHeat = reactor.currentFuelMultiplier == 3 ? 15
                            : (reactor.currentFuelMultiplier == 2 ? 10 : 6);
                    int normalHeat = Math.max(1, (int) Math.round(baseHeat * Math.sqrt(coreScale) * powerFactor));

                    if (drained > 0) {
                        if (reactor.temperature < OPTIMAL_TEMP) {
                            reactor.temperature = Math.min(OPTIMAL_TEMP, reactor.temperature + Math.max(2, normalHeat / 2));
                        } else {
                            reactor.temperature = Math.max(AMBIENT_TEMP + 100, reactor.temperature + normalHeat - coolingPower);
                        }
                    } else {
                        int runaway = (int) Math.max(10, Math.round((reactor.currentFuelMultiplier * 14) * powerFactor));
                        reactor.temperature = Math.min(MAX_TEMP, reactor.temperature + runaway);
                    }
                } else {
                    // БЕЗ ОХОЛОДЖУВАЧА РЕАКТОР СТРІМКО ПЕРЕГРІВАЄТЬСЯ І ВИБУХАЄ!
                    int runawayHeat = (int) Math.max(12, Math.round((reactor.currentFuelMultiplier * 16 + 8) * powerFactor));
                    reactor.temperature = Math.min(MAX_TEMP, reactor.temperature + runawayHeat);
                }

                if (reactor.temperature >= WARNING_TEMP && level instanceof ServerLevel serverLevel) {
                    reactor.emitOverheatEffects(serverLevel, center, gameTime);
                }

                if (reactor.temperature >= MELTDOWN_TEMP) {
                    reactor.triggerNuclearMeltdown(level, pos, center);
                    return;
                }

                if (reactor.temperature >= SCRAM_TEMP && hasCoolant) {
                    reactor.active = false;
                    reactor.currentSteamPerTick = 0;
                    reactor.updateBlockState();
                    level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.7f);
                } else if (powerFactor > 0.0 && reactor.temperature >= 80) {
                    // КИП'ЯТІННЯ ВОДИ В ГАРЯЧУ ПАРУ (1 mB води -> 10 mB гарячої пари)
                    int desiredWater = (int) Math.max(1, Math.round(
                            8.0 * reactor.currentFuelMultiplier * coreScale * powerFactor * Math.min(1.6, reactor.temperature / 600.0)
                    ));
                    int waterUsed = reactor.drainWater(desiredWater);
                    int steamProduced = waterUsed * 10;
                    if (steamProduced > 0) {
                        reactor.insertSteam(steamProduced);
                    }
                    reactor.currentSteamPerTick = steamProduced;
                } else {
                    reactor.currentSteamPerTick = 0;
                }

                if (reactor.fuelTicksRemaining == 0) {
                    reactor.outputDepletedRod();
                }
            } else {
                reactor.active = false;
                reactor.currentSteamPerTick = 0;
                reactor.updateBlockState();
            }
        } else {
            reactor.currentSteamPerTick = 0;
            if (reactor.temperature > AMBIENT_TEMP && gameTime % 5 == 0) {
                reactor.temperature = Math.max(AMBIENT_TEMP, reactor.temperature - 1);
            }
        }

        reactor.setChanged();
    }

    private void emitOverheatEffects(ServerLevel serverLevel, BlockPos center, long gameTime) {
        if (gameTime % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5,
                    3, 0.5, 0.2, 0.5, 0.03);
            if (temperature >= SCRAM_TEMP) {
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        center.getX() + 0.5, center.getY() + 1.8, center.getZ() + 0.5,
                        2, 0.5, 0.3, 0.5, 0.05);
            }
        }

        if (gameTime % 20 == 0) {
            float pitch = temperature >= SCRAM_TEMP ? 1.8f : 1.2f;
            serverLevel.playSound(null, center, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0f, pitch);

            AABB radBox = new AABB(center).inflate(8.0);
            List<Player> nearby = serverLevel.getEntitiesOfClass(Player.class, radBox);
            for (Player player : nearby) {
                if (!player.isCreative() && !player.isSpectator() && !player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                    if (temperature >= SCRAM_TEMP) {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                    }
                }
                player.displayClientMessage(
                        Component.literal(String.format("⚠ [АВАРІЯ РЕАКТОРА] %d°C — РАДІАЦІЯ! Потрібен охолоджувач!", temperature))
                                .withStyle(temperature >= SCRAM_TEMP ? ChatFormatting.RED : ChatFormatting.GOLD),
                        true
                );
            }
        }
    }

    private void tryConsumeNextFuelRod() {
        ItemStack fuelStack = inventory.getStackInSlot(FUEL_SLOT);
        if (fuelStack.isEmpty()) return;
        ItemStack depletedOut = inventory.getStackInSlot(DEPLETED_SLOT);
        if (!depletedOut.isEmpty() && depletedOut.getCount() >= depletedOut.getMaxStackSize()) return;

        if (fuelStack.is(ModItems.MOX_FUEL_CAPSULE.get())) {
            this.currentFuelMultiplier = 3;
            this.maxFuelTicks = 3600;
            this.fuelTicksRemaining = 3600;
            fuelStack.shrink(1);
        } else if (fuelStack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get())) {
            this.currentFuelMultiplier = 2;
            this.maxFuelTicks = 4800;
            this.fuelTicksRemaining = 4800;
            fuelStack.shrink(1);
        } else if (fuelStack.is(ModItems.NUCLEAR_FUEL_ROD.get())) {
            this.currentFuelMultiplier = 1;
            this.maxFuelTicks = 2400;
            this.fuelTicksRemaining = 2400;
            fuelStack.shrink(1);
        }
    }

    private void outputDepletedRod() {
        inventory.insertItem(DEPLETED_SLOT, new ItemStack(ModItems.DEPLETED_FUEL_ROD.get(), 1), false);
    }

    private void triggerNuclearMeltdown(Level level, BlockPos pos, BlockPos center) {
        this.active = false;
        this.temperature = MAX_TEMP;
        updateBlockState();

        if (level instanceof ServerLevel serverLevel) {
            AABB alertBox = new AABB(center).inflate(48.0);
            for (Player player : serverLevel.getEntitiesOfClass(Player.class, alertBox)) {
                player.sendSystemMessage(Component.translatable("message.ienuclear.meltdown")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.destroyBlock(center.offset(dx, dy, dz), false);
                }
            }
        }

        float power = Math.min(24.0F, 12.0F + coreCount * 0.5F);
        level.explode(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                power, true, Level.ExplosionInteraction.BLOCK);

        BlockState corium = ModBlocks.MOLTEN_CORIUM.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 1) {
                    level.setBlock(center.offset(dx, -2, dz), corium, 3);
                }
            }
        }
    }

    // --- Агрегація кількох портів для великих реакторів (3x3x3..7x7x7) ---
    private int getCoolantStored() {
        if (level == null) return 0;
        int sum = 0;
        for (BlockPos p : cachedCoolantPorts) {
            if (level.getBlockEntity(p) instanceof CoolantInputPortBlockEntity port) sum += port.getFluidAmount();
        }
        return sum;
    }

    private int getCoolantCapacity() {
        return Math.max(1, cachedCoolantPorts.size() * CoolantInputPortBlockEntity.CAPACITY);
    }

    private boolean hasHeavyCoolant() {
        if (level == null) return false;
        for (BlockPos p : cachedCoolantPorts) {
            if (level.getBlockEntity(p) instanceof CoolantInputPortBlockEntity port && port.isHeavyCoolant()) return true;
        }
        return false;
    }

    private int drainCoolant(int amount) {
        if (level == null) return 0;
        int remaining = amount;
        for (BlockPos p : cachedCoolantPorts) {
            if (remaining <= 0) break;
            if (level.getBlockEntity(p) instanceof CoolantInputPortBlockEntity port) {
                remaining -= port.drainCoolant(remaining);
            }
        }
        return amount - remaining;
    }

    private int getWaterStored() {
        if (level == null) return 0;
        int sum = 0;
        for (BlockPos p : cachedWaterPorts) {
            if (level.getBlockEntity(p) instanceof WaterInputPortBlockEntity port) sum += port.getFluidAmount();
        }
        return sum;
    }

    private int getWaterCapacity() {
        return Math.max(1, cachedWaterPorts.size() * WaterInputPortBlockEntity.CAPACITY);
    }

    private int drainWater(int amount) {
        if (level == null) return 0;
        int remaining = amount;
        for (BlockPos p : cachedWaterPorts) {
            if (remaining <= 0) break;
            if (level.getBlockEntity(p) instanceof WaterInputPortBlockEntity port) {
                remaining -= port.drainWater(remaining);
            }
        }
        return amount - remaining;
    }

    private int getSteamStored() {
        if (level == null) return 0;
        int sum = 0;
        for (BlockPos p : cachedSteamPorts) {
            if (level.getBlockEntity(p) instanceof SteamOutputPortBlockEntity port) sum += port.getSteamStored();
        }
        return sum;
    }

    private int getSteamCapacity() {
        return Math.max(1, cachedSteamPorts.size() * SteamOutputPortBlockEntity.CAPACITY);
    }

    private void insertSteam(int amount) {
        if (level == null || amount <= 0) return;
        int remaining = amount;
        for (BlockPos p : cachedSteamPorts) {
            if (remaining <= 0) break;
            if (level.getBlockEntity(p) instanceof SteamOutputPortBlockEntity port) {
                remaining -= port.insertGeneratedSteam(remaining);
            }
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
        tag.putInt("ControlRods", controlRodInsertion);
        tag.putInt("CoreCount", coreCount);
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
        controlRodInsertion = Math.max(0, Math.min(100, tag.getInt("ControlRods")));
        coreCount = tag.getInt("CoreCount");
    }
}
