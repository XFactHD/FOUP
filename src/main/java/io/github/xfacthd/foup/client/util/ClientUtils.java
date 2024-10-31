package io.github.xfacthd.foup.client.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;

public final class ClientUtils
{
    public static final RenderType INFO_QUADS = RenderType.create(
            "foup_info_quads",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            RenderType.TRANSIENT_BUFFER_SIZE,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );

    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event)
    {
        event.registerRenderBuffer(INFO_QUADS);
    }

    private ClientUtils() { }
}
