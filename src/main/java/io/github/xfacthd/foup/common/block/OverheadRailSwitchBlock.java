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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

public final class OverheadRailSwitchBlock extends AbstractOverheadRailBlock
{
    private static final VoxelShape SHAPE = ShapeUtils.orUnoptimized(box(3, 9, 0, 13, 16, 16), box(0, 9, 3, 13, 16, 13));
    private static final VoxelShape[] SHAPES = ShapeUtils.makeHorizontalRotations(SHAPE, Direction.NORTH);

    public OverheadRailSwitchBlock(Properties props)
    {
        super(props, RailType.SWITCH);
        registerDefaultState(defaultBlockState()
                .setValue(PropertyHolder.RIGHT, false)
                .setValue(PropertyHolder.OUTWARD, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(PropertyHolder.FACING_HOR, PropertyHolder.RIGHT, PropertyHolder.OUTWARD);
    }

    // FIXME: all orientations must be tested in case all three legs can connect in one of them
    // FIXME: a third leg being present must force the outward flag to the correct value
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        boolean outward = ctx.isSecondaryUseActive();

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

        Predicate<Direction> localCon = d -> d == dir.getClockWise() || d == d.getCounterClockWise();
        Connection secAdjCon = outward ? findConnectionTowardsEntry(level, pos, localCon) : findConnectionTowardsExit(level, pos, localCon);

        boolean right;
        if (secAdjCon != null)
        {
            if (secAdjCon.entry() != outward)
            {
                // TODO: message
                return null;
            }
            right = secAdjCon.dir() == dir.getClockWise();
        }
        else if (face == dir.getClockWise())
        {
            right = true;
        }
        else if (face == dir.getCounterClockWise())
        {
            right = false;
        }
        else
        {
            right = Utils.fractionInDir(ctx.getClickLocation(), dir.getClockWise()) > .5;
        }

        return defaultBlockState().setValue(PropertyHolder.FACING_HOR, dir)
                .setValue(PropertyHolder.RIGHT, right)
                .setValue(PropertyHolder.OUTWARD, outward);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        Direction dir = state.getValue(PropertyHolder.FACING_HOR);
        if (state.getValue(PropertyHolder.RIGHT))
        {
            dir = dir.getOpposite();
        }
        return SHAPES[dir.get2DDataValue()];
    }

    @Override
    public boolean isEntrySide(BlockState state, Direction side)
    {
        Direction dir = state.getValue(PropertyHolder.FACING_HOR);
        if (side == dir.getOpposite())
        {
            return true;
        }
        if (!state.getValue(PropertyHolder.OUTWARD))
        {
            boolean right = state.getValue(PropertyHolder.RIGHT);
            return right ? side == dir.getClockWise() : side == dir.getCounterClockWise();
        }
        return false;
    }

    @Override
    public boolean isExitSide(BlockState state, Direction side)
    {
        Direction dir = state.getValue(PropertyHolder.FACING_HOR);
        if (side == dir)
        {
            return true;
        }
        if (state.getValue(PropertyHolder.OUTWARD))
        {
            boolean right = state.getValue(PropertyHolder.RIGHT);
            return right ? side == dir.getClockWise() : side == dir.getCounterClockWise();
        }
        return false;
    }
}
