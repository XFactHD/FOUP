package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundOpenOverheadRailStationScreenPayload(BlockPos pos) implements CustomPacketPayload
{
    public static final Type<ClientboundOpenOverheadRailStationScreenPayload> TYPE = Utils.payloadType("open_rail_station_screen");
    public static final StreamCodec<ByteBuf, ClientboundOpenOverheadRailStationScreenPayload> STREAM_CODEC = BlockPos.STREAM_CODEC
            .map(ClientboundOpenOverheadRailStationScreenPayload::new, ClientboundOpenOverheadRailStationScreenPayload::pos);

    @Override
    public Type<ClientboundOpenOverheadRailStationScreenPayload> type()
    {
        return TYPE;
    }
}
