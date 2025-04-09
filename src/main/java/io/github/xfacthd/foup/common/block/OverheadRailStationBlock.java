package io.github.xfacthd.foup.common.block;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.RailType;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundOpenOverheadRailStationScreenPayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

public final class OverheadRailStationBlock extends OverheadRailBlock
{
    public static final VoxelShape STATION_SHAPE_X = Shapes.or(SHAPE_X, box(0, 11, 0, 16, 16, 16));
    public static final VoxelShape STATION_SHAPE_Z = Shapes.or(SHAPE_Z, box(0, 11, 0, 16, 16, 16));

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
            if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity)
            {
                PacketDistributor.sendToPlayer(serverPlayer, new ClientboundOpenOverheadRailStationScreenPayload(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved)
    {
        if (level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity be)
        {
            be.unlink(true);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Utils.isX(state.getValue(PropertyHolder.FACING_HOR)) ? STATION_SHAPE_X : STATION_SHAPE_Z;
    }
}
