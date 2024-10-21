package io.github.xfacthd.foup.common.network;

import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugPayload;
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
                );
    }

    private NetworkSetup() { }
}
