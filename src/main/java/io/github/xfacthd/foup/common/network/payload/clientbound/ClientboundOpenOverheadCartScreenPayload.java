package io.github.xfacthd.foup.common.network.payload.clientbound;

import io.github.xfacthd.foup.client.util.ClientAccess;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ClientboundOpenOverheadCartScreenPayload(int cartId, List<Schedule.Entry> scheduleEntries, Map<String, StationType> stations) implements CustomPacketPayload
{
    public static final Type<ClientboundOpenOverheadCartScreenPayload> TYPE = Utils.payloadType("open_cart_screen");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenOverheadCartScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundOpenOverheadCartScreenPayload::cartId,
            Schedule.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ClientboundOpenOverheadCartScreenPayload::scheduleEntries,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, StationType.STREAM_CODEC),
            ClientboundOpenOverheadCartScreenPayload::stations,
            ClientboundOpenOverheadCartScreenPayload::new
    );

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().level().getEntity(cartId) instanceof OverheadCartEntity cart)
        {
            ClientAccess.openCartScreen(cart, scheduleEntries, stations);
        }
    }

    @Override
    public Type<ClientboundOpenOverheadCartScreenPayload> type()
    {
        return TYPE;
    }
}
