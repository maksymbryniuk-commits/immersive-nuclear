package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.RemoteControlPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Порт дистанційного керування на оболонці реактора.
 */
public class RemoteControlPortBlock extends Block implements EntityBlock {
    public RemoteControlPortBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemoteControlPortBlockEntity(pos, state);
    }
}
