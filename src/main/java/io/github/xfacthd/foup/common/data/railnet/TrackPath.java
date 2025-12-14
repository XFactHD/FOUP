package io.github.xfacthd.foup.common.data.railnet;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public final class TrackPath
{
    public static final TrackPath INVALID = Util.make(new TrackPath(new ArrayDeque<>()), TrackPath::invalidate);

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

    @Nullable
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

    int size()
    {
        return nodes.size();
    }

    public void save(ValueOutput.TypedOutputList<BlockPos> nodeOutput)
    {
        nodes.forEach(node -> nodeOutput.add(node.pos));
    }

    public static TrackPath load(ValueInput.TypedInputList<BlockPos> nodeInput)
    {
        Queue<PathNode> nodes = new ArrayDeque<>();
        for (BlockPos nodePos : nodeInput)
        {
            nodes.offer(new PathNode(nodePos, null));
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

        @Nullable
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
