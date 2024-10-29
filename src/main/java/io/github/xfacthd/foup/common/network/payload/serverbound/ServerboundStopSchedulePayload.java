package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundStopSchedulePayload(int cartId) implements CustomPacketPayload
{
    public static final Type<ServerboundStopSchedulePayload> TYPE = Utils.payloadType("stop_schedule");
    public static final StreamCodec<ByteBuf, ServerboundStopSchedulePayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(ServerboundStopSchedulePayload::new, ServerboundStopSchedulePayload::cartId);

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().level().getEntity(cartId) instanceof OverheadCartEntity cart && cart.isUsableByPlayer(ctx.player()))
        {
            cart.stopSchedule();
        }
    }

    @Override
    public Type<ServerboundStopSchedulePayload> type()
    {
        return TYPE;
    }
}
