package io.github.xfacthd.foup.common.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum RenameResult
{
    SUCCESS,
    NAME_INVALID,
    NAME_TAKEN,
    UNKNOWN;

    private static final IntFunction<RenameResult> BY_ID = ByIdMap.continuous(RenameResult::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RenameResult> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RenameResult::ordinal);
}
