package io.github.xfacthd.foup.client.renderer.item;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public final class OverheadCartItemProperty
{
    public static final ResourceLocation HAS_FOUP = Utils.rl("has_foup");

    public static void register()
    {
        ItemProperties.register(FoupContent.ITEM_CART.get(), HAS_FOUP, (stack, level, entity, seed) ->
                stack.getOrDefault(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY).hasFoup() ? 1 : 0
        );
    }

    private OverheadCartItemProperty() { }
}
