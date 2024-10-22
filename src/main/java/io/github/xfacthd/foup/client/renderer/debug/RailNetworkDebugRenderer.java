package io.github.xfacthd.foup.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.common.data.railnet.debug.RailNetworkDebugData;
import io.github.xfacthd.foup.common.util.Utils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class RailNetworkDebugRenderer
{
    private static final Long2ObjectMap<RailNetworkDebugData> DEBUG_DATA = new Long2ObjectOpenHashMap<>();
    private static final RenderType DEBUG_QUADS = RenderType.create(
            "foup_debug_quads",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );

    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if (DEBUG_DATA.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        RenderSystem.disableDepthTest();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Font font = Minecraft.getInstance().font;
        for (RailNetworkDebugData entry : DEBUG_DATA.values())
        {
            renderNetwork(entry, buffer, event.getPoseStack(), event.getCamera(), font);
        }
        buffer.endBatch(DEBUG_QUADS);
    }

    private static void renderNetwork(RailNetworkDebugData entry, MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera, Font font)
    {
        VertexConsumer builder = buffer.getBuffer(DEBUG_QUADS);
        for (RailNetworkDebugData.Node node : entry.nodes())
        {
            poseStack.pushPose();

            Vec3 offset = Vec3.atCenterOf(node.pos()).add(0, .25, 0).subtract(camera.getPosition());
            poseStack.translate(offset.x, offset.y, offset.z);

            Matrix4f pose = poseStack.last().pose();
            int color = node.occupied() ? 0xFFFF0000 : 0xFF00FF00;
            builder.addVertex(pose, -.15F, 0, -.15F).setColor(color);
            builder.addVertex(pose, -.15F, 0,  .15F).setColor(color);
            builder.addVertex(pose,  .15F, 0,  .15F).setColor(color);
            builder.addVertex(pose,  .15F, 0, -.15F).setColor(color);

            for (BlockPos neighbour : node.neighbours())
            {
                Direction dir = Utils.getDirByNormal(node.pos(), neighbour);
                if (dir == null) continue;

                poseStack.pushPose();
                poseStack.mulPose(Axis.YN.rotationDegrees(dir.toYRot()));

                pose = poseStack.last().pose();
                builder.addVertex(pose, -.05F, 0, .15F).setColor(0xFF0000FF);
                builder.addVertex(pose, -.05F, 0,  .4F).setColor(0xFF0000FF);
                builder.addVertex(pose,  .05F, 0,  .4F).setColor(0xFF0000FF);
                builder.addVertex(pose,  .05F, 0, .15F).setColor(0xFF0000FF);

                builder.addVertex(pose, -.1F, 0, .4F).setColor(0xFF0000FF);
                builder.addVertex(pose,   0F, 0, .5F).setColor(0xFF0000FF);
                builder.addVertex(pose,   0F, 0, .5F).setColor(0xFF0000FF);
                builder.addVertex(pose,  .1F, 0, .4F).setColor(0xFF0000FF);

                poseStack.popPose();
            }

            if (node.stationName().isPresent())
            {
                poseStack.pushPose();

                poseStack.translate(0, .5, 0);
                poseStack.mulPose(camera.rotation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                poseStack.scale(1F/40F, 1F/40F, 1);

                pose = poseStack.last().pose();
                String name = node.stationName().get();
                font.drawInBatch(name, -(font.width(name) / 2F), 0, 0xFFBB00FF, false, pose, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

                poseStack.popPose();
            }

            poseStack.popPose();
        }
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

    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event)
    {
        event.registerRenderBuffer(DEBUG_QUADS);
    }

    public static void onPlayerDisconnect(@SuppressWarnings("unused") ClientPlayerNetworkEvent.LoggingOut event)
    {
        DEBUG_DATA.clear();
    }

    private RailNetworkDebugRenderer() { }
}
