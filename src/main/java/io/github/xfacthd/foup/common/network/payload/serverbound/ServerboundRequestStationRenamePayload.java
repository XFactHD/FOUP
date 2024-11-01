package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.RenameResult;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationRenamePayload;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundRequestStationRenamePayload(BlockPos pos, String name) implements CustomPacketPayload
{
    public static final Type<ServerboundRequestStationRenamePayload> TYPE = Utils.payloadType("request_station_rename");
    public static final StreamCodec<ByteBuf, ServerboundRequestStationRenamePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ServerboundRequestStationRenamePayload::pos,
            ByteBufCodecs.STRING_UTF8,
            ServerboundRequestStationRenamePayload::name,
            ServerboundRequestStationRenamePayload::new
    );

    @SuppressWarnings("deprecation")
    public void handle(IPayloadContext ctx)
    {
        Level level = ctx.player().level();
        RenameResult result = RenameResult.UNKNOWN;
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity station)
        {
            if (station.isUsableByPlayer(ctx.player()) && ctx.player().mayBuild())
            {
                result = station.setName(name);
            }
        }
        ctx.reply(new ClientboundAcknowledgeStationRenamePayload(pos, result));
    }

    @Override
    public Type<ServerboundRequestStationRenamePayload> type()
    {
        return TYPE;
    }
}
