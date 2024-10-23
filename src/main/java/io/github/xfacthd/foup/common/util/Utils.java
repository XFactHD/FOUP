package io.github.xfacthd.foup.common.util;

import io.github.xfacthd.foup.Foup;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Utils
{
    private static final Long2ObjectMap<Direction> DIRECTION_BY_NORMAL = Arrays.stream(Direction.values())
            .collect(Collectors.toMap(
                    side -> new BlockPos(side.getNormal()).asLong(),
                    Function.identity(),
                    (sideA, sideB) -> { throw new IllegalArgumentException("Duplicate keys"); },
                    Long2ObjectOpenHashMap::new
            ));

    public static ResourceLocation rl(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(Foup.MOD_ID, path);
    }

    public static Vec3 fraction(Vec3 vec)
    {
        return new Vec3(Mth.frac(vec.x()), Mth.frac(vec.y()), Mth.frac(vec.z()));
    }

    /**
     * Calculate how far into the block the coordinate of the given direction's axis points in the given direction
     */
    public static double fractionInDir(Vec3 vec, Direction dir)
    {
        double coord = switch (dir.getAxis())
        {
            case X -> vec.x;
            case Y -> vec.y;
            case Z -> vec.z;
        };
        coord = Mth.frac(coord);
        return isPositive(dir) ? coord : (1D - coord);
    }

    public static boolean isPositive(Direction dir)
    {
        return dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
    }

    public static boolean isX(Direction dir)
    {
        return dir.getAxis() == Direction.Axis.X;
    }

    public static boolean isY(Direction dir)
    {
        return dir.getAxis() == Direction.Axis.Y;
    }

    public static boolean isZ(Direction dir)
    {
        return dir.getAxis() == Direction.Axis.Z;
    }

    public static Direction getDirByNormal(int x, int y, int z)
    {
        return DIRECTION_BY_NORMAL.get(BlockPos.asLong(x, y, z));
    }

    public static Direction getDirByNormal(BlockPos from, BlockPos to)
    {
        int nx = to.getX() - from.getX();
        int ny = to.getY() - from.getY();
        int nz = to.getZ() - from.getZ();
        return getDirByNormal(nx, ny, nz);
    }

    public static Direction getDirByViewVec(Entity entity)
    {
        Vec3 view = entity.getViewVector(1F);
        return Direction.getNearest(view);
    }

    public static Vec3 setAlongAxis(Vec3 vec, double value, Direction dir)
    {
        Direction.Axis axis = dir.getAxis();
        Direction.Axis perpAxis = dir.getClockWise().getAxis();
        double x = axis.choose(value, 0, 0) + perpAxis.choose(vec.x, 0, 0);
        double z = axis.choose(0, 0, value) + perpAxis.choose(0, 0, vec.z);
        return new Vec3(x, vec.y, z);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String name)
    {
        return new CustomPacketPayload.Type<>(Utils.rl(name));
    }

    public static void addPlayerInvSlots(Consumer<Slot> slotConsumer, Inventory playerInv, int x, int y)
    {
        for (int row = 0; row < 3; ++row)
        {
            for (int col = 0; col < 9; ++col)
            {
                slotConsumer.accept(new Slot(playerInv, col + row * 9 + 9, x + col * 18, y));
            }
            y += 18;
        }

        for (int col = 0; col < 9; ++col)
        {
            slotConsumer.accept(new Slot(playerInv, col, x + col * 18, y + 4));
        }
    }

    private Utils() { }
}
