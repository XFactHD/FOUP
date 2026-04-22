package io.github.xfacthd.foup.common.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.function.IntFunction;

public enum StationAction implements StringRepresentable {
    LOAD,
    UNLOAD;

    private static final IntFunction<StationAction> BY_ID = ByIdMap.continuous(StationAction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<StationAction> CODEC = StringRepresentable.fromEnum(StationAction::values);
    public static final StreamCodec<ByteBuf, StationAction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, StationAction::ordinal);

    private final String name = toString().toLowerCase(Locale.ROOT);
    private final Component translation = Component.translatable("desc.foup.station_action." + name);

    @Override
    public String getSerializedName() {
        return name;
    }

    public Component getTranslation() {
        return translation;
    }

    public static StationAction byId(int id) {
        return BY_ID.apply(id);
    }
}
