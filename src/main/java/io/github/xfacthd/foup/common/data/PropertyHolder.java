package io.github.xfacthd.foup.common.data;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class PropertyHolder
{
    public static final DirectionProperty FACING_HOR = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty OUTWARD = BooleanProperty.create("outward");

    private PropertyHolder() { }
}
