package io.github.xfacthd.foup.common.data.component;

import com.mojang.serialization.Codec;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ScheduleSnapshot(List<Schedule.Entry> entries)
{
    public static final Codec<ScheduleSnapshot> CODEC = Schedule.Entry.CODEC.listOf()
            .xmap(ScheduleSnapshot::new, ScheduleSnapshot::entries);
    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleSnapshot> STREAM_CODEC = Schedule.Entry.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(ScheduleSnapshot::new, ScheduleSnapshot::entries);
    public static final ScheduleSnapshot EMPTY = new ScheduleSnapshot(List.of());
}
