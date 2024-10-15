package io.github.xfacthd.foup.common.data.railnet;

import com.google.common.base.Preconditions;
import dev.gigaherz.graph3.Mergeable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class RailNetwork implements Mergeable<RailNetwork>
{
    private final ServerLevel level;
    private final Map<String, TrackNode> stations = new HashMap<>();
    private final Long2ObjectMap<TrackNode> nodes = new Long2ObjectOpenHashMap<>();
    private final ReferenceSet<TrackPath> activePaths = new ReferenceOpenHashSet<>();
    private long id;

    RailNetwork(ServerLevel level)
    {
        this.level = level;
        this.id = -1;
    }

    private RailNetwork(ServerLevel level, long id, Long2ObjectMap<TrackNode> nodes, Map<String, TrackNode> stations)
    {
        this.level = level;
        this.id = id;
        this.nodes.putAll(nodes);
        this.stations.putAll(stations);
    }

    ServerLevel getLevel()
    {
        return level;
    }

    long getId()
    {
        return id;
    }

    void setId(long id)
    {
        Preconditions.checkState(this.id == -1, "Can't assign ID to network with existing ID");
        this.id = id;
    }

    void addNode(TrackNode node)
    {
        nodes.put(node.getPos().asLong(), node);
        if (node.isStation())
        {
            stations.put(node.getName(), node);
        }
        invalidatePaths();
    }

    void removeNode(TrackNode node)
    {
        nodes.remove(node.getPos().asLong());
        if (node.isStation())
        {
            stations.remove(node.getName());
        }
        invalidatePaths();
    }

    @Nullable
    public TrackNode getNode(BlockPos pos)
    {
        return getNode(pos.asLong());
    }

    @Nullable
    public TrackNode getNode(long pos)
    {
        return nodes.get(pos);
    }

    @Nullable
    public TrackNode getStation(String name)
    {
        return stations.get(name);
    }

    public void registerPath(TrackPath path)
    {
        activePaths.add(path);
    }

    public void removePath(TrackPath path)
    {
        activePaths.remove(path);
    }

    private void invalidatePaths()
    {
        if (!activePaths.isEmpty())
        {
            activePaths.forEach(TrackPath::invalidate);
            activePaths.clear();
        }
    }

    @Override
    public RailNetwork mergeWith(RailNetwork other)
    {
        if (!TrackNode.inhibitDataAccess)
        {
            // Must happen here because the graph where other comes from is never cleared
            // and therefor not caught by the removal through TrackNode#setGraph()
            RailNetworkSavedData.get(level).removeNetwork(other.id);
            invalidatePaths();
        }

        RailNetwork newNet = new RailNetwork(level, id, nodes, stations);
        newNet.nodes.putAll(other.nodes);
        newNet.stations.putAll(other.stations);
        return newNet;
    }

    @Override
    public RailNetwork copy()
    {
        return new RailNetwork(level, -1, nodes, stations);
    }
}
