package io.github.xfacthd.foup.common.data;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class PropertyHolder
{
    public static final DirectionProperty FACING_HOR = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty OUTWARD = BooleanProperty.create("outward");
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    public static final BooleanProperty LOCKER_FRONT_BL = BooleanProperty.create("front_bottom_left");
    public static final BooleanProperty LOCKER_FRONT_BR = BooleanProperty.create("front_bottom_right");
    public static final BooleanProperty LOCKER_FRONT_TL = BooleanProperty.create("front_top_left");
    public static final BooleanProperty LOCKER_FRONT_TR = BooleanProperty.create("front_top_right");
    public static final BooleanProperty LOCKER_BACK_BL = BooleanProperty.create("back_bottom_left");
    public static final BooleanProperty LOCKER_BACK_BR = BooleanProperty.create("back_bottom_right");
    public static final BooleanProperty LOCKER_BACK_TL = BooleanProperty.create("back_top_left");
    public static final BooleanProperty LOCKER_BACK_TR = BooleanProperty.create("back_top_right");
    public static final BooleanProperty[] LOCKER_PROPERTIES = new BooleanProperty[] {
            LOCKER_FRONT_BL,
            LOCKER_FRONT_BR,
            LOCKER_FRONT_TL,
            LOCKER_FRONT_TR,
            LOCKER_BACK_BL,
            LOCKER_BACK_BR,
            LOCKER_BACK_TL,
            LOCKER_BACK_TR
    };

    private PropertyHolder() { }
}
