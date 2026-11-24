package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.menu.FuelAccessHatchMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
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

/**
 * Люк завантаження палива з сіткою 6×3 (18 слотів) у стилі IndustrialCraft 2:
 * - Підтримує паливні капсули з міцністю (після вичерпання стають Порожніми капсулами)
 * - Підтримує тепловідводи, реакторні пластини, теплообмінники та нейтронні відбивачі
 */
public class FuelAccessHatchBlockEntity extends BlockEntity implements MenuProvider {
    public static final int COLS = 6;
    public static final int ROWS = 3;
    public static final int SLOTS = COLS * ROWS;

    public record GridTickResult(int activeCapsules, double effectivePower, int netHeatDelta, int ventsCount) {}

    private int lastActiveCapsules = 0;
    private int lastVentsCount = 0;
    private int lastNetHeat = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFuelCapsule(stack) || isReactorPlate(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> lastActiveCapsules;
                case 1 -> lastVentsCount;
                case 2 -> lastNetHeat;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() { return 3; }
    };

    public FuelAccessHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUEL_ACCESS_HATCH.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }

    public static boolean isFuelCapsule(ItemStack stack) {
        return stack.is(ModItems.NUCLEAR_FUEL_ROD.get())
                || stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get())
                || stack.is(ModItems.MOX_FUEL_CAPSULE.get());
    }

    public static boolean isReactorPlate(ItemStack stack) {
        return stack.is(ModItems.HEAT_VENT.get())
                || stack.is(ModItems.REACTOR_PLATING.get())
                || stack.is(ModItems.HEAT_EXCHANGER.get())
                || stack.is(ModItems.NEUTRON_REFLECTOR.get());
    }

    private int[] getNeighbors(int slot) {
        int r = slot / COLS;
        int c = slot % COLS;
        int[] tmp = new int[4];
        int count = 0;
        if (r > 0) tmp[count++] = (r - 1) * COLS + c;
        if (r < ROWS - 1) tmp[count++] = (r + 1) * COLS + c;
        if (c > 0) tmp[count++] = r * COLS + (c - 1);
        if (c < COLS - 1) tmp[count++] = r * COLS + (c + 1);
        int[] res = new int[count];
        System.arraycopy(tmp, 0, res, 0, count);
        return res;
    }

    private void damageComponent(int slot, int amount) {
        ItemStack st = inventory.getStackInSlot(slot);
        if (st.isEmpty() || !st.isDamageableItem()) return;
        int next = st.getDamageValue() + amount;
        if (next >= st.getMaxDamage()) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            st.setDamageValue(next);
        }
    }

    /**
     * Обробляє 1 тік роботи сітки 6×3: рахує генерацію, тепло, взаємодію сусідніх слотів
     * і зношує міцність капсул (перетворюючи на Порожні капсули при вичерпанні).
     */
    public GridTickResult processGridTick(long gameTime, double powerFactor, int temperature) {
        int activeCapsules = 0;
        int vents = 0;
        double totalPower = 0.0;
        int totalHeat = 0;
        int totalCooling = 0;

        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            if (isFuelCapsule(stack)) {
                activeCapsules++;
                double basePower = stack.is(ModItems.MOX_FUEL_CAPSULE.get()) ? 3.0
                        : (stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get()) ? 2.0 : 1.0);
                int baseHeat = stack.is(ModItems.MOX_FUEL_CAPSULE.get()) ? 11
                        : (stack.is(ModItems.ENRICHED_NUCLEAR_FUEL_ROD.get()) ? 7 : 4);

                double adjBonus = 0.0;
                int heatMod = 0;
                for (int nb : getNeighbors(slot)) {
                    ItemStack nStack = inventory.getStackInSlot(nb);
                    if (nStack.isEmpty()) continue;
                    if (isFuelCapsule(nStack)) {
                        adjBonus += 0.25;
                        heatMod += 2;
                    } else if (nStack.is(ModItems.NEUTRON_REFLECTOR.get())) {
                        adjBonus += 0.35;
                        if (powerFactor > 0.0 && gameTime % 10 == 0) {
                            damageComponent(nb, 1);
                        }
                    } else if (nStack.is(ModItems.REACTOR_PLATING.get())) {
                        heatMod -= 2;
                        if (powerFactor > 0.0 && gameTime % 20 == 0) {
                            damageComponent(nb, 1);
                        }
                    }
                }

                totalPower += (basePower + adjBonus) * powerFactor;
                totalHeat += Math.max(1, (int) Math.round((baseHeat + heatMod) * powerFactor));

                // Знос міцності паливної капсули; при 0% -> перетворюється на Порожню капсулу!
                if (powerFactor > 0.0 && (powerFactor >= 0.75 || gameTime % 2 == 0)) {
                    int nextDmg = stack.getDamageValue() + 1;
                    if (nextDmg >= stack.getMaxDamage()) {
                        inventory.setStackInSlot(slot, new ItemStack(ModItems.EMPTY_FUEL_ROD.get()));
                    } else {
                        stack.setDamageValue(nextDmg);
                    }
                }
            } else if (stack.is(ModItems.HEAT_VENT.get())) {
                vents++;
                boolean hasExchanger = false;
                for (int nb : getNeighbors(slot)) {
                    if (inventory.getStackInSlot(nb).is(ModItems.HEAT_EXCHANGER.get())) {
                        hasExchanger = true;
                        if (temperature >= 350 && gameTime % 20 == 0) {
                            damageComponent(nb, 1);
                        }
                    }
                }
                totalCooling += hasExchanger ? 9 : 5;
                if (temperature >= 350 && gameTime % 20 == 0) {
                    damageComponent(slot, 1);
                }
            } else if (stack.is(ModItems.REACTOR_PLATING.get())) {
                totalCooling += 2;
            }
        }

        int netHeat = totalHeat - totalCooling;
        this.lastActiveCapsules = activeCapsules;
        this.lastVentsCount = vents;
        this.lastNetHeat = netHeat;
        setChanged();
        return new GridTickResult(activeCapsules, totalPower, netHeat, vents);
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
        return Component.translatable("block.ienuclear.fuel_access_hatch");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FuelAccessHatchMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }
}
