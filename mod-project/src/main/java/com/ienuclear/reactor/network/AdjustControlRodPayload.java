package com.ienuclear.reactor.network;

import com.ienuclear.reactor.IENuclearMod;
import com.ienuclear.reactor.blockentity.ReactorControllerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AdjustControlRodPayload(BlockPos pos, int delta) implements CustomPacketPayload {
    public static final Type<AdjustControlRodPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IENuclearMod.MODID, "adjust_control_rod"));

    public static final StreamCodec<ByteBuf, AdjustControlRodPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AdjustControlRodPayload::pos,
            ByteBufCodecs.VAR_INT, AdjustControlRodPayload::delta,
            AdjustControlRodPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(AdjustControlRodPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().isLoaded(payload.pos())
                        && player.blockPosition().distSqr(payload.pos()) <= 64.0
                        && player.level().getBlockEntity(payload.pos()) instanceof ReactorControllerBlockEntity reactor) {
                    reactor.adjustControlRods(payload.delta());
                }
            }
        });
    }
}
