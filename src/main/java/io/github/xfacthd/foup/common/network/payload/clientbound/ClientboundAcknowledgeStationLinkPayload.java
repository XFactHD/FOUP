package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.common.network.FoupStreamCodecs;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.TriState;

public record ClientboundAcknowledgeStationLinkPayload(BlockPos pos, TriState result) implements CustomPacketPayload {
    public static final Type<ClientboundAcknowledgeStationLinkPayload> TYPE = Utils.payloadType("ack_station_link");
    public static final StreamCodec<ByteBuf, ClientboundAcknowledgeStationLinkPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundAcknowledgeStationLinkPayload::pos,
            FoupStreamCodecs.TRI_STATE,
            ClientboundAcknowledgeStationLinkPayload::result,
            ClientboundAcknowledgeStationLinkPayload::new
    );

    @Override
    public Type<ClientboundAcknowledgeStationLinkPayload> type() {
        return TYPE;
    }
}
