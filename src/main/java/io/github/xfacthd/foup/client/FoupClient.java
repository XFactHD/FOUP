package io.github.xfacthd.foup.client;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.client.renderer.block.FoupStorageInterfaceRenderer;
import io.github.xfacthd.foup.client.renderer.debug.RailNetworkDebugRenderer;
import io.github.xfacthd.foup.client.renderer.entity.OverheadCartModel;
import io.github.xfacthd.foup.client.renderer.entity.OverheadCartRenderer;
import io.github.xfacthd.foup.client.renderer.item.OverheadCartItemProperty;
import io.github.xfacthd.foup.client.screen.FoupStorageLockerScreen;
import io.github.xfacthd.foup.common.FoupContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Foup.MOD_ID, dist = Dist.CLIENT)
public final class FoupClient
{
    public FoupClient(IEventBus modBus)
    {
        modBus.addListener(FoupClient::onRegisterLayerDefinitions);
        modBus.addListener(FoupClient::onRegisterRenderers);
        modBus.addListener(FoupClient::onClientSetup);
        modBus.addListener(FoupClient::onRegisterMenuScreens);
        modBus.addListener(FoupStorageInterfaceRenderer::onTextureAtlasStitched);

        if (!FMLEnvironment.production)
        {
            modBus.addListener(RailNetworkDebugRenderer::onRegisterRenderBuffers);
            NeoForge.EVENT_BUS.addListener(RailNetworkDebugRenderer::onRenderLevelStage);
            NeoForge.EVENT_BUS.addListener(RailNetworkDebugRenderer::onPlayerDisconnect);
        }
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(OverheadCartModel.LAYER_LOCATION, OverheadCartModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(FoupContent.ENTITY_TYPE_CART.value(), OverheadCartRenderer::new);
        event.registerBlockEntityRenderer(FoupContent.BE_TYPE_FOUP_STORAGE_INTERFACE.value(), FoupStorageInterfaceRenderer::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(OverheadCartItemProperty::register);
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event)
    {
        event.register(FoupContent.MENU_TYPE_FOUP_STORAGE_LOCKER.value(), FoupStorageLockerScreen::new);
    }
}
