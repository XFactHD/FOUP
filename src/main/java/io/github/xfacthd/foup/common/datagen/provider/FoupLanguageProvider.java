package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.FoupContent;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class FoupLanguageProvider extends LanguageProvider
{
    public FoupLanguageProvider(PackOutput output)
    {
        super(output, Foup.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        add(FoupContent.BLOCK_RAIL.value(), "Overhead Rail");
        add(FoupContent.BLOCK_RAIL_CURVE.value(), "Overhead Rail Curve");
        add(FoupContent.BLOCK_RAIL_SWITCH.value(), "Overhead Rail Switch");
        add(FoupContent.BLOCK_RAIL_STATION.value(), "Overhead Rail Station");
        add(FoupContent.BLOCK_FOUP_LOADER.value(), "FOUP Loader");
        add(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value(), "FOUP Storage Interface");
        add(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "FOUP Storage Locker");

        add(FoupContent.ITEM_FOUP.value(), "FOUP");
        add(FoupContent.ITEM_CART.value(), "Overhead Hoist Cart");

        add("item.foup.foup.desc", "Front Opening Unified Pod");

        add(FoupContent.TAB_MAIN.value().getDisplayName(), "FOUP");

        add("desc.foup.component.held_foup.contents", "FOUP Contents: %s x %s");
        add("desc.foup.component.held_foup.contents.empty", "FOUP Contents: Empty");
        add("desc.foup.component.item_contents.contents", "Contains: %s x %s");
    }

    private void add(Component key, String value)
    {
        ComponentContents contents = key.getContents();
        if (contents instanceof TranslatableContents translatable)
        {
            add(translatable.getKey(), value);
        }
        else
        {
            add(key.getString(), value);
        }
    }
}
