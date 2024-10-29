package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.client.screen.FoupLoaderScreen;
import io.github.xfacthd.foup.client.screen.OverheadCartScreen;
import io.github.xfacthd.foup.client.screen.OverheadRailStationScreen;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.FoupLoaderBlockEntity;
import io.github.xfacthd.foup.common.blockentity.FoupStorageLockerBlockEntity;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.entity.OverheadCartIssue;
import io.github.xfacthd.foup.common.entity.OverheadCartState;
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

        add(StationAction.LOAD.getTranslation(), "Load");
        add(StationAction.UNLOAD.getTranslation(), "Unload");

        add(OverheadCartState.IDLE.getTranslation(), "Idle");
        add(OverheadCartState.MOVING.getTranslation(), "Moving");
        add(OverheadCartState.PARK_AFTER_ARRIVAL.getTranslation(), "Arriving");
        add(OverheadCartState.LOWERING_HOIST.getTranslation(), "Lowering hoist");
        add(OverheadCartState.POD_IN_LOADER_OR_STORAGE.getTranslation(), "Pod in loader or storage");
        add(OverheadCartState.RAISING_HOIST.getTranslation(), "Raising hoist");
        add(OverheadCartState.PARK_BEFORE_DEPARTURE.getTranslation(), "Departing");
        add(OverheadCartState.PATHING.getTranslation(), "Determining path");

        add(OverheadCartIssue.Type.NONE.getTranslationKey(), "None");
        add(OverheadCartIssue.Type.EMPTY_SCHEDULE.getTranslationKey(), "Schedule is empty");
        add(OverheadCartIssue.Type.TARGET_MISSING.getTranslationKey(), "Target '%s' does not exist");
        add(OverheadCartIssue.Type.TARGET_UNREACHABLE.getTranslationKey(), "Target '%s' is unreachable from this cart's position");
        add(OverheadCartIssue.Type.TARGET_INVALID.getTranslationKey(), "Target '%s' cannot be used");
        add(OverheadCartIssue.Type.MOVEMENT_ERROR.getTranslationKey(), "Critical error during movement, see logs");
        add(OverheadCartIssue.Type.PATH_INVALID.getTranslationKey(), "Active movement path is invalid");

        add(OverheadRailStationScreen.SCREEN_TITLE, "Overhead Rail Station");
        add(FoupLoaderBlockEntity.MENU_TITLE, "FOUP Loader");
        add(FoupStorageLockerBlockEntity.MENU_TITLE, "FOUP Storage Locker");
        add(FoupItem.MENU_TITLE, "FOUP");
        add(OverheadCartScreen.SCREEN_TITLE, "Overhead Hoist Cart");
        add(OverheadCartScreen.EntryValidity.VALID.getDescription(), "Valid");
        add(OverheadCartScreen.EntryValidity.INVALID_STATION.getDescription(), "Specified station does not exist or is not linked");
        add(OverheadCartScreen.EntryValidity.LOADER_FILTER_EMPTY.getDescription(), "Loaders require a non-empty filter if filtering is enabled");
        add(OverheadCartScreen.EntryValidity.NO_COUNT_SET.getDescription(), "Item count limit enabled but not count is specified");
        add(OverheadCartScreen.BUTTON_ADD_ENTRY, "Add Destination");
        add(OverheadCartScreen.BUTTON_EXEC, "Execute");
        add(OverheadCartScreen.BUTTON_STOP, "Stop");
        add(OverheadCartScreen.LABEL_STATE, "State:");
        add(OverheadCartScreen.LABEL_ISSUE, "Issue:");
        add(OverheadCartScreen.LABEL_SCHEDULE, "Schedule:");
        add(OverheadCartScreen.BUTTON_MOVE_UP, "Move entry up");
        add(OverheadCartScreen.BUTTON_MOVE_DOWN, "Move entry down");
        add(OverheadCartScreen.SELECT_STATION, "Target station");
        add(OverheadCartScreen.BUTTON_ACTION, "Action");
        add(OverheadCartScreen.CHECK_FILTER, "Use filter");
        add(OverheadCartScreen.SLOT_FILTER, "Specify filter");
        add(OverheadCartScreen.CHECK_COUNT, "Use count");
        add(OverheadCartScreen.CYCLE_COUNT, "Specify count");
        add(OverheadCartScreen.BUTTON_EDIT, "Edit entry");
        add(OverheadCartScreen.BUTTON_SAVE, "Save entry");
        add(OverheadCartScreen.BUTTON_DELETE, "Delete entry");
        add(OverheadCartScreen.LABEL_FILTER, "Filter:");
        add(OverheadCartScreen.LABEL_COUNT, "Count:");

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
