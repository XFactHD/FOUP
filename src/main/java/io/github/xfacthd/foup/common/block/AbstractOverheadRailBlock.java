package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.blockentity.AbstractOverheadRailBlockEntity;
import io.github.xfacthd.foup.common.data.RailType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class AbstractOverheadRailBlock extends Block implements EntityBlock
{
    private static final Direction[] HORIZONTAL_DIRECTIONS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

    private final RailType type;

    protected AbstractOverheadRailBlock(Properties props, RailType type)
    {
        super(props.noOcclusion()
                .mapColor(MapColor.METAL)
                .pushReaction(PushReaction.BLOCK)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        );
        this.type = type;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        // TODO: any cart on the track being destroyed must be dropped
        if (!newState.is(state.getBlock()) && !level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractOverheadRailBlockEntity be)
        {
            be.destroyNode();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return type.getBlockEntity().create(pos, state);
    }

    public abstract boolean isEntrySide(BlockState state, Direction side);

    public abstract boolean isExitSide(BlockState state, Direction side);

    @Nullable
    protected static Connection findConnectionTowardsExit(Level level, BlockPos placePos, Predicate<Direction> validEntryDir)
    {
        Connection con = null;
        for (Direction dir : HORIZONTAL_DIRECTIONS)
        {
            if (!validEntryDir.test(dir)) continue;

            BlockState state = level.getBlockState(placePos.relative(dir));
            if (state.getBlock() instanceof AbstractOverheadRailBlock block)
            {
                if (block.isExitSide(state, dir.getOpposite()))
                {
                    return new Connection(dir, false);
                }
                else if (block.isEntrySide(state, dir.getOpposite()))
                {
                    con = new Connection(dir, true);
                }
            }
        }
        return con;
    }

    @Nullable
    protected static Connection findConnectionTowardsEntry(Level level, BlockPos placePos, Predicate<Direction> validExitDir)
    {
        Connection con = null;
        for (Direction dir : HORIZONTAL_DIRECTIONS)
        {
            if (!validExitDir.test(dir)) continue;

            BlockState state = level.getBlockState(placePos.relative(dir));
            if (state.getBlock() instanceof AbstractOverheadRailBlock block)
            {
                if (block.isEntrySide(state, dir.getOpposite()))
                {
                    return new Connection(dir, true);
                }
                else if (block.isExitSide(state, dir.getOpposite()))
                {
                    con = new Connection(dir, false);
                }
            }
        }
        return con;
    }

    protected record Connection(Direction dir, boolean entry) { }
}
