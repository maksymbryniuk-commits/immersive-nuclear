package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.menu.ControlDeskMenu;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * BlockEntity реакторного пульта керування: шукає контролер реактора по кабелях CONTROL_CABLE (до 64 блоків).
 */
public class ControlDeskBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_CABLE_SCAN = 256;
    private static final int MAX_CABLE_DIST = 64;

    private BlockPos linkedControllerPos = null;
    private int linked = 0;
    private int active = 0;
    private int temperature = 20;
    private int steamPerTick = 0;
    private int rods = 100;
    private int activeCapsules = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> linked;
                case 1 -> active;
                case 2 -> temperature;
                case 3 -> steamPerTick;
                case 4 -> rods;
                case 5 -> activeCapsules;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() { return 6; }
    };

    public ControlDeskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTROL_DESK.get(), pos, state);
    }

    public ContainerData getData() { return data; }

    @Nullable
    public ReactorControllerBlockEntity findLinkedReactor() {
        if (level == null) return null;
        if (linkedControllerPos != null && level.getBlockEntity(linkedControllerPos) instanceof ReactorControllerBlockEntity ctrl) {
            return ctrl;
        }
        // BFS по ControlCable до RemoteControlPort
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition);
        visited.add(worldPosition);

        while (!queue.isEmpty() && visited.size() < MAX_CABLE_SCAN) {
            BlockPos cur = queue.poll();
            if (cur.distManhattan(worldPosition) > MAX_CABLE_DIST) continue;

            for (Direction dir : Direction.values()) {
                BlockPos next = cur.relative(dir);
                if (!visited.add(next)) continue;

                BlockState bs = level.getBlockState(next);
                if (bs.is(ModBlocks.REMOTE_CONTROL_PORT.get())) {
                    if (level.getBlockEntity(next) instanceof RemoteControlPortBlockEntity port && port.getControllerPos() != null) {
                        linkedControllerPos = port.getControllerPos();
                        if (level.getBlockEntity(linkedControllerPos) instanceof ReactorControllerBlockEntity ctrl) {
                            return ctrl;
                        }
                    }
                } else if (bs.is(ModBlocks.CONTROL_CABLE.get())) {
                    queue.add(next);
                }
            }
        }
        linkedControllerPos = null;
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ControlDeskBlockEntity desk) {
        if (level.getGameTime() % 10 != 0) return;
        // Періодично оновлюємо посилання через кабель
        if (level.getGameTime() % 40 == 0) {
            desk.linkedControllerPos = null;
        }
        ReactorControllerBlockEntity ctrl = desk.findLinkedReactor();
        if (ctrl != null) {
            desk.linked = 1;
            desk.active = ctrl.isActive() ? 1 : 0;
            desk.temperature = ctrl.getTemperature();
            desk.steamPerTick = ctrl.getCurrentSteamPerTick();
            desk.rods = ctrl.getControlRodInsertion();
            desk.activeCapsules = ctrl.getActiveCapsulesCount();
        } else {
            desk.linked = 0;
            desk.active = 0;
            desk.temperature = 20;
            desk.steamPerTick = 0;
            desk.rods = 100;
            desk.activeCapsules = 0;
        }
        desk.setChanged();
    }

    public void handleButton(int buttonId) {
        ReactorControllerBlockEntity ctrl = findLinkedReactor();
        if (ctrl == null) return;
        switch (buttonId) {
            case 0 -> ctrl.toggleActive();
            case 1 -> ctrl.setControlRodInsertion(ctrl.getControlRodInsertion() - 10);
            case 2 -> ctrl.setControlRodInsertion(ctrl.getControlRodInsertion() + 10);
            case 3 -> {
                // AZ-5: Аварійний захист!
                ctrl.setControlRodInsertion(100);
                if (ctrl.isActive()) ctrl.toggleActive();
            }
            case 4 -> ctrl.triggerEccsFlood();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ienuclear.control_desk");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ControlDeskMenu(id, inv, this, this.data);
    }
}
