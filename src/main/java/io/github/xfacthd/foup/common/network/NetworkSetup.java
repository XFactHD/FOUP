package io.github.xfacthd.foup.common.network;

import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugPayload;
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
                .playToServer(
                        ServerboundToggleLoaderAutoEjectPayload.TYPE,
                        ServerboundToggleLoaderAutoEjectPayload.STREAM_CODEC,
                        ServerboundToggleLoaderAutoEjectPayload::handle
                );
    }

    private NetworkSetup() { }
}
