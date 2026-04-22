package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.menu.OverheadCartMenu;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundEditScheduleEntryPayload(int cartId, int idx, Schedule.Entry entry) implements CustomPacketPayload {
    public static final Type<ServerboundEditScheduleEntryPayload> TYPE = Utils.payloadType("edit_schedule_entry");
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEditScheduleEntryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundEditScheduleEntryPayload::cartId,
            ByteBufCodecs.VAR_INT,
            ServerboundEditScheduleEntryPayload::idx,
            Schedule.Entry.STREAM_CODEC,
            ServerboundEditScheduleEntryPayload::entry,
            ServerboundEditScheduleEntryPayload::new
    );

    public void handle(IPayloadContext ctx) {
        if (ctx.player().containerMenu instanceof OverheadCartMenu menu && menu.getCart().getId() == cartId && ctx.player().mayBuild()) {
            if (!menu.getCart().getSchedule().updateEntry(idx, entry)) {
                ctx.reply(ClientboundRefreshStaleSchedulePayload.of(menu.getCart(), Schedule.RejectedAction.EDIT));
            }
        }
    }

    @Override
    public Type<ServerboundEditScheduleEntryPayload> type() {
        return TYPE;
    }
}
