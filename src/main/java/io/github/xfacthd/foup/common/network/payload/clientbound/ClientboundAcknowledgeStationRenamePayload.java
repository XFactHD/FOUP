package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.common.data.RenameResult;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundAcknowledgeStationRenamePayload(BlockPos pos, RenameResult result) implements CustomPacketPayload
{
    public static final Type<ClientboundAcknowledgeStationRenamePayload> TYPE = Utils.payloadType("ack_station_rename");
    public static final StreamCodec<ByteBuf, ClientboundAcknowledgeStationRenamePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundAcknowledgeStationRenamePayload::pos,
            RenameResult.STREAM_CODEC,
            ClientboundAcknowledgeStationRenamePayload::result,
            ClientboundAcknowledgeStationRenamePayload::new
    );

    @Override
    public Type<ClientboundAcknowledgeStationRenamePayload> type()
    {
        return TYPE;
    }
}
