package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCartInteractorBlock extends Block implements EntityBlock
{
    protected AbstractCartInteractorBlock(Properties props)
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
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!newState.is(state.getBlock()) && !level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractCartInteractorBlockEntity be)
        {
            be.unlink();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected abstract BlockEntityType<? extends AbstractCartInteractorBlockEntity> getBlockEntityType(BlockState state);

    protected boolean hasTicker(BlockState state)
    {
        return true;
    }

    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return getBlockEntityType(state).create(pos, state);
    }

    @Override
    @Nullable
    public final  <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (!level.isClientSide() && hasTicker(state))
        {
            return BaseEntityBlock.createTickerHelper(type, getBlockEntityType(state), AbstractCartInteractorBlockEntity::tick);
        }
        return null;
    }
}
