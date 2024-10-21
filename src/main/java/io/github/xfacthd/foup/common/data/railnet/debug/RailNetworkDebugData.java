package io.github.xfacthd.foup.common.data.railnet.debug;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;

public record RailNetworkDebugData(List<Node> nodes)
{
    public static final StreamCodec<ByteBuf, RailNetworkDebugData> STREAM_CODEC = Node.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(RailNetworkDebugData::new, RailNetworkDebugData::nodes);

    public record Node(BlockPos pos, Optional<String> stationName, boolean occupied, List<BlockPos> neighbours)
    {
        private static final StreamCodec<ByteBuf, Node> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                Node::pos,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                Node::stationName,
                ByteBufCodecs.BOOL,
                Node::occupied,
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
                Node::neighbours,
                Node::new
        );
    }
}
