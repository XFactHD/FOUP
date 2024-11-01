package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationLinkPayload;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundRequestStationLinkPayload(BlockPos pos) implements CustomPacketPayload
{
    public static final Type<ServerboundRequestStationLinkPayload> TYPE = Utils.payloadType("request_station_link");
    public static final StreamCodec<ByteBuf, ServerboundRequestStationLinkPayload> STREAM_CODEC = BlockPos.STREAM_CODEC
            .map(ServerboundRequestStationLinkPayload::new, ServerboundRequestStationLinkPayload::pos);

    @SuppressWarnings("deprecation")
    public void handle(IPayloadContext ctx)
    {
        Level level = ctx.player().level();
        TriState result = TriState.FALSE;
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity station)
        {
            if (station.isUsableByPlayer(ctx.player()) && ctx.player().mayBuild())
            {
                result = station.tryLink();
            }
        }
        ctx.reply(new ClientboundAcknowledgeStationLinkPayload(pos, result));
    }

    @Override
    public Type<ServerboundRequestStationLinkPayload> type()
    {
        return TYPE;
    }
}
