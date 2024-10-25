package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.FoupLoaderBlockEntity;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.util.ShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FoupLoaderBlock extends AbstractCartInteractorBlock
{
    private static final VoxelShape BASE_SHAPE = Shapes.join(ShapeUtils.orUnoptimized(
            box(0, 0, 0, 16, 5, 16), box(0, 0, 0, 16, 16, 2), box(3, 2, 2, 13, 9, 3)
    ), box(3, 2, 3, 13, 5, 13), BooleanOp.ONLY_FIRST);
    private static final VoxelShape[] SHAPES = ShapeUtils.makeHorizontalRotations(BASE_SHAPE, Direction.NORTH);

    public FoupLoaderBlock(Properties props)
    {
        super(props);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(PropertyHolder.FACING_HOR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return defaultBlockState().setValue(PropertyHolder.FACING_HOR, ctx.getHorizontalDirection());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FoupLoaderBlockEntity be)
        {
            player.openMenu(be);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[state.getValue(PropertyHolder.FACING_HOR).get2DDataValue()];
    }

    @Override
    protected BlockEntityType<FoupLoaderBlockEntity> getBlockEntityType(BlockState state)
    {
        return FoupContent.BE_TYPE_FOUP_LOADER.value();
    }
}
