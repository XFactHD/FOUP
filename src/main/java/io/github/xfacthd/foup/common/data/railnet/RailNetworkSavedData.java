package io.github.xfacthd.foup.common.data.railnet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import io.github.xfacthd.foup.common.data.RenameResult;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class RailNetworkSavedData extends SavedData
{
    private static final String NAME = "foup_rail_networks";
    private static final SavedDataType<RailNetworkSavedData> TYPE = new SavedDataType<>(
            NAME,
            RailNetworkSavedData::new,
            ctx -> PackedData.CODEC.xmap(data -> unpack(data, ctx), RailNetworkSavedData::pack)
    );

    private final ServerLevel level;
    private final Long2ObjectMap<Graph<RailNetwork>> networks;
    private long idCounter = 0;

    private RailNetworkSavedData(@Nullable ServerLevel level)
    {
        this.level = Objects.requireNonNull(level);
        this.networks = new Long2ObjectOpenHashMap<>();
    }

    private RailNetworkSavedData(ServerLevel level, long idCounter, Long2ObjectMap<Graph<RailNetwork>> networks)
    {
        this.level = level;
        this.idCounter = idCounter;
        this.networks = networks;
    }

    public static RailNetworkSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static void connectTracks(ServerLevel level, TrackNode node, @Nullable TrackNode neighbour)
    {
        if (neighbour != null)
        {
            Graph.connect(node, neighbour, g -> new RailNetwork(level));
        }
        else
        {
            Graph.integrate(node, List.of(), g -> new RailNetwork(level));
        }
    }

    public static RenameResult setStationName(ServerLevel level, TrackNode node, String newName)
    {
        RailNetwork network = node.getNetwork();
        RenameResult result = network.isAvailableStationName(newName);
        if (result == RenameResult.SUCCESS)
        {
            String oldName = node.getName();
            if (!oldName.isBlank())
            {
                network.removeStation(oldName);
            }
            node.setName(newName);
            network.addStation(newName, node);
            network.invalidatePaths();
            RailNetworkSavedData.get(level).setDirty();
        }
        return result;
    }

    void tryAddNetwork(Graph<RailNetwork> graph)
    {
        if (!networks.containsValue(graph))
        {
            networks.put(idCounter, graph);
            graph.getContextData().setId(idCounter);
            RailNetworkDebugPayloads.enqueueNetworkDebugUpdate(level, idCounter);
            idCounter++;
        }
        // Always mark dirty as a call to this means a graph changed
        setDirty();
    }

    void tryRemoveNetwork(Graph<RailNetwork> graph, TrackNode node)
    {
        Collection<GraphObject<RailNetwork>> objects = graph.getObjects();
        // The graph clears the node's ref to the graph before removing the node from the graph
        if (objects.size() == 1 && objects.contains(node))
        {
            long network = graph.getContextData().getId();
            networks.remove(network);
            RailNetworkDebugPayloads.enqueueNetworkDebugUpdate(level, network);
        }
        // Always mark dirty as a call to this means a graph changed
        setDirty();
    }

    @Nullable
    public Graph<RailNetwork> getNetwork(long network)
    {
        return networks.get(network);
    }

    public boolean removeNetwork(long network)
    {
        boolean removed = networks.remove(network) != null;
        setDirty();
        return removed;
    }

    @Nullable
    public TrackNode findNode(BlockPos pos)
    {
        for (Graph<RailNetwork> graph : networks.values())
        {
            TrackNode node = graph.getContextData().getNode(pos);
            if (node != null)
            {
                return node;
            }
        }
        return null;
    }

    public void forEach(BiConsumer<Long, Graph<RailNetwork>> consumer)
    {
        for (Long2ObjectMap.Entry<Graph<RailNetwork>> entry : networks.long2ObjectEntrySet())
        {
            consumer.accept(entry.getLongKey(), entry.getValue());
        }
    }

    private PackedData pack()
    {
        List<PackedNetwork> netList = new ArrayList<>();
        for (Long2ObjectMap.Entry<Graph<RailNetwork>> entry : networks.long2ObjectEntrySet())
        {
            Graph<RailNetwork> graph = entry.getValue();
            if (graph.getObjects().isEmpty()) continue;

            List<PackedNode> nodeList = new ArrayList<>();
            List<GraphObject<?>> nodes = new ArrayList<>(graph.getObjects());
            for (GraphObject<?> obj : nodes)
            {
                TrackNode node = (TrackNode) obj;

                IntList neighbours = new IntArrayList();
                for (GraphObject<RailNetwork> neighbour : graph.getNeighbours(node))
                {
                    neighbours.add(nodes.indexOf(neighbour));
                }

                nodeList.add(new PackedNode(node, neighbours));
            }

            netList.add(new PackedNetwork(entry.getLongKey(), nodeList));
        }
        return new PackedData(netList, idCounter);
    }

    private static RailNetworkSavedData unpack(PackedData data, @Nullable ServerLevel level)
    {
        Objects.requireNonNull(level);

        Long2ObjectMap<Graph<RailNetwork>> networks = new Long2ObjectOpenHashMap<>(data.networks.size());
        for (PackedNetwork net : data.networks)
        {
            if (net.nodes.isEmpty())
            {
                continue;
            }

            TrackNode.inhibitDataAccess = true;

            // Ensure that a graph exists even if no neighbors exist
            connectTracks(level, net.nodes.getFirst().node, null);

            for (PackedNode nodeData : net.nodes)
            {
                TrackNode node = nodeData.node;
                for (int neighbour : nodeData.neighbors)
                {
                    connectTracks(level, node, net.nodes.get(neighbour).node);
                }
            }

            TrackNode.inhibitDataAccess = false;

            Graph<RailNetwork> graph = net.nodes.getFirst().node.getGraph();
            Objects.requireNonNull(graph);
            graph.getContextData().setId(net.id);
            networks.put(net.id, graph);
        }
        return new RailNetworkSavedData(level, data.idCounter, networks);
    }

    private record PackedData(List<PackedNetwork> networks, long idCounter)
    {
        private static final Codec<PackedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                PackedNetwork.CODEC.listOf().fieldOf("networks").forGetter(PackedData::networks),
                Codec.LONG.fieldOf("id_counter").forGetter(PackedData::idCounter)
        ).apply(inst, PackedData::new));
    }

    private record PackedNetwork(long id, List<PackedNode> nodes)
    {
        private static final Codec<PackedNetwork> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.LONG.fieldOf("id").forGetter(PackedNetwork::id),
                PackedNode.CODEC.listOf().fieldOf("nodes").forGetter(PackedNetwork::nodes)
        ).apply(inst, PackedNetwork::new));
    }

    private record PackedNode(TrackNode node, List<Integer> neighbors)
    {
        private static final Codec<PackedNode> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                TrackNode.CODEC.forGetter(PackedNode::node),
                Codec.INT.listOf().fieldOf("neighbors").forGetter(PackedNode::neighbors)
        ).apply(inst, PackedNode::new));
    }
}
