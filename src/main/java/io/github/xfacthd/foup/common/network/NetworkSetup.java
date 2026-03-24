package io.github.xfacthd.foup.common.network;

import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationLinkPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundAcknowledgeStationRenamePayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundOpenOverheadRailStationScreenPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugClearPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRailNetworkDebugDataPayload;
import io.github.xfacthd.foup.common.network.payload.clientbound.ClientboundRefreshStaleSchedulePayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundAddScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundDeleteScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundEditScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundExecuteSchedulePayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundMoveScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundRequestStationLinkPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundRequestStationRenamePayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundStopSchedulePayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundToggleLoaderAutoEjectPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NetworkSetup
{
    private static final String NET_VERSION = "1";

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
    {
        event.registrar(NET_VERSION)
                .playToClient(
                        ClientboundRailNetworkDebugDataPayload.TYPE,
                        ClientboundRailNetworkDebugDataPayload.STREAM_CODEC
                )
                .playToClient(
                        ClientboundRailNetworkDebugClearPayload.TYPE,
                        ClientboundRailNetworkDebugClearPayload.STREAM_CODEC
                )
                .playToClient(
                        ClientboundOpenOverheadRailStationScreenPayload.TYPE,
                        ClientboundOpenOverheadRailStationScreenPayload.STREAM_CODEC
                )
                .playToClient(
                        ClientboundAcknowledgeStationRenamePayload.TYPE,
                        ClientboundAcknowledgeStationRenamePayload.STREAM_CODEC
                )
                .playToClient(
                        ClientboundAcknowledgeStationLinkPayload.TYPE,
                        ClientboundAcknowledgeStationLinkPayload.STREAM_CODEC
                )
                .playToClient(
                        ClientboundRefreshStaleSchedulePayload.TYPE,
                        ClientboundRefreshStaleSchedulePayload.STREAM_CODEC
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
                )
                .playToServer(
                        ServerboundAddScheduleEntryPayload.TYPE,
                        ServerboundAddScheduleEntryPayload.STREAM_CODEC,
                        ServerboundAddScheduleEntryPayload::handle
                )
                .playToServer(
                        ServerboundEditScheduleEntryPayload.TYPE,
                        ServerboundEditScheduleEntryPayload.STREAM_CODEC,
                        ServerboundEditScheduleEntryPayload::handle
                )
                .playToServer(
                        ServerboundMoveScheduleEntryPayload.TYPE,
                        ServerboundMoveScheduleEntryPayload.STREAM_CODEC,
                        ServerboundMoveScheduleEntryPayload::handle
                )
                .playToServer(
                        ServerboundDeleteScheduleEntryPayload.TYPE,
                        ServerboundDeleteScheduleEntryPayload.STREAM_CODEC,
                        ServerboundDeleteScheduleEntryPayload::handle
                )
                .playToServer(
                        ServerboundExecuteSchedulePayload.TYPE,
                        ServerboundExecuteSchedulePayload.STREAM_CODEC,
                        ServerboundExecuteSchedulePayload::handle
                )
                .playToServer(
                        ServerboundStopSchedulePayload.TYPE,
                        ServerboundStopSchedulePayload.STREAM_CODEC,
                        ServerboundStopSchedulePayload::handle
                );
    }

    private NetworkSetup() { }
}
