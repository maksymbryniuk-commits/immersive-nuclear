package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.NuclearWasteCaskBlockEntity;
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
 * Свинцево-бетонний контейнер для ядерних відходів (Nuclear Waste Cask).
 * Безпечний лише закопаним під землею (без доступу до неба)!
 */
public class NuclearWasteCaskBlock extends Block implements EntityBlock {
    public NuclearWasteCaskBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NuclearWasteCaskBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof NuclearWasteCaskBlockEntity be) {
            if (stack.is(ModItems.DEPLETED_FUEL_ROD.get()) || stack.is(ModItems.PLUTONIUM_DUST.get())) {
                if (be.addWaste(stack)) {
                    player.displayClientMessage(Component.literal("Waste stored: " + be.getWasteCount() + " / 9"), true);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof NuclearWasteCaskBlockEntity be) {
            boolean buried = !level.canSeeSky(pos.above());
            player.displayClientMessage(Component.literal(
                    "Waste Cask: " + be.getWasteCount() + "/9 | " + (buried ? "BURIED (SAFE)" : "EXPOSED TO SKY (LEAKING!)")
            ), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.NUCLEAR_WASTE_CASK.get()
                ? (lvl, p, st, be) -> NuclearWasteCaskBlockEntity.serverTick(lvl, p, st, (NuclearWasteCaskBlockEntity) be)
                : null;
    }
}
