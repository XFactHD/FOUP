package io.github.xfacthd.foup.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum OverheadCartState
{
    IDLE(false),
    MOVING(false),
    PARK_AFTER_ARRIVAL(false),
    LOWERING_HOIST(true),
    POD_IN_LOADER(true),
    POD_IN_STORAGE(true),
    RAISING_HOIST(true),
    PARK_BEFORE_DEPARTURE(false),
    PATHING(false),
    ;

    public static final IntFunction<OverheadCartState> BY_ID = ByIdMap.continuous(OverheadCartState::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, OverheadCartState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, OverheadCartState::ordinal);

    private final boolean movingHoist;

    OverheadCartState(boolean movingHoist)
    {
        this.movingHoist = movingHoist;
    }

    public boolean hasMovingHoist()
    {
        return movingHoist;
    }

    public static OverheadCartState of(int idx)
    {
        return BY_ID.apply(idx);
    }
}
