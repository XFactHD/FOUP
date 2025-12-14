package io.github.xfacthd.foup.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public record OverheadCartIssue(Type type, Optional<String> detail)
{
    public static final StreamCodec<RegistryFriendlyByteBuf, OverheadCartIssue> STREAM_CODEC = StreamCodec.composite(
            Type.STREAM_CODEC,
            OverheadCartIssue::type,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            OverheadCartIssue::detail,
            OverheadCartIssue::new
    );
    public static final OverheadCartIssue NONE = new OverheadCartIssue(Type.NONE, Optional.empty());

    public enum Type implements StringRepresentable
    {
        NONE,
        EMPTY_SCHEDULE,
        TARGET_MISSING,
        TARGET_UNREACHABLE,
        TARGET_INVALID,
        MOVEMENT_ERROR,
        PATH_INVALID,
        ;

        private static final Map<String, Type> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(
                Type::getSerializedName, Function.identity()
        ));
        private static final StreamCodec<ByteBuf, Type> STREAM_CODEC = ByteBufCodecs.idMapper(
                ByIdMap.continuous(Type::ordinal, Type.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
                Type::ordinal
        );

        private final String name = toString().toLowerCase(Locale.ROOT);
        private final String translationKey = "msg.foup.overhead_cart.issue." + name;
        private final Component translation = Component.translatable(translationKey);

        public String getTranslationKey()
        {
            return translationKey;
        }

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
        public static Type byName(String name)
        {
            return LOOKUP.get(name);
        }
    }
}
