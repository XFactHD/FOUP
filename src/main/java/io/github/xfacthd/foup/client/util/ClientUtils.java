package io.github.xfacthd.foup.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class ClientUtils
{
    private static final RenderPipeline INFO_QUADS_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Utils.rl("info_quads"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build();
    public static final RenderType INFO_QUADS = RenderType.create(
            "foup_info_quads",
            RenderSetup.builder(INFO_QUADS_PIPELINE)
                    .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                    .createRenderSetup()
    );

    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event)
    {
        event.registerPipeline(INFO_QUADS_PIPELINE);
    }

    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event)
    {
        event.registerRenderBuffer(INFO_QUADS);
    }

    private ClientUtils() { }
}
