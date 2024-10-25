package io.github.xfacthd.foup.common.network.payload.serverbound;

import io.github.xfacthd.foup.common.menu.FoupLoaderMenu;
import io.github.xfacthd.foup.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundToggleLoaderAutoEjectPayload(int containerId, boolean autoEject) implements CustomPacketPayload
{
    public static final Type<ServerboundToggleLoaderAutoEjectPayload> TYPE = Utils.payloadType("toggle_loader_auto_eject");
    public static final StreamCodec<ByteBuf, ServerboundToggleLoaderAutoEjectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundToggleLoaderAutoEjectPayload::containerId,
            ByteBufCodecs.BOOL,
            ServerboundToggleLoaderAutoEjectPayload::autoEject,
            ServerboundToggleLoaderAutoEjectPayload::new
    );

    public void handle(IPayloadContext ctx)
    {
        if (ctx.player().containerMenu instanceof FoupLoaderMenu menu && menu.containerId == containerId)
        {
            menu.setAutoEject(autoEject);
        }
    }

    @Override
    public Type<ServerboundToggleLoaderAutoEjectPayload> type()
    {
        return TYPE;
    }
}
