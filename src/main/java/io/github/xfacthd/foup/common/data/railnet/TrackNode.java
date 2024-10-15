package io.github.xfacthd.foup.common.data.railnet;

import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import io.github.xfacthd.foup.common.blockentity.AbstractOverheadRailBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TrackNode implements GraphObject<RailNetwork>
{
    // Ensure that nodes being attached to a graph during RailNetworkSavedData construction
    // from NBT data don't try to add/remove that graph from the storage
    static boolean inhibitDataAccess = false;

    private final String name;
    private final BlockPos pos;
    private final boolean station;
    private boolean occupied;
    @Nullable
    private Graph<RailNetwork> graph = null;
    @Nullable
    private AbstractOverheadRailBlockEntity blockEntity = null;

    public TrackNode(String name, BlockPos pos, boolean station)
    {
        this(name, pos, station, false);
    }

    TrackNode(String name, BlockPos pos, boolean station, boolean occupied)
    {
        this.name = name;
        this.pos = pos;
        this.station = station;
        this.occupied = occupied;
    }

    public String getName()
    {
        return name;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public boolean isStation()
    {
        return station;
    }

    public boolean isOccupied()
    {
        return occupied;
    }

    public void setOccupied(boolean occupied)
    {
        this.occupied = occupied;
    }

    public boolean isAccessible()
    {
        return !station || blockEntity != null;
    }

    public int getPathingCost()
    {
        return station ? 5 : 1;
    }

    public void attach(AbstractOverheadRailBlockEntity blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    public void detach()
    {
        this.blockEntity = null;
    }

    public RailNetwork getNetwork()
    {
        return Objects.requireNonNull(graph).getContextData();
    }

    @Override
    @Nullable
    public Graph<RailNetwork> getGraph()
    {
        return graph;
    }

    @Override
    public void setGraph(@Nullable Graph<RailNetwork> graph)
    {
        boolean graphChanged = ((this.graph == null) != (graph == null)) || (this.graph != graph);
        if (this.graph != null && graphChanged)
        {
            this.graph.getContextData().removeNode(this);
            if (!inhibitDataAccess)
            {
                ServerLevel level = this.graph.getContextData().getLevel();
                RailNetworkSavedData.get(level).tryRemoveNetwork(this.graph, this);
            }
        }
        this.graph = graph;
        if (graph != null && graphChanged)
        {
            this.graph.getContextData().addNode(this);
            if (!inhibitDataAccess)
            {
                ServerLevel level = this.graph.getContextData().getLevel();
                RailNetworkSavedData.get(level).tryAddNetwork(this.graph);
            }
        }
    }

    @Override
    public String toString()
    {
        return "TrackNode[" + name + "]";
    }
}
