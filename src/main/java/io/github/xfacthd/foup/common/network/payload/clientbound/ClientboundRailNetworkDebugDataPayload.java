package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugData;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;

public record ClientboundRailNetworkDebugDataPayload(long networkId, Optional<RailNetworkDebugData> data) implements CustomPacketPayload {
    public static final Type<ClientboundRailNetworkDebugDataPayload> TYPE = Utils.payloadType("rail_network_debug_data");
    public static final StreamCodec<ByteBuf, ClientboundRailNetworkDebugDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ClientboundRailNetworkDebugDataPayload::networkId,
            ByteBufCodecs.optional(RailNetworkDebugData.STREAM_CODEC),
            ClientboundRailNetworkDebugDataPayload::data,
            ClientboundRailNetworkDebugDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
