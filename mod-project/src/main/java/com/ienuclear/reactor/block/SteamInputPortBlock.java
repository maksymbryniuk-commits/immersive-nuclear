package com.ienuclear.reactor.block;

import com.ienuclear.reactor.blockentity.SteamInputPortBlockEntity;
import com.ienuclear.reactor.registry.ModFluids;
import com.ienuclear.reactor.registry.ModItems;
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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class SteamInputPortBlock extends BaseEntityBlock {
    public static final MapCodec<SteamInputPortBlock> CODEC = simpleCodec(SteamInputPortBlock::new);

    public SteamInputPortBlock(Properties properties) {
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
        return new SteamInputPortBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SteamInputPortBlockEntity port) {
            if (stack.is(ModItems.HOT_STEAM_BUCKET.get())) {
                int filled = port.getSteamTank().fill(
                        new FluidStack(ModFluids.HOT_STEAM_SOURCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if (filled == 1000) {
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8f, 1.1f);
                    player.displayClientMessage(Component.literal(
                            "Гаряча пара в турбіні: " + port.getSteamAmount() + " / " + SteamInputPortBlockEntity.CAPACITY + " mB"), true);
                    return ItemInteractionResult.CONSUME;
                }
            }
            player.displayClientMessage(Component.literal(
                    "Порт входу пари: " + port.getSteamAmount() + " / " + SteamInputPortBlockEntity.CAPACITY + " mB"), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
