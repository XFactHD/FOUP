package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundRailNetworkDebugClearPayload() implements CustomPacketPayload {
    public static final Type<ClientboundRailNetworkDebugClearPayload> TYPE = Utils.payloadType("rail_network_debug_clear");
    public static final ClientboundRailNetworkDebugClearPayload INSTANCE = new ClientboundRailNetworkDebugClearPayload();
    public static final StreamCodec<ByteBuf, ClientboundRailNetworkDebugClearPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ClientboundRailNetworkDebugClearPayload> type() {
        return TYPE;
    }
}
