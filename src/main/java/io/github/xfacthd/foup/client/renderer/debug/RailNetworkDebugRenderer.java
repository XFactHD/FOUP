package io.github.xfacthd.foup.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.client.renderer.PipelineModifiers;
import io.github.xfacthd.foup.client.renderer.special.OverheadRailInfoRenderer;
import io.github.xfacthd.foup.client.util.ClientUtils;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugData;
import io.github.xfacthd.foup.common.util.Utils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class RailNetworkDebugRenderer
{
    private static final Long2ObjectMap<RailNetworkDebugData> DEBUG_DATA = new Long2ObjectOpenHashMap<>();
    private static final Function<@Nullable StationType, @Nullable Component> STATION_FORMATTER = stationType ->
            Component.literal(stationType != null ? stationType.name() : "NULL");
    private static final ContextKey<List<RailNetworkDebugRenderState>> DATA_KEY = new ContextKey<>(Utils.rl("rail_net_debug_renderer"));

    public static void onExtractRenderState(ExtractLevelRenderStateEvent event)
    {
        if (DEBUG_DATA.isEmpty()) return;

        List<RailNetworkDebugRenderState> renderStates = new ArrayList<>();
        boolean showNetId = Objects.requireNonNull(Minecraft.getInstance().player).isShiftKeyDown();
        for (Long2ObjectMap.Entry<RailNetworkDebugData> entry : DEBUG_DATA.long2ObjectEntrySet())
        {
            renderStates.add(new RailNetworkDebugRenderState(entry.getLongKey(), entry.getValue(), showNetId));
        }
        if (!renderStates.isEmpty())
        {
            event.getRenderState().setRenderData(DATA_KEY, renderStates);
        }
    }

    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentParticles event)
    {
        List<RailNetworkDebugRenderState> renderStates = event.getLevelRenderState().getRenderData(DATA_KEY);
        if (renderStates == null) return;

        RenderSystem.pushPipelineModifier(PipelineModifiers.NO_DEPTH_TEST);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        CameraRenderState camera = event.getLevelRenderState().cameraRenderState;
        Font font = Minecraft.getInstance().font;
        for (RailNetworkDebugRenderState renderState : renderStates)
        {
            renderNetwork(renderState, buffer, event.getPoseStack(), camera, font);
        }
        buffer.endBatch(ClientUtils.INFO_QUADS);
        buffer.endLastBatch();
        RenderSystem.popPipelineModifier();
    }

    private static void renderNetwork(RailNetworkDebugRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack, CameraRenderState camera, Font font)
    {
        VertexConsumer builder = buffer.getBuffer(ClientUtils.INFO_QUADS);
        for (RailNetworkDebugData.Node node : renderState.data.nodes())
        {
            poseStack.pushPose();

            Vec3 offset = Vec3.atCenterOf(node.pos()).add(0, .25, 0).subtract(camera.pos);
            poseStack.translate(offset.x, offset.y, offset.z);

            Matrix4f pose = poseStack.last().pose();
            int color = node.occupied() ? 0xFFFF0000 : 0xFF00FF00;
            builder.addVertex(pose, -.15F, 0, -.15F).setColor(color);
            builder.addVertex(pose, -.15F, 0,  .15F).setColor(color);
            builder.addVertex(pose,  .15F, 0,  .15F).setColor(color);
            builder.addVertex(pose,  .15F, 0, -.15F).setColor(color);

            if (renderState.showNetId)
            {
                renderNetworkId(buffer, poseStack, camera, font, renderState.netId);
            }

            for (BlockPos neighbour : node.neighbours())
            {
                Direction dir = Utils.getDirByNormal(node.pos(), neighbour);
                if (dir != null)
                {
                    OverheadRailInfoRenderer.renderArrow(poseStack, builder, dir, .15F, true);
                }
                else
                {
                    Vec3 diff = Vec3.atCenterOf(neighbour).subtract(Vec3.atCenterOf(node.pos())).normalize();
                    double angle = Math.atan2(diff.z, diff.x) - Math.toRadians(90F);
                    Quaternionfc yRot = Axis.YN.rotation((float) angle);
                    OverheadRailInfoRenderer.renderArrow(poseStack, builder, yRot, 0F, true, 0xFFFF0000);
                }
            }

            if (node.stationName().isPresent())
            {
                OverheadRailInfoRenderer.renderStationInfo(
                        poseStack, camera, buffer, font, node.stationName().get(), node.stationType().orElse(null), STATION_FORMATTER
                );
            }

            poseStack.popPose();
        }
    }

    private static void renderNetworkId(MultiBufferSource.BufferSource buffer, PoseStack poseStack, CameraRenderState camera, Font font, long netId)
    {
        poseStack.pushPose();

        poseStack.translate(0, 0, 0);
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.scale(1F/40F, 1F/40F, 1);

        String name = Long.toString(netId);
        Matrix4f pose = poseStack.last().pose();
        font.drawInBatch(name, -(font.width(name) / 2F), -9, 0xFFFFFFFF, false, pose, buffer, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);

        poseStack.popPose();
    }

    public static void handleData(long networkId, @Nullable RailNetworkDebugData data)
    {
        if (data != null)
        {
            DEBUG_DATA.put(networkId, data);
        }
        else
        {
            DEBUG_DATA.remove(networkId);
        }
    }

    public static void clearData()
    {
        DEBUG_DATA.clear();
    }

    public static void onPlayerDisconnect(@SuppressWarnings("unused") ClientPlayerNetworkEvent.LoggingOut event)
    {
        DEBUG_DATA.clear();
    }

    private record RailNetworkDebugRenderState(long netId, RailNetworkDebugData data, boolean showNetId) { }

    private RailNetworkDebugRenderer() { }
}
