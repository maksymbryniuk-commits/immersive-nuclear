package com.ienuclear.reactor.network;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.blockentity.PrimitiveReactorBlockEntity;
import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleReactorPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleReactorPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "toggle_reactor"));

    public static final StreamCodec<ByteBuf, ToggleReactorPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ToggleReactorPayload::pos,
            ToggleReactorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(ToggleReactorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().isLoaded(payload.pos()) && player.blockPosition().distSqr(payload.pos()) <= 64.0) {
                    BlockEntity be = player.level().getBlockEntity(payload.pos());
                    if (be instanceof ReactorControllerBlockEntity reactor) {
                        reactor.toggleActive();
                    } else if (be instanceof PrimitiveReactorBlockEntity primitive) {
                        primitive.toggleActive();
                    }
                }
            }
        });
    }
}
