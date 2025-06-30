package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ClientboundRefreshStaleSchedulePayload(
        int cartId,
        Schedule.RejectedAction rejectedAction,
        List<Schedule.Entry> scheduleEntries,
        Map<String, StationType> stations
) implements CustomPacketPayload
{
    public static final Type<ClientboundRefreshStaleSchedulePayload> TYPE = Utils.payloadType("refresh_state_schedule");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRefreshStaleSchedulePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundRefreshStaleSchedulePayload::cartId,
            Schedule.RejectedAction.STREAM_CODEC,
            ClientboundRefreshStaleSchedulePayload::rejectedAction,
            Schedule.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ClientboundRefreshStaleSchedulePayload::scheduleEntries,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, StationType.STREAM_CODEC),
            ClientboundRefreshStaleSchedulePayload::stations,
            ClientboundRefreshStaleSchedulePayload::new
    );

    @Override
    public Type<ClientboundRefreshStaleSchedulePayload> type()
    {
        return TYPE;
    }

    public static ClientboundRefreshStaleSchedulePayload of(OverheadCartEntity cart, Schedule.RejectedAction action)
    {
        return new ClientboundRefreshStaleSchedulePayload(cart.getId(), action, cart.getSchedule().getEntriesCopy(), cart.getAvailableStations());
    }
}
