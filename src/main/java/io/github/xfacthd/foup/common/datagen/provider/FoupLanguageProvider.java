package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.client.screen.FoupLoaderScreen;
import io.github.xfacthd.foup.client.screen.OverheadRailStationScreen;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.FoupLoaderBlockEntity;
import io.github.xfacthd.foup.common.blockentity.FoupStorageLockerBlockEntity;
import io.github.xfacthd.foup.common.item.FoupItem;
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

        add(OverheadRailStationScreen.SCREEN_TITLE, "Overhead Rail Station");
        add(FoupLoaderBlockEntity.MENU_TITLE, "FOUP Loader");
        add(FoupStorageLockerBlockEntity.MENU_TITLE, "FOUP Storage Locker");
        add(FoupItem.MENU_TITLE, "FOUP");

        add(OverheadRailStationScreen.TITLE_NAME_EDIT, "Station Name");
        add(OverheadRailStationScreen.LABEL_NAME, "Name:");
        add(OverheadRailStationScreen.LABEL_STATE, "State:");
        add(OverheadRailStationScreen.LABEL_ERROR, "Error:");
        add(OverheadRailStationScreen.VALUE_UNLINKED, "Unlinked");
        add(OverheadRailStationScreen.VALUE_LINKED_LOADER, "Linked to loader");
        add(OverheadRailStationScreen.VALUE_LINKED_STORAGE, "Linked to storage");
        add(OverheadRailStationScreen.MSG_NAME_INVALID, "This name is invalid");
        add(OverheadRailStationScreen.MSG_NAME_TAKEN, "This name is taken");
        add(OverheadRailStationScreen.MSG_RENAME_FAILED, "Renaming failed");
        add(OverheadRailStationScreen.MSG_LINK_FAILED, "Failed to link station");
        add(OverheadRailStationScreen.MSG_ALREADY_LINKED, "Station is already linked");
        add(FoupLoaderScreen.MSG_LOADING_BLOCKED, "Loader is waiting for items to load");
        add(FoupLoaderScreen.MSG_UNLOADING_BLOCKED, "Loader is waiting for space in the output");
        add(FoupLoaderScreen.BUTTON_AUTO_EJECT, "Auto-eject");
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
