package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.blockentity.FoupStorageLockerBlockEntity;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public final class FoupStorageLockerBlock extends Block implements EntityBlock
{
    public FoupStorageLockerBlock(Properties props)
    {
        super(props);
        registerDefaultState(defaultBlockState()
                .setValue(PropertyHolder.LOCKER_FRONT_BL, false)
                .setValue(PropertyHolder.LOCKER_FRONT_BR, false)
                .setValue(PropertyHolder.LOCKER_FRONT_TL, false)
                .setValue(PropertyHolder.LOCKER_FRONT_TR, false)
                .setValue(PropertyHolder.LOCKER_BACK_BL, false)
                .setValue(PropertyHolder.LOCKER_BACK_BR, false)
                .setValue(PropertyHolder.LOCKER_BACK_TL, false)
                .setValue(PropertyHolder.LOCKER_BACK_TR, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(
                PropertyHolder.FACING_HOR,
                PropertyHolder.LOCKER_FRONT_BL,
                PropertyHolder.LOCKER_FRONT_BR,
                PropertyHolder.LOCKER_FRONT_TL,
                PropertyHolder.LOCKER_FRONT_TR,
                PropertyHolder.LOCKER_BACK_BL,
                PropertyHolder.LOCKER_BACK_BR,
                PropertyHolder.LOCKER_BACK_TL,
                PropertyHolder.LOCKER_BACK_TR
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return defaultBlockState().setValue(PropertyHolder.FACING_HOR, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FoupStorageLockerBlockEntity be)
        {
            player.openMenu(be);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FoupStorageLockerBlockEntity(pos, state);
    }
}
