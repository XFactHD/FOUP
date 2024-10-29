package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundAddScheduleEntryPayload(int cartId, int idx, Schedule.Entry entry) implements CustomPacketPayload
{
    public static final Type<ServerboundAddScheduleEntryPayload> TYPE = Utils.payloadType("add_schedule_entry");
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAddScheduleEntryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundAddScheduleEntryPayload::cartId,
            ByteBufCodecs.VAR_INT,
            ServerboundAddScheduleEntryPayload::idx,
            Schedule.Entry.STREAM_CODEC,
            ServerboundAddScheduleEntryPayload::entry,
            ServerboundAddScheduleEntryPayload::new
    );

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().level().getEntity(cartId) instanceof OverheadCartEntity cart && cart.isUsableByPlayer(ctx.player()))
        {
            if (!cart.getSchedule().addEntry(idx, entry))
            {
                ctx.reply(ClientboundRefreshStaleSchedulePayload.of(cart, Schedule.RejectedAction.ADD));
            }
        }
    }

    @Override
    public Type<ServerboundAddScheduleEntryPayload> type()
    {
        return TYPE;
    }
}
