package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.EccsUnitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Система аварійного охолодження (ECCS): ставиться впритул до контролера реактора.
 */
public class EccsUnitBlock extends Block implements EntityBlock {
    public EccsUnitBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EccsUnitBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EccsUnitBlockEntity be) {
            if (stack.is(Items.WATER_BUCKET) && be.getTank().getSpace() >= 1000) {
                be.getTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                player.displayClientMessage(Component.literal("ECCS Coolant: " + be.getTank().getFluidAmount() + " / 16000 mB"), true);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EccsUnitBlockEntity be) {
            player.displayClientMessage(Component.literal("ECCS Tank: " + be.getTank().getFluidAmount() + " / 16000 mB"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
