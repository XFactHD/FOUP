package io.github.xfacthd.foup.client;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.client.renderer.entity.OverheadCartModel;
import io.github.xfacthd.foup.client.renderer.entity.OverheadCartRenderer;
import io.github.xfacthd.foup.common.FoupContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Foup.MOD_ID, dist = Dist.CLIENT)
public final class FoupClient
{
    public FoupClient(IEventBus modBus)
    {
        modBus.addListener(FoupClient::onRegisterLayerDefinitions);
        modBus.addListener(FoupClient::onRegisterRenderers);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(OverheadCartModel.LAYER_LOCATION, OverheadCartModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(FoupContent.ENTITY_TYPE_CART.value(), OverheadCartRenderer::new);
    }
}
