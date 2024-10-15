package io.github.xfacthd.foup.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record OverheadCartAction(OverheadCartState state, int duration, int heightDiff)
{
    public static final StreamCodec<ByteBuf, OverheadCartAction> STREAM_CODEC = StreamCodec.composite(
            OverheadCartState.STREAM_CODEC,
            OverheadCartAction::state,
            ByteBufCodecs.VAR_INT,
            OverheadCartAction::duration,
            ByteBufCodecs.VAR_INT,
            OverheadCartAction::heightDiff,
            OverheadCartAction::new
    );
    public static final OverheadCartAction DEFAULT = new OverheadCartAction(OverheadCartState.IDLE, 0, 0);
}
