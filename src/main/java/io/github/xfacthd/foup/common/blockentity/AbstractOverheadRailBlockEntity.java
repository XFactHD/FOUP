package io.github.xfacthd.foup.common.blockentity;

import dev.gigaherz.graph3.Graph;
import io.github.xfacthd.foup.common.block.AbstractOverheadRailBlock;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.data.railnet.RailNetworkSavedData;
import io.github.xfacthd.foup.common.data.railnet.TrackNode;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractOverheadRailBlockEntity extends BaseBlockEntity
{
    private static final Direction[] HORIZONTAL_DIRECTIONS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

    @Nullable
    private TrackNode trackNode;

    protected AbstractOverheadRailBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    protected String getName()
    {
        return "";
    }

    protected boolean isStation()
    {
        return false;
    }

    public int getStationHeightDifference()
    {
        return -1;
    }

    public final void destroyNode()
    {
        Graph<RailNetwork> graph = Objects.requireNonNull(trackNode, "Track node missing").getGraph();
        Objects.requireNonNull(graph, "Graph missing").remove(trackNode);
    }

    @Nullable
    public TrackNode getTrackNode()
    {
        return trackNode;
    }

    public void notifyArrival(OverheadCartEntity cart, AbstractCartInteractorBlockEntity.Action action) { }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if (!level().isClientSide())
        {
            Objects.requireNonNull(trackNode, "Track node missing").detach();
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel)
        {
            trackNode = RailNetworkSavedData.get(serverLevel).findNode(worldPosition);
            if (trackNode != null)
            {
                trackNode.attach(this);
                return;
            }

            trackNode = new TrackNode(getName(), worldPosition, isStation());
            trackNode.attach(this);

            boolean anyConnected = false;
            ServerLevel level = (ServerLevel) level();
            AbstractOverheadRailBlock block = (AbstractOverheadRailBlock) getBlockState().getBlock();
            for (Direction dir : HORIZONTAL_DIRECTIONS)
            {
                if (block.isEntrySide(getBlockState(), dir))
                {
                    anyConnected |= tryConnect(level, worldPosition, dir, trackNode, false);
                }
                if (block.isExitSide(getBlockState(), dir))
                {
                    anyConnected |= tryConnect(level, worldPosition, dir, trackNode, true);
                }
            }
            // Ensure a graph at least exists for this track
            if (!anyConnected)
            {
                RailNetworkSavedData.connectTracks((ServerLevel) level(), trackNode, null);
            }
        }
    }

    private static boolean tryConnect(ServerLevel level, BlockPos pos, Direction dir, TrackNode localNode, boolean outward)
    {
        BlockPos adjPos = pos.relative(dir);
        BlockState state = level.getBlockState(adjPos);
        if (state.getBlock() instanceof AbstractOverheadRailBlock block && level.getBlockEntity(adjPos) instanceof AbstractOverheadRailBlockEntity be)
        {
            boolean connected = false;
            if (outward && block.isEntrySide(state, dir.getOpposite()) && be.trackNode != null)
            {
                RailNetworkSavedData.connectTracks(level, localNode, be.trackNode);
                connected = true;
            }
            else if (!outward && block.isExitSide(state, dir.getOpposite()) && be.trackNode != null)
            {
                RailNetworkSavedData.connectTracks(level, be.trackNode, localNode);
                connected = true;
            }
            return connected;
        }
        return false;
    }
}
