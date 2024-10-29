package io.github.xfacthd.foup.client.util;

import io.github.xfacthd.foup.client.screen.OverheadCartScreen;
import io.github.xfacthd.foup.client.screen.OverheadRailStationScreen;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.RenameResult;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.util.TriState;

import java.util.List;
import java.util.Map;

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

    public static void openCartScreen(OverheadCartEntity cart, List<Schedule.Entry> scheduleEntries, Map<String, StationType> stations)
    {
        Minecraft.getInstance().setScreen(new OverheadCartScreen(cart, scheduleEntries, stations));
    }

    public static void handleRefreshStaleSchedule(int cartId, Schedule.RejectedAction rejectedAction, List<Schedule.Entry> scheduleEntries, Map<String, StationType> stations)
    {
        if (Minecraft.getInstance().screen instanceof OverheadCartScreen screen && screen.getCart().getId() == cartId)
        {
            screen.updateStaleSchedule(scheduleEntries, rejectedAction, stations);
        }
    }

    private ClientAccess() { }
}
