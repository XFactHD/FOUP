package io.github.xfacthd.foup.common.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;

public final class FoupCodecs {
    public static final Codec<BlockPos> POS_AS_LONG = Codec.LONG.xmap(BlockPos::of, BlockPos::asLong);

    private FoupCodecs() { }
}
