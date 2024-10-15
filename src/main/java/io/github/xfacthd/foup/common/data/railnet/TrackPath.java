package io.github.xfacthd.foup.common.data.railnet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public final class TrackPath
{
    private final Queue<PathNode> nodes;
    private boolean valid = true;

    TrackPath(Queue<PathNode> nodes)
    {
        this.nodes = nodes;
    }

    @Nullable
    public TrackNode peek(RailNetwork network)
    {
        PathNode node = nodes.peek();
        if (node != null)
        {
            return node.getNode(network);
        }
        return null;
    }

    public TrackNode remove(RailNetwork network)
    {
        return nodes.remove().getNode(network);
    }

    public boolean isEmpty()
    {
        return nodes.isEmpty();
    }

    public boolean isValid()
    {
        return valid;
    }

    void invalidate()
    {
        valid = false;
    }

    public ListTag save()
    {
        ListTag pathNodes = new ListTag();
        for (PathNode node : nodes)
        {
            pathNodes.add(LongTag.valueOf(node.pos.asLong()));
        }
        return pathNodes;
    }

    public static TrackPath load(ListTag tag)
    {
        Queue<PathNode> nodes = new ArrayDeque<>();
        for (Tag nodeTag : tag)
        {
            long pos = ((LongTag) nodeTag).getAsLong();
            nodes.offer(new PathNode(BlockPos.of(pos), null));
        }
        return new TrackPath(nodes);
    }

    static final class PathNode
    {
        private final BlockPos pos;
        @Nullable
        private TrackNode node;

        private PathNode(BlockPos pos, @Nullable TrackNode node)
        {
            this.pos = pos;
            this.node = node;
        }

        static PathNode of(TrackNode node)
        {
            return new PathNode(node.getPos(), node);
        }

        private TrackNode getNode(RailNetwork network)
        {
            if (node == null)
            {
                node = network.getNode(pos);
            }
            return node;
        }
    }
}
