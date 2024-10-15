package io.github.xfacthd.foup.common.data.railnet;

import com.google.common.base.Preconditions;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public final class Dijkstra
{
    /**
     * Computes the shortest path between the given nodes
     * @param start The starting node (must be the cart's current position to handle single-step paths correctly)
     * @param target The target node intended to be reached
     * @return The shortest path between the nodes as a queue of nodes to travel along
     * // FIXME: produces completely broken paths
     */
    public static TrackPath getShortestPath(Graph<RailNetwork> graph, TrackNode start, TrackNode target)
    {
        Preconditions.checkState(start.getGraph() == graph, "Start node is not part of the graph");
        Preconditions.checkState(target.getGraph() == graph, "Target node is not part of the graph");

        // Special-case single-step paths
        if (graph.getNeighbours(start).contains(target))
        {
            Queue<TrackPath.PathNode> nodes = new ArrayDeque<>();
            nodes.offer(TrackPath.PathNode.of(target));
            return createPath(graph, nodes);
        }

        int size = graph.getObjects().size();
        Reference2IntMap<TrackNode> distances = new Reference2IntOpenHashMap<>(size);
        distances.defaultReturnValue(Integer.MAX_VALUE);
        distances.put(start, 0);
        Reference2ReferenceMap<TrackNode, TrackNode> predecessors = new Reference2ReferenceOpenHashMap<>(size);
        Queue<GraphObject<?>> queue = new PriorityQueue<>(size, Comparator.comparingInt(distances::getInt));
        queue.addAll(graph.getObjects());

        while (!queue.isEmpty())
        {
            TrackNode node = (TrackNode) queue.remove();
            if (node == target)
            {
                // FIXME: bad workaround for priority queue reordering in undesired ways
                if (predecessors.containsKey(node)) break;
                queue.offer(node);
            }

            for (GraphObject<RailNetwork> neighbour : graph.getNeighbours(node))
            {
                TrackNode adjNode = (TrackNode) neighbour;
                if (queue.contains(adjNode))
                {
                    int altDist = distances.getInt(node) + adjNode.getPathingCost();
                    if (altDist < distances.getInt(adjNode))
                    {
                        distances.put(adjNode, altDist);
                        predecessors.put(adjNode, node);
                    }
                }
            }
        }

        Deque<TrackPath.PathNode> nodes = new ArrayDeque<>();

        nodes.offer(TrackPath.PathNode.of(target));
        TrackNode currNode = target;
        while ((currNode = predecessors.get(currNode)) != null)
        {
            nodes.offerFirst(TrackPath.PathNode.of(currNode));
        }

        return createPath(graph, nodes);
    }

    public static TrackPath getTestPath(Graph<RailNetwork> graph)
    {
        RailNetwork network = graph.getContextData();
        Queue<TrackPath.PathNode> nodes = new ArrayDeque<>();
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 62))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 63))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 64))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 65))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(20, 116, 65))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(21, 116, 65))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(22, 116, 65))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 65))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 64))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 63))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 62))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 61))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 60))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 59))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 58))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 57))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 56))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 55))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 54))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(23, 116, 53))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(22, 116, 53))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(21, 116, 53))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(20, 116, 53))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 53))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 54))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 55))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 56))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 57))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 58))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 59))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 60))));
        nodes.offer(TrackPath.PathNode.of(network.getNode(new BlockPos(19, 116, 61))));
        return createPath(graph, nodes);
    }

    private static TrackPath createPath(Graph<RailNetwork> graph, Queue<TrackPath.PathNode> nodes)
    {
        TrackPath path = new TrackPath(nodes);
        graph.getContextData().registerPath(path);
        return path;
    }

    private Dijkstra() { }
}
