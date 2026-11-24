package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.WaterInputPortBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class WaterInputPortBlock extends BaseEntityBlock {
    public static final MapCodec<WaterInputPortBlock> CODEC = simpleCodec(WaterInputPortBlock::new);

    public WaterInputPortBlock(Properties properties) {
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
        return new WaterInputPortBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof WaterInputPortBlockEntity port) {
            if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection())) {
                player.displayClientMessage(Component.literal(
                        "Вода для пари: " + port.getFluidAmount() + " / " + WaterInputPortBlockEntity.CAPACITY + " mB"), true);
                return ItemInteractionResult.CONSUME;
            }
            if (stack.is(Items.WATER_BUCKET)) {
                int filled = port.getFluidTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                if (filled == 1000) {
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8f, 1.0f);
                    player.displayClientMessage(Component.literal(
                            "Вода для пари: " + port.getFluidAmount() + " / " + WaterInputPortBlockEntity.CAPACITY + " mB"), true);
                    return ItemInteractionResult.CONSUME;
                }
            }
            player.displayClientMessage(Component.literal(
                    "Порт води: " + port.getFluidAmount() + " / " + WaterInputPortBlockEntity.CAPACITY + " mB"), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
