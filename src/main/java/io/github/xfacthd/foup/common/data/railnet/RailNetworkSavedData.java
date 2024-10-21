package io.github.xfacthd.foup.common.data.railnet;

import com.mojang.datafixers.util.Pair;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class RailNetworkSavedData extends SavedData
{
    private static final String NAME = "foup_rail_networks";

    private final ServerLevel level;
    private final Long2ObjectMap<Graph<RailNetwork>> networks;
    private long idCounter = 0;

    private RailNetworkSavedData(ServerLevel level)
    {
        this.level = level;
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
        return level.getDataStorage().computeIfAbsent(new Factory<>(() -> new RailNetworkSavedData(level), (tag, registries) -> load(tag, level)), NAME);
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

    public static boolean setStationName(ServerLevel level, TrackNode node, String newName)
    {
        RailNetwork network = node.getNetwork();
        if (network.isValidStationName(newName))
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
            return true;
        }
        return false;
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

    void removeNetwork(long network)
    {
        networks.remove(network);
        setDirty();
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries)
    {
        ListTag netList = new ListTag();
        for (Long2ObjectMap.Entry<Graph<RailNetwork>> entry : networks.long2ObjectEntrySet())
        {
            Graph<RailNetwork> graph = entry.getValue();
            if (graph.getObjects().isEmpty()) continue;

            CompoundTag netTag = new CompoundTag();
            netTag.putLong("id", entry.getLongKey());

            ListTag nodeList = new ListTag();
            List<GraphObject<?>> nodes = new ArrayList<>(graph.getObjects());
            for (GraphObject<?> obj : nodes)
            {
                CompoundTag nodeTag = new CompoundTag();

                TrackNode node = (TrackNode) obj;
                nodeTag.putString("name", node.getName());
                nodeTag.putLong("pos", node.getPos().asLong());
                nodeTag.putBoolean("station", node.isStation());
                nodeTag.putBoolean("occupied", node.isOccupied());

                IntList neighbours = new IntArrayList();
                for (GraphObject<RailNetwork> neighbour : graph.getNeighbours(node))
                {
                    neighbours.add(nodes.indexOf(neighbour));
                }
                nodeTag.putIntArray("neighbours", neighbours);

                nodeList.add(nodeTag);
            }
            netTag.put("nodes", nodeList);

            netList.add(netTag);
        }
        tag.putLong("id_counter", idCounter);
        tag.put("networks", netList);
        return tag;
    }

    private static RailNetworkSavedData load(CompoundTag tag, ServerLevel level)
    {
        ListTag netList = tag.getList("networks", Tag.TAG_COMPOUND);
        Long2ObjectMap<Graph<RailNetwork>> networks = new Long2ObjectOpenHashMap<>(netList.size());
        for (int i = 0; i < netList.size(); i++)
        {
            CompoundTag netTag = netList.getCompound(i);

            ListTag nodeList = netTag.getList("nodes", Tag.TAG_COMPOUND);
            List<Pair<TrackNode, int[]>> nodes = new ArrayList<>(nodeList.size());
            for (int j = 0; j < nodeList.size(); j++)
            {
                CompoundTag nodeTag = nodeList.getCompound(j);

                String name = nodeTag.getString("name");
                BlockPos pos = BlockPos.of(nodeTag.getLong("pos"));
                boolean station = nodeTag.getBoolean("station");
                boolean occupied = nodeTag.getBoolean("occupied");
                TrackNode node = new TrackNode(name, pos, station, occupied);

                nodes.add(Pair.of(node, nodeTag.getIntArray("neighbours")));
            }

            TrackNode.inhibitDataAccess = true;

            // Ensure that a graph exists even if no neighbors exist
            connectTracks(level, nodes.getFirst().getFirst(), null);

            for (Pair<TrackNode, int[]> nodeData : nodes)
            {
                TrackNode node = nodeData.getFirst();
                for (int neighbour : nodeData.getSecond())
                {
                    connectTracks(level, node, nodes.get(neighbour).getFirst());
                }
            }

            TrackNode.inhibitDataAccess = false;

            long id = netTag.getLong("id");
            Graph<RailNetwork> graph = nodes.getFirst().getFirst().getGraph();
            Objects.requireNonNull(graph);
            graph.getContextData().setId(id);
            networks.put(id, graph);
        }
        return new RailNetworkSavedData(level, tag.getLong("id_counter"), networks);
    }
}
