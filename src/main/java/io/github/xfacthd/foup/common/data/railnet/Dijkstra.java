package io.github.xfacthd.foup.common.data.railnet;

import com.google.common.base.Preconditions;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Queue;

public final class Dijkstra
{
    /**
     * Computes the shortest path between the given nodes
     * @param start The starting node (must be the cart's current position to handle single-step paths correctly)
     * @param target The target node intended to be reached
     * @return The shortest path between the nodes as a queue of nodes to travel along
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
        Reference2ReferenceMap<TrackNode, TrackNode> predecessors = new Reference2ReferenceOpenHashMap<>(size);
        SearchQueue queue = new SearchQueue(graph.getObjects());
        Objects.requireNonNull(queue.findNode(start)).distance = 0;

        while (!queue.isEmpty())
        {
            SearchNode node = queue.remove();
            if (node.node == target) break;

            for (GraphObject<RailNetwork> neighbour : graph.getNeighbours(node.node))
            {
                TrackNode adjNode = (TrackNode) neighbour;
                SearchNode adjSearchNode = queue.findNode(adjNode);
                if (adjSearchNode != null)
                {
                    int altDist = node.distance + adjNode.getPathingCost();
                    if (altDist < adjSearchNode.distance)
                    {
                        adjSearchNode.distance = altDist;
                        predecessors.put(adjNode, node.node);
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

    private static final class SearchNode
    {
        private final TrackNode node;
        private int distance = Integer.MAX_VALUE;

        private SearchNode(TrackNode node)
        {
            this.node = node;
        }
    }

    private static final class SearchQueue
    {
        private final SearchNode[] nodes;
        private int size;

        private SearchQueue(Collection<GraphObject<RailNetwork>> nodes)
        {
            this.nodes = new SearchNode[nodes.size()];
            int i = 0;
            for (GraphObject<RailNetwork> node : nodes)
            {
                this.nodes[i] = new SearchNode((TrackNode) node);
                i++;
            }
            this.size = nodes.size();
        }

        SearchNode remove()
        {
            if (size == 0) throw new IllegalStateException("Queue is empty");

            int minDist = Integer.MAX_VALUE;
            SearchNode result = null;
            int resultIdx = -1;
            for (int i = 0; i < nodes.length; i++)
            {
                SearchNode node = nodes[i];
                if (node != null && (result == null || node.distance < minDist))
                {
                    minDist = node.distance;
                    result = node;
                    resultIdx = i;
                }
            }
            if (resultIdx == -1) throw new IllegalStateException("No node found");
            nodes[resultIdx] = null;
            size--;
            return Objects.requireNonNull(result, "No node found");
        }

        @Nullable
        SearchNode findNode(TrackNode node)
        {
            for (SearchNode searchNode : nodes)
            {
                if (searchNode != null && searchNode.node == node)
                {
                    return searchNode;
                }
            }
            return null;
        }

        boolean isEmpty()
        {
            return size == 0;
        }
    }

    private static TrackPath createPath(Graph<RailNetwork> graph, Queue<TrackPath.PathNode> nodes)
    {
        TrackPath path = new TrackPath(nodes);
        graph.getContextData().registerPath(path);
        return path;
    }

    private Dijkstra() { }
}
