package io.github.xfacthd.foup.common.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StationType implements StringRepresentable
{
    UNKNOWN,
    LOADER,
    STORAGE,
    ;

    private static final StationType[] VALUES = values();
    private static final Map<String, StationType> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(t -> t.name, Function.identity()));
    public static final Codec<StationType> CODEC = StringRepresentable.fromEnum(StationType::values);
    public static final StreamCodec<ByteBuf, StationType> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(StationType::ordinal, VALUES, ByIdMap.OutOfBoundsStrategy.ZERO),
            StationType::ordinal
    );

    private final String name = toString().toLowerCase(Locale.ROOT);
    private final Component translation = Component.translatable("desc.foup.station_type." + name);

    public Component getTranslation()
    {
        return translation;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    @Nullable
    public static StationType byName(String name)
    {
        return LOOKUP.get(name);
    }

    @Nullable
    public static StationType byId(int id)
    {
        return id >= 0 && id < VALUES.length ? VALUES[id] : null;
    }
}
