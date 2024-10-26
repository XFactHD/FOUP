package io.github.xfacthd.foup.client.util;

import io.github.xfacthd.foup.client.screen.OverheadRailStationScreen;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.RenameResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.util.TriState;

public final class ClientAccess
{
    public static void openStationScreen(BlockPos pos, OverheadRailStationBlockEntity station)
    {
        Minecraft.getInstance().setScreen(new OverheadRailStationScreen(pos, station));
    }

    public static void handleStationRenameAck(BlockPos pos, RenameResult result)
    {
        if (Minecraft.getInstance().screen instanceof OverheadRailStationScreen screen)
        {
            screen.onRenameAck(pos, result);
        }
    }

    public static void handleStationLinkAck(BlockPos pos, TriState result)
    {
        if (Minecraft.getInstance().screen instanceof OverheadRailStationScreen screen)
        {
            screen.onLinkAck(pos, result);
        }
    }

    private ClientAccess() { }
}
