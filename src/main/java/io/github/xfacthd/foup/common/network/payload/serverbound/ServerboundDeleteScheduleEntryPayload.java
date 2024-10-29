package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ServerboundDeleteScheduleEntryPayload(int cartId, int idx, UUID entryUid) implements CustomPacketPayload
{
    public static final Type<ServerboundDeleteScheduleEntryPayload> TYPE = Utils.payloadType("delete_schedule_entry");
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDeleteScheduleEntryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundDeleteScheduleEntryPayload::cartId,
            ByteBufCodecs.VAR_INT,
            ServerboundDeleteScheduleEntryPayload::idx,
            UUIDUtil.STREAM_CODEC,
            ServerboundDeleteScheduleEntryPayload::entryUid,
            ServerboundDeleteScheduleEntryPayload::new
    );

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().level().getEntity(cartId) instanceof OverheadCartEntity cart && cart.isUsableByPlayer(ctx.player()))
        {
            if (!cart.getSchedule().removeEntry(idx, entryUid))
            {
                ctx.reply(ClientboundRefreshStaleSchedulePayload.of(cart, Schedule.RejectedAction.DELETE));
            }
        }
    }

    @Override
    public Type<ServerboundDeleteScheduleEntryPayload> type()
    {
        return TYPE;
    }
}
