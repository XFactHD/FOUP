package io.github.xfacthd.foup.client.renderer;

import com.mojang.blaze3d.platform.DepthTestFunction;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.RegisterPipelineModifiersEvent;

public final class PipelineModifiers
{
    public static final ResourceKey<PipelineModifier> NO_DEPTH_TEST = key("no_depth_test");

    public static void onRegisterPipelineModifiers(RegisterPipelineModifiersEvent event)
    {
        event.register(NO_DEPTH_TEST, (pipeline, name) ->
        {
            DepthTestFunction depthTest = pipeline.getDepthTestFunction();
            if (depthTest != DepthTestFunction.NO_DEPTH_TEST)
            {
                return pipeline.toBuilder()
                        .withLocation(name)
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
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
