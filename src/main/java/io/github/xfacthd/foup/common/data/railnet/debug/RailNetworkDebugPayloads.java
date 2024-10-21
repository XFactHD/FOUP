package io.github.xfacthd.foup.common.data.railnet.debug;

import com.google.common.collect.Sets;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.data.railnet.RailNetworkSavedData;
import io.github.xfacthd.foup.common.data.railnet.TrackNode;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class RailNetworkDebugPayloads
{
    private static final Set<ServerPlayer> RECEIVERS = Sets.newIdentityHashSet();
    private static final Set<PendingNetwork> PENDING_UPDATES = new HashSet<>();

    public static void enqueueNetworkDebugUpdate(ServerLevel level, long id)
    {
        if (!FMLEnvironment.production && !RECEIVERS.isEmpty())
        {
            PENDING_UPDATES.add(new PendingNetwork(level, id));
        }
    }

    public static void sendImmediateNetworkDebugUpdate(ServerLevel level, long id)
    {
        if (!FMLEnvironment.production && !RECEIVERS.isEmpty())
        {
            sendNetworkDebugUpdate(level, id);
        }
    }

    private static void sendNetworkDebugUpdate(ServerLevel level, long id)
    {
        RailNetworkSavedData savedData = RailNetworkSavedData.get(level);
        Graph<RailNetwork> network = savedData.getNetwork(id);
        Optional<RailNetworkDebugData> debugData = network != null ? pack(network) : Optional.empty();
        ClientboundRailNetworkDebugPayload payload = new ClientboundRailNetworkDebugPayload(id, debugData);
        RECEIVERS.forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    private static Optional<RailNetworkDebugData> pack(Graph<RailNetwork> network)
    {
        List<RailNetworkDebugData.Node> nodes = new ArrayList<>();
        for (GraphObject<RailNetwork> object : network.getObjects())
        {
            TrackNode node = (TrackNode) object;
            Optional<String> name = node.isStation() ? Optional.of(node.getName()) : Optional.empty();
            List<BlockPos> neighbours = new ArrayList<>();
            for (GraphObject<RailNetwork> neighbour : network.getNeighbours(object))
            {
                neighbours.add(((TrackNode) neighbour).getPos());
            }
            nodes.add(new RailNetworkDebugData.Node(node.getPos(), name, node.isOccupied(), neighbours));
        }
        return Optional.of(new RailNetworkDebugData(nodes));
    }

    public static boolean addReceiver(ServerPlayer player)
    {
        if (RECEIVERS.add(player))
        {
            RailNetworkSavedData.get(player.serverLevel()).forEach((id, network) ->
                    PacketDistributor.sendToPlayer(player, new ClientboundRailNetworkDebugPayload(id, pack(network)))
            );
            return true;
        }
        return false;
    }

    public static void onServerTickEnd(@SuppressWarnings("unused") ServerTickEvent.Post event)
    {
        PENDING_UPDATES.forEach(net -> sendNetworkDebugUpdate(net.level, net.id));
        PENDING_UPDATES.clear();
    }

    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            RECEIVERS.remove(player);
        }
    }

    private record PendingNetwork(ServerLevel level, long id) { }

    private RailNetworkDebugPayloads() { }
}
