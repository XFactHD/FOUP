package io.github.xfacthd.foup.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.TriState;

import java.util.OptionalInt;

public final class FoupStreamCodecs
{
    public static final StreamCodec<ByteBuf, TriState> TRI_STATE = ByteBufCodecs.idMapper(
            ByIdMap.continuous(TriState::ordinal, TriState.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
            TriState::ordinal
    );

    public static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_VAR_INT = new StreamCodec<>()
    {
        @Override
        public OptionalInt decode(ByteBuf buffer)
        {
            if (buffer.readBoolean())
            {
                return OptionalInt.of(VarInt.read(buffer));
            }
            return OptionalInt.empty();
        }

        @Override
        public void encode(ByteBuf buffer, OptionalInt value)
        {
            boolean present = value.isPresent();
            buffer.writeBoolean(present);
            if (present)
            {
                VarInt.write(buffer, value.getAsInt());
            }
        }
    };

    private FoupStreamCodecs() { }
}
