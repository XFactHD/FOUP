package io.github.xfacthd.foup.common.data;

import io.github.xfacthd.foup.common.FoupContent;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public enum RailType
{
    STRAIGHT(FoupContent.BE_TYPE_RAIL),
    CURVE(FoupContent.BE_TYPE_RAIL),
    SWITCH(FoupContent.BE_TYPE_RAIL),
    STATION(FoupContent.BE_TYPE_RAIL_STATION),
    ;

    private final Holder<BlockEntityType<?>> blockEntity;

    RailType(Holder<BlockEntityType<?>> blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    public BlockEntityType<?> getBlockEntity()
    {
        return blockEntity.value();
    }
}
