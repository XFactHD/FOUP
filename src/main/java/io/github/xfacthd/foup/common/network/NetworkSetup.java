package io.github.xfacthd.foup.common.network;

import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationLinkPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationRenamePayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundRequestStationLinkPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundRequestStationRenamePayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundToggleLoaderAutoEjectPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NetworkSetup
{
    private static final String NET_VERSION = "1";

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
    {
        event.registrar(NET_VERSION)
                .playToClient(
                        ClientboundRailNetworkDebugPayload.TYPE,
                        ClientboundRailNetworkDebugPayload.STREAM_CODEC,
                        ClientboundRailNetworkDebugPayload::handle
                )
                .playToClient(
                        ClientboundAcknowledgeStationRenamePayload.TYPE,
                        ClientboundAcknowledgeStationRenamePayload.STREAM_CODEC,
                        ClientboundAcknowledgeStationRenamePayload::handle
                )
                .playToClient(
                        ClientboundAcknowledgeStationLinkPayload.TYPE,
                        ClientboundAcknowledgeStationLinkPayload.STREAM_CODEC,
                        ClientboundAcknowledgeStationLinkPayload::handle
                )
                .playToServer(
                        ServerboundToggleLoaderAutoEjectPayload.TYPE,
                        ServerboundToggleLoaderAutoEjectPayload.STREAM_CODEC,
                        ServerboundToggleLoaderAutoEjectPayload::handle
                )
                .playToServer(
                        ServerboundRequestStationRenamePayload.TYPE,
                        ServerboundRequestStationRenamePayload.STREAM_CODEC,
                        ServerboundRequestStationRenamePayload::handle
                )
                .playToServer(
                        ServerboundRequestStationLinkPayload.TYPE,
                        ServerboundRequestStationLinkPayload.STREAM_CODEC,
                        ServerboundRequestStationLinkPayload::handle
                );
    }

    private NetworkSetup() { }
}
