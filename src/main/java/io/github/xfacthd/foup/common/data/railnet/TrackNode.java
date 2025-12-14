package io.github.xfacthd.foup.common.data.railnet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import io.github.xfacthd.foup.common.blockentity.AbstractOverheadRailBlockEntity;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class TrackNode implements GraphObject<RailNetwork>
{
    static final MapCodec<TrackNode> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(TrackNode::getName),
            BlockPos.CODEC.fieldOf("pos").forGetter(TrackNode::getPos),
            Codec.BOOL.fieldOf("station").forGetter(TrackNode::isStation),
            StationType.CODEC.optionalFieldOf("station_type").forGetter(node -> Optional.ofNullable(node.stationType)),
            Codec.BOOL.fieldOf("occupied").forGetter(TrackNode::isOccupied)
    ).apply(inst, TrackNode::of));
    // Ensure that nodes being attached to a graph during RailNetworkSavedData construction
    // from NBT data don't try to add/remove that graph from the storage
    static boolean inhibitDataAccess = false;

    private String name;
    private final BlockPos pos;
    private final boolean station;
    @Nullable
    private StationType stationType;
    private boolean occupied;
    @Nullable
    private Graph<RailNetwork> graph = null;
    @Nullable
    private AbstractOverheadRailBlockEntity blockEntity = null;

    private static TrackNode of(String name, BlockPos pos, boolean station, Optional<StationType> stationType, boolean occupied)
    {
        return new TrackNode(name, pos, station, stationType.orElse(null), occupied);
    }

    public TrackNode(String name, BlockPos pos, boolean station)
    {
        this(name, pos, station, null, false);
    }

    TrackNode(String name, BlockPos pos, boolean station, @Nullable StationType stationType, boolean occupied)
    {
        this.name = name;
        this.pos = pos;
        this.station = station;
        this.stationType = stationType;
        this.occupied = occupied;
    }

    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
        saveAndDebugSync();
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public boolean isStation()
    {
        return station;
    }

    @Nullable
    public StationType getStationType()
    {
        return stationType;
    }

    public void setLinkedStationType(@Nullable StationType stationType)
    {
        this.stationType = stationType;
        getNetwork().invalidatePaths();
        saveAndDebugSync();
    }

    public boolean isOccupied()
    {
        return occupied;
    }

    public void setOccupied(boolean occupied)
    {
        this.occupied = occupied;
        saveAndDebugSync();
    }

    public boolean isAccessible()
    {
        return !station || blockEntity != null;
    }

    public int getPathingCost()
    {
        return station ? 5 : 1;
    }

    public boolean isInvalid()
    {
        return graph == null;
    }

    public void attach(AbstractOverheadRailBlockEntity blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    public void detach()
    {
        this.blockEntity = null;
    }

    @Nullable
    public AbstractOverheadRailBlockEntity getOwner()
    {
        return blockEntity;
    }

    public RailNetwork getNetwork()
    {
        return Objects.requireNonNull(graph).getContextData();
    }

    public void notifyArrival(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        if (blockEntity != null)
        {
            blockEntity.notifyArrival(cart, scheduleEntry);
        }
    }

    private void saveAndDebugSync()
    {
        RailNetwork network = getNetwork();
        RailNetworkSavedData.get(network.getLevel()).setDirty();
        RailNetworkDebugPayloads.sendImmediateNetworkDebugUpdate(network.getLevel(), network.getId());
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
        return "TrackNode[" + name + "@(" + pos.toShortString() + ")]";
    }
}
