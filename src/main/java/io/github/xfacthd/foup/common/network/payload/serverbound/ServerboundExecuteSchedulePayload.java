package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundExecuteSchedulePayload(int cartId) implements CustomPacketPayload
{
    public static final Type<ServerboundExecuteSchedulePayload> TYPE = Utils.payloadType("execute_schedule");
    public static final StreamCodec<ByteBuf, ServerboundExecuteSchedulePayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(ServerboundExecuteSchedulePayload::new, ServerboundExecuteSchedulePayload::cartId);

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().level().getEntity(cartId) instanceof OverheadCartEntity cart && cart.isUsableByPlayer(ctx.player()))
        {
            if (!cart.executeSchedule())
            {
                ctx.reply(ClientboundRefreshStaleSchedulePayload.of(cart, Schedule.RejectedAction.EXECUTE));
            }
        }
    }

    @Override
    public Type<ServerboundExecuteSchedulePayload> type()
    {
        return TYPE;
    }
}
