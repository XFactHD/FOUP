package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class OverheadRailBlockEntity extends AbstractOverheadRailBlockEntity
{
    public OverheadRailBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(FoupContent.BE_TYPE_RAIL.value(), pos, blockState);
    }
}
