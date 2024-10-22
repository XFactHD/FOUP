package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract sealed class AbstractCartInteractorBlock extends Block implements EntityBlock permits FoupLoaderBlock, FoupStorageInterfaceBlock
{
    protected AbstractCartInteractorBlock(Properties props)
    {
        super(props);
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

    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return getBlockEntityType(state).create(pos, state);
    }

    @Override
    @Nullable
    public final  <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (!level.isClientSide())
        {
            return BaseEntityBlock.createTickerHelper(type, getBlockEntityType(state), AbstractCartInteractorBlockEntity::tick);
        }
        return null;
    }
}
