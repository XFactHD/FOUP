package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.client.util.ClientAccess;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.RailType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public final class OverheadRailStationBlock extends OverheadRailBlock
{
    public OverheadRailStationBlock(Properties props)
    {
        super(props, RailType.STATION);
        registerDefaultState(defaultBlockState().setValue(PropertyHolder.LINKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(PropertyHolder.LINKED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!player.getMainHandItem().is(FoupContent.ITEM_CART) && !player.getOffhandItem().is(FoupContent.ITEM_CART))
        {
            if (level.isClientSide() && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity be)
            {
                ClientAccess.openStationScreen(pos, be);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!newState.is(state.getBlock()) && !level.isClientSide() && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity be)
        {
            be.unlink(true);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
