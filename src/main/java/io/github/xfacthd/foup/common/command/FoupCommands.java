package io.github.xfacthd.foup.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.xfacthd.foup.common.data.railnet.RailNetworkSavedData;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class FoupCommands
{
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Commands.literal("foup")
                .then(registerDebugRailNetworkCommand())
                .then(registerNetworkInfoCommand())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerDebugRailNetworkCommand()
    {
        return Commands.literal("debug_rail_net")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
                        .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
                        .requires(cs -> cs.hasPermission(Commands.LEVEL_ADMINS))
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

    private FoupCommands() { }
}
