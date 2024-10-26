package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.client.util.ClientAccess;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundOpenOverheadRailStationPayload(BlockPos pos) implements CustomPacketPayload
{
    public static final Type<ClientboundOpenOverheadRailStationPayload> TYPE = Utils.payloadType("open_rail_station_screen");
    public static final StreamCodec<ByteBuf, ClientboundOpenOverheadRailStationPayload> STREAM_CODEC = BlockPos.STREAM_CODEC
            .map(ClientboundOpenOverheadRailStationPayload::new, ClientboundOpenOverheadRailStationPayload::pos);

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().level().getBlockEntity(pos) instanceof OverheadRailStationBlockEntity station)
        {
            ClientAccess.openStationScreen(pos, station);
        }
    }

    @Override
    public Type<ClientboundOpenOverheadRailStationPayload> type()
    {
        return TYPE;
    }
}
