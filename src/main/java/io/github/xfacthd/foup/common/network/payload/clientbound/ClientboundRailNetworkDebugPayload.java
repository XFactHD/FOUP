package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.client.renderer.debug.RailNetworkDebugRenderer;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugData;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record ClientboundRailNetworkDebugPayload(long networkId, Optional<RailNetworkDebugData> data) implements CustomPacketPayload
{
    public static final Type<ClientboundRailNetworkDebugPayload> TYPE = Utils.payloadType("rail_network_debug");
    public static final StreamCodec<ByteBuf, ClientboundRailNetworkDebugPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ClientboundRailNetworkDebugPayload::networkId,
            ByteBufCodecs.optional(RailNetworkDebugData.STREAM_CODEC),
            ClientboundRailNetworkDebugPayload::data,
            ClientboundRailNetworkDebugPayload::new
    );

    public void handle(@SuppressWarnings("unused") IPayloadContext ctx)
    {
        if (FMLEnvironment.dist.isClient())
        {
            RailNetworkDebugRenderer.handleData(networkId, data.orElse(null));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
