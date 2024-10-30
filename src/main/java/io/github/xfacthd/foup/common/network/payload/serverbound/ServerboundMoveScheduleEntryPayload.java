package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.menu.OverheadCartMenu;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ServerboundMoveScheduleEntryPayload(int cartId, int idx, boolean down, UUID entryUid) implements CustomPacketPayload
{
    public static final Type<ServerboundMoveScheduleEntryPayload> TYPE = Utils.payloadType("move_schedule_entry");
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMoveScheduleEntryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundMoveScheduleEntryPayload::cartId,
            ByteBufCodecs.VAR_INT,
            ServerboundMoveScheduleEntryPayload::idx,
            ByteBufCodecs.BOOL,
            ServerboundMoveScheduleEntryPayload::down,
            UUIDUtil.STREAM_CODEC,
            ServerboundMoveScheduleEntryPayload::entryUid,
            ServerboundMoveScheduleEntryPayload::new
    );

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().containerMenu instanceof OverheadCartMenu menu && menu.getCart().getId() == cartId)
        {
            if (!menu.getCart().getSchedule().moveEntry(idx, down, entryUid))
            {
                ctx.reply(ClientboundRefreshStaleSchedulePayload.of(menu.getCart(), Schedule.RejectedAction.MOVE));
            }
        }
    }

    @Override
    public Type<ServerboundMoveScheduleEntryPayload> type()
    {
        return TYPE;
    }
}
