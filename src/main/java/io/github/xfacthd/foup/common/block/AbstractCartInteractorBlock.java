package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jspecify.annotations.Nullable;

public abstract sealed class AbstractCartInteractorBlock extends Block implements EntityBlock permits FoupLoaderBlock, FoupStorageInterfaceBlock
{
    protected AbstractCartInteractorBlock(Properties props)
    {
        super(props.mapColor(MapColor.METAL)
                .pushReaction(PushReaction.BLOCK)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
        );
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved)
    {
        if (level.getBlockEntity(pos) instanceof AbstractCartInteractorBlockEntity be)
        {
            be.unlink();
            be.dropContents(stack -> popResource(level, pos, stack));
        }
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
