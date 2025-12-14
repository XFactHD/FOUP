package io.github.xfacthd.foup.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.gigaherz.graph3.Graph;
import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.data.railnet.RailNetworkSavedData;
import io.github.xfacthd.foup.common.data.railnet.TrackNode;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.entity.OverheadCartState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class FoupCommands
{
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Commands.literal("foup")
                .then(registerDebugRailNetworkCommand())
                .then(registerNetworkInfoCommand())
                .then(registerFixStuckCartsCommand())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerDebugRailNetworkCommand()
    {
        return Commands.literal("debug_rail_net")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx ->
                {
                    if (RailNetworkDebugPayloads.addReceiver(ctx.getSource().getPlayerOrException()))
                    {
                        return Command.SINGLE_SUCCESS;
                    }
                    ctx.getSource().sendFailure(Component.literal("Player is already registered"));
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerNetworkInfoCommand()
    {
        return Commands.literal("networks")
                .then(Commands.literal("list")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx ->
                        {
                            MutableComponent networks = Component.literal("List of FOUP rail networks:");
                            RailNetworkSavedData.get(ctx.getSource().getLevel()).forEach((id, graph) ->
                                    networks.append(Component.literal("\n- ID: " + id + ", Nodes: " + graph.getObjects().size()))
                            );
                            ctx.getSource().sendSuccess(() -> networks, true);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("remove")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.argument("network_id", LongArgumentType.longArg())
                                .executes(ctx ->
                                {
                                    long id = LongArgumentType.getLong(ctx, "network_id");
                                    if (RailNetworkSavedData.get(ctx.getSource().getLevel()).removeNetwork(id))
                                    {
                                        ctx.getSource().sendSuccess(() -> Component.literal("Removed network with ID " + id), true);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    ctx.getSource().sendFailure(Component.literal("Network with ID " + id + " does not exist"));
                                    return 0;
                                })
                        )
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerFixStuckCartsCommand()
    {
        return Commands.literal("fix_stuck_carts")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("network_id", LongArgumentType.longArg())
                        .executes(ctx ->
                        {
                            long id = LongArgumentType.getLong(ctx, "network_id");
                            ServerLevel level = ctx.getSource().getLevel();

                            Graph<RailNetwork> network = RailNetworkSavedData.get(level).getNetwork(id);
                            if (network == null)
                            {
                                ctx.getSource().sendFailure(Component.literal("Network with ID " + id + " does not exist"));
                                return 0;
                            }

                            int carts = 0;
                            for (TrackNode node : network.getContextData().getStationNodes())
                            {
                                BlockPos pos = node.getPos();
                                if (!level.shouldTickBlocksAt(ChunkPos.asLong(pos))) continue;
                                if (!(node.getOwner() instanceof OverheadRailStationBlockEntity station)) continue;

                                AbstractCartInteractorBlockEntity interactor = station.getLinkedBlock();
                                if (interactor == null || interactor.getState() != AbstractCartInteractorBlockEntity.State.IDLE) continue;

                                AABB area = AABB.encapsulatingFullBlocks(pos, pos.below());
                                for (OverheadCartEntity cart : level.getEntitiesOfClass(OverheadCartEntity.class, area))
                                {
                                    if (cart.getState() == OverheadCartState.POD_IN_LOADER_OR_STORAGE)
                                    {
                                        // Assume the loader somehow lost track of the cart, effectively deadlocking the system
                                        cart.rescue();
                                        carts++;
                                    }
                                }
                            }
                            int finalCarts = carts;
                            ctx.getSource().sendSuccess(() -> Component.literal("Fixed " + finalCarts + " stuck carts on network " + id), true);
                            return carts;
                        })
                );
    }

    private FoupCommands() { }
}
