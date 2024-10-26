package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.client.util.ClientAccess;
import io.github.xfacthd.foup.common.network.FoupStreamCodecs;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundAcknowledgeStationLinkPayload(BlockPos pos, TriState result) implements CustomPacketPayload
{
    public static final Type<ClientboundAcknowledgeStationLinkPayload> TYPE = Utils.payloadType("ack_station_link");
    public static final StreamCodec<ByteBuf, ClientboundAcknowledgeStationLinkPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundAcknowledgeStationLinkPayload::pos,
            FoupStreamCodecs.TRI_STATE,
            ClientboundAcknowledgeStationLinkPayload::result,
            ClientboundAcknowledgeStationLinkPayload::new
    );

    public void handle(@SuppressWarnings("unused") IPayloadContext ctx)
    {
        ClientAccess.handleStationLinkAck(pos, result);
    }

    @Override
    public Type<ClientboundAcknowledgeStationLinkPayload> type()
    {
        return TYPE;
    }
}
