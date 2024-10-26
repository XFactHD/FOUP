package io.github.xfacthd.foup.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.common.util.TriState;

public final class FoupStreamCodecs
{
    public static final StreamCodec<ByteBuf, TriState> TRI_STATE = ByteBufCodecs.idMapper(
            ByIdMap.continuous(TriState::ordinal, TriState.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
            TriState::ordinal
    );

    private FoupStreamCodecs() { }
}
