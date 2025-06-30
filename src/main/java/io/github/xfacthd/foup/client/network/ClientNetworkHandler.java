package io.github.xfacthd.foup.client.network;

import io.github.xfacthd.foup.client.renderer.debug.RailNetworkDebugRenderer;
import io.github.xfacthd.foup.client.screen.OverheadCartScreen;
import io.github.xfacthd.foup.client.screen.OverheadRailStationScreen;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationLinkPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationRenamePayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundOpenOverheadRailStationScreenPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientNetworkHandler
{
    public static void onRegisterPayloadHandlers(RegisterClientPayloadHandlersEvent event)
    {
        event.register(ClientboundRailNetworkDebugPayload.TYPE, ClientNetworkHandler::handleRailNetDebug);
        event.register(ClientboundOpenOverheadRailStationScreenPayload.TYPE, ClientNetworkHandler::handleOpenOverheadRailStationScreen);
        event.register(ClientboundAcknowledgeStationRenamePayload.TYPE, ClientNetworkHandler::handleStationRenameAck);
        event.register(ClientboundAcknowledgeStationLinkPayload.TYPE, ClientNetworkHandler::handleStationLinkAck);
        event.register(ClientboundRefreshStaleSchedulePayload.TYPE, ClientNetworkHandler::handleRefreshStaleSchedule);
    }

    private static void handleRailNetDebug(ClientboundRailNetworkDebugPayload payload, IPayloadContext ctx)
    {
        RailNetworkDebugRenderer.handleData(payload.networkId(), payload.data().orElse(null));
    }

    private static void handleOpenOverheadRailStationScreen(ClientboundOpenOverheadRailStationScreenPayload payload, IPayloadContext ctx)
    {
        BlockPos pos = payload.pos();
        if (ctx.player().level().getBlockEntity(pos) instanceof OverheadRailStationBlockEntity station)
        {
            Minecraft.getInstance().setScreen(new OverheadRailStationScreen(pos, station));
        }
    }

    private static void handleStationRenameAck(ClientboundAcknowledgeStationRenamePayload payload, IPayloadContext ctx)
    {
        if (Minecraft.getInstance().screen instanceof OverheadRailStationScreen screen)
        {
            screen.onRenameAck(payload.pos(), payload.result());
        }
    }

    private static void handleStationLinkAck(ClientboundAcknowledgeStationLinkPayload payload, IPayloadContext ctx)
    {
        if (Minecraft.getInstance().screen instanceof OverheadRailStationScreen screen)
        {
            screen.onLinkAck(payload.pos(), payload.result());
        }
    }

    private static void handleRefreshStaleSchedule(ClientboundRefreshStaleSchedulePayload payload, IPayloadContext ctx)
    {
        if (Minecraft.getInstance().screen instanceof OverheadCartScreen screen && screen.getCart().getId() == payload.cartId())
        {
            screen.updateStaleSchedule(payload.scheduleEntries(), payload.rejectedAction(), payload.stations());
        }
    }

    private ClientNetworkHandler() { }
}
