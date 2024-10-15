package io.github.xfacthd.foup.common.data;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public enum TrackShape
{
    STRAIGHT_NORTH(Direction.NORTH, Direction.NORTH),
    STRAIGHT_SOUTH(Direction.SOUTH, Direction.SOUTH),
    STRAIGHT_EAST(Direction.EAST, Direction.EAST),
    STRAIGHT_WEST(Direction.WEST, Direction.WEST),
    CURVE_NORTH_EAST(Direction.NORTH, Direction.EAST),
    CURVE_NORTH_WEST(Direction.NORTH, Direction.WEST),
    CURVE_SOUTH_EAST(Direction.SOUTH, Direction.EAST),
    CURVE_SOUTH_WEST(Direction.SOUTH, Direction.WEST),
    CURVE_EAST_NORTH(Direction.EAST, Direction.NORTH),
    CURVE_EAST_SOUTH(Direction.EAST, Direction.SOUTH),
    CURVE_WEST_NORTH(Direction.WEST, Direction.NORTH),
    CURVE_WEST_SOUTH(Direction.WEST, Direction.SOUTH),
    ;

    private static final TrackShape[] SHAPES_BY_DIR_PAIR = computeShapesByDirPair();

    private final Direction entryDir;
    private final Direction exitDir;
    private final boolean straight;
    private final Vec3 entryPoint;
    private final Vec3 exitPoint;

    TrackShape(Direction entryDir, Direction exitDir)
    {
        this.entryDir = entryDir;
        this.exitDir = exitDir;
        this.entryPoint = computeEntryPoint(entryDir.getOpposite());
        this.exitPoint = computeEntryPoint(exitDir);
        this.straight = entryDir == exitDir;
    }

    public Direction getEntryDir()
    {
        return entryDir;
    }

    public Direction getExitDir()
    {
        return exitDir;
    }

    public boolean isStraight()
    {
        return straight;
    }

    public Vec3 getEntryPoint()
    {
        return entryPoint;
    }

    public Vec3 getExitPoint()
    {
        return exitPoint;
    }

    public static TrackShape byDirPair(Direction incoming, Direction outgoing)
    {
        TrackShape shape = SHAPES_BY_DIR_PAIR[dirPairIdx(incoming, outgoing)];
        if (shape == null)
        {
            throw new IllegalArgumentException("Invalid direction pair: incoming=" + incoming + ", outgoing=" + outgoing);
        }
        return shape;
    }

    private static Vec3 computeEntryPoint(Direction dir)
    {
        Vec3i normal = dir.getNormal();
        Direction.Axis axis = dir.getAxis();
        Direction.Axis perpAxis = dir.getClockWise().getAxis();
        double x = axis.choose(Math.clamp(normal.getX(), 0, 1), 0, 0) + perpAxis.choose(.5, 0, 0);
        double z = axis.choose(0, 0, Math.clamp(normal.getZ(), 0, 1)) + perpAxis.choose(0, 0, .5);
        return new Vec3(x, 0, z);
    }

    private static int dirPairIdx(Direction incoming, Direction outgoing)
    {
        return (incoming.get2DDataValue() << 2) | outgoing.get2DDataValue();
    }

    private static TrackShape[] computeShapesByDirPair()
    {
        TrackShape[] arr = new TrackShape[4 * 4];
        for (TrackShape shape : values())
        {
            arr[dirPairIdx(shape.entryDir, shape.exitDir)] = shape;
        }
        return arr;
    }
}
