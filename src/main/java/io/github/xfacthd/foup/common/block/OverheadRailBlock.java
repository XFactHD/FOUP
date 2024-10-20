package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.RailType;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

public sealed class OverheadRailBlock extends AbstractOverheadRailBlock permits OverheadRailStationBlock
{
    public static final VoxelShape SHAPE_X = box(0, 9, 3, 16, 16, 13);
    public static final VoxelShape SHAPE_Z = box(3, 9, 0, 13, 16, 16);

    public OverheadRailBlock(Properties props)
    {
        super(props, RailType.STRAIGHT);
    }

    protected OverheadRailBlock(Properties props, RailType type)
    {
        super(props, type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(PropertyHolder.FACING_HOR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        Connection adjExit = findConnectionTowardsExit(level, pos, d -> true);
        Predicate<Direction> localEntry = adjExit == null || adjExit.entry() ? d -> true : d -> d == adjExit.dir().getOpposite();
        Connection adjEntry = findConnectionTowardsEntry(level, pos, localEntry);

        Direction dir;
        if (adjExit != null && !adjExit.entry())
        {
            dir = adjExit.dir().getOpposite();
            if (adjEntry != null && !adjEntry.entry())
            {
                // TODO: message
                return null;
            }
        }
        else if (adjEntry != null)
        {
            dir = adjEntry.dir();
        }
        else
        {
            dir = ctx.getHorizontalDirection();
        }
        return defaultBlockState().setValue(PropertyHolder.FACING_HOR, dir);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Utils.isX(state.getValue(PropertyHolder.FACING_HOR)) ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public boolean isEntrySide(BlockState state, Direction side)
    {
        return side == state.getValue(PropertyHolder.FACING_HOR).getOpposite();
    }

    @Override
    public boolean isExitSide(BlockState state, Direction side)
    {
        return side == state.getValue(PropertyHolder.FACING_HOR);
    }
}
