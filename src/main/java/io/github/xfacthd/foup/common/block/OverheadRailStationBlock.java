package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.RailType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.TriState;

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
        // TODO: move linking to UI
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity be)
        {
            TriState result = be.tryLink();
            switch (result)
            {
                case TRUE -> player.displayClientMessage(Component.literal("Linked successfully"), true);
                case DEFAULT -> player.displayClientMessage(Component.literal("Already linked"), true);
                case FALSE -> player.displayClientMessage(Component.literal("Link failed"), true);
            }
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
