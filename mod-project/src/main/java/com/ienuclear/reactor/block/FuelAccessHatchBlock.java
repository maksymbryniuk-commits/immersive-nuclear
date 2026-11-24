package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.FuelAccessHatchBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Верхній люк завантаження палива та IC2-пластин (6×3 сітка).
 * Розміщується на верхній кришці мультиблокового реактора (3×3×3 .. 7×7×7).
 */
public class FuelAccessHatchBlock extends BaseEntityBlock {
    public static final MapCodec<FuelAccessHatchBlock> CODEC = simpleCodec(FuelAccessHatchBlock::new);

    public FuelAccessHatchBlock(Properties properties) {
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
        return new FuelAccessHatchBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof FuelAccessHatchBlockEntity hatch) {
                serverPlayer.openMenu(hatch, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FuelAccessHatchBlockEntity hatch) {
                hatch.dropInventory(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
