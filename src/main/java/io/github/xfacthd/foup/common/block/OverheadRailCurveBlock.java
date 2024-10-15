package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.RailType;
import io.github.xfacthd.foup.common.util.ShapeUtils;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class OverheadRailCurveBlock extends AbstractOverheadRailBlock
{
    private static final VoxelShape SHAPE = ShapeUtils.orUnoptimized(box(3, 9, 0, 13, 16, 13), box(0, 9, 3, 13, 16, 13));
    private static final VoxelShape[] SHAPES = ShapeUtils.makeHorizontalRotations(SHAPE, Direction.NORTH);

    public OverheadRailCurveBlock(Properties props)
    {
        super(props, RailType.CURVE);
        registerDefaultState(defaultBlockState().setValue(PropertyHolder.RIGHT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(PropertyHolder.FACING_HOR, PropertyHolder.RIGHT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();

        Connection adjExit = findConnectionTowardsExit(level, pos, d -> true);
        Predicate<Direction> localEntry = adjExit == null || adjExit.entry() ? d -> true : d -> d == adjExit.dir().getClockWise() || d == adjExit.dir().getCounterClockWise();
        Connection adjEntry = findConnectionTowardsEntry(level, pos, localEntry);

        if (adjExit != null && !adjExit.entry())
        {
            if (adjEntry == null)
            {
                return getStateForPlacement(null, adjExit.dir().getOpposite(), face, ctx.getClickLocation());
            }
            if (!adjEntry.entry())
            {
                // TODO: message
                return null;
            }
            return defaultBlockState()
                    .setValue(PropertyHolder.FACING_HOR, adjEntry.dir())
                    .setValue(PropertyHolder.RIGHT, adjEntry.dir() == adjExit.dir().getCounterClockWise());
        }
        else if (adjEntry != null)
        {
            return getStateForPlacement(adjEntry.dir(), adjEntry.dir(), face, ctx.getClickLocation());
        }
        else
        {
            return getStateForPlacement(null, ctx.getHorizontalDirection(), face, ctx.getClickLocation());
        }
    }

    private BlockState getStateForPlacement(@Nullable Direction dir, Direction horDir, Direction face, Vec3 hitVec)
    {
        boolean right;
        if (face == horDir.getCounterClockWise())
        {
            right = true;
        }
        else if (face == horDir.getClockWise())
        {
            right = false;
        }
        else
        {
            right = Utils.fractionInDir(hitVec, horDir.getClockWise()) > .5;
        }
        if (dir == null)
        {
            dir = right ? horDir.getClockWise() : horDir.getCounterClockWise();
        }
        return defaultBlockState().setValue(PropertyHolder.FACING_HOR, dir).setValue(PropertyHolder.RIGHT, right);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        Direction dir = state.getValue(PropertyHolder.FACING_HOR);
        if (state.getValue(PropertyHolder.RIGHT))
        {
            dir = dir.getClockWise();
        }
        return SHAPES[dir.get2DDataValue()];
    }

    @Override
    public boolean isEntrySide(BlockState state, Direction side)
    {
        Direction dir = state.getValue(PropertyHolder.FACING_HOR);
        boolean right = state.getValue(PropertyHolder.RIGHT);
        return right ? side == dir.getClockWise() : side == dir.getCounterClockWise();
    }

    @Override
    public boolean isExitSide(BlockState state, Direction side)
    {
        return side == state.getValue(PropertyHolder.FACING_HOR);
    }
}
