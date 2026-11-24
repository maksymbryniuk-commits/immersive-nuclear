package com.ienuclear.reactor.blockentity;

import com.ienuclear.reactor.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity порту дистанційного керування: зберігає позицію контролера реактора.
 */
public class RemoteControlPortBlockEntity extends BlockEntity {
    private BlockPos controllerPos = null;

    public RemoteControlPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REMOTE_CONTROL_PORT.get(), pos, state);
    }

    public void setControllerPos(BlockPos pos) {
        this.controllerPos = pos;
        setChanged();
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }
}
