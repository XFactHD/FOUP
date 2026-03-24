package io.github.xfacthd.foup.client.renderer;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.RegisterPipelineModifiersEvent;

import java.util.Optional;

public final class PipelineModifiers
{
    public static final ResourceKey<PipelineModifier> NO_DEPTH_TEST = key("no_depth_test");

    public static void onRegisterPipelineModifiers(RegisterPipelineModifiersEvent event)
    {
        event.register(NO_DEPTH_TEST, (pipeline, name) ->
        {
            DepthStencilState depthTest = pipeline.getDepthStencilState();
            if (depthTest != null)
            {
                return pipeline.toBuilder()
                        .withLocation(name)
                        .withDepthStencilState(Optional.empty())
                        .build();
            }
            return pipeline;
        });
    }

    private static ResourceKey<PipelineModifier> key(String path)
    {
        return ResourceKey.create(PipelineModifier.MODIFIERS_KEY, Utils.rl(path));
    }

    private PipelineModifiers() { }
}
