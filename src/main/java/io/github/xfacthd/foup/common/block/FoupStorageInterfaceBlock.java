package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.FoupStorageInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class FoupStorageInterfaceBlock extends AbstractCartInteractorBlock
{
    private static final VoxelShape SHAPE = Shapes.or(box(0, 0, 0, 16, 2, 16), box(1, 2, 1, 15, 5, 15));

    public FoupStorageInterfaceBlock(Properties props)
    {
        super(props);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        if (canSurvive(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos()))
        {
            return defaultBlockState();
        }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState adjState, LevelAccessor level, BlockPos pos, BlockPos adjPos)
    {
        if (side == Direction.DOWN && !canSurvive(state, level, pos))
        {
            // FIXME: doesn't call onRemoved and therefor doesn't break the station link
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        return level.getBlockState(pos.below()).is(FoupContent.BLOCK_FOUP_STORAGE_LOCKER);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected BlockEntityType<FoupStorageInterfaceBlockEntity> getBlockEntityType(BlockState state)
    {
        return FoupContent.BE_TYPE_FOUP_STORAGE_INTERFACE.value();
    }
}
