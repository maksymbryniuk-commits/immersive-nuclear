package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.RtgGeneratorBlockEntity;
import com.ienuclear.reactor.registry.ModBlockEntities;
import com.ienuclear.reactor.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Радіоізотопний термоелектричний генератор (РІТЕГ / RTG) для віддалених баз.
 */
public class RtgGeneratorBlock extends Block implements EntityBlock {
    public RtgGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RtgGeneratorBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RtgGeneratorBlockEntity be) {
            if (stack.is(ModItems.TRITIUM_CELL.get()) || stack.is(ModItems.PLUTONIUM_DUST.get())) {
                if (be.insertFuel(stack)) {
                    player.displayClientMessage(Component.literal("RTG Fuel Loaded! Output: " + be.getOutputFePerTick() + " FE/t"), true);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RtgGeneratorBlockEntity be) {
            player.displayClientMessage(Component.literal("RTG: " + be.getOutputFePerTick() + " FE/t | Stored: " + be.getEnergyStorage().getEnergyStored() + " FE"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.RTG_GENERATOR.get()
                ? (lvl, p, st, be) -> RtgGeneratorBlockEntity.serverTick(lvl, p, st, (RtgGeneratorBlockEntity) be)
                : null;
    }
}
