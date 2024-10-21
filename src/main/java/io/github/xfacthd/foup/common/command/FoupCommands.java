package io.github.xfacthd.foup.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugPayloads;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class FoupCommands
{
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Commands.literal("foup")
                .then(registerDebugRailNetworkCommand())
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

    private FoupCommands() { }
}
