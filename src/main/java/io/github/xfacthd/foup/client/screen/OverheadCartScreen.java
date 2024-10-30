package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.client.screen.widget.FilterSlot;
import io.github.xfacthd.foup.client.screen.widget.IndicatorButton;
import io.github.xfacthd.foup.client.screen.widget.StackSizeCycleBox;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.entity.OverheadCartIssue;
import io.github.xfacthd.foup.common.entity.OverheadCartState;
import io.github.xfacthd.foup.common.item.FoupItem;
import io.github.xfacthd.foup.common.menu.OverheadCartMenu;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundAddScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundDeleteScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundEditScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundExecuteSchedulePayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundMoveScheduleEntryPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundStopSchedulePayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

// TODO: add description tooltips for filter and count
public final class OverheadCartScreen extends AbstractContainerScreen<OverheadCartMenu>
{
    private static final ResourceLocation BACKGROUND = Utils.rl("background");
    private static final ResourceLocation INVENTORY = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int WIDTH = 300;
    private static final int MIN_HEIGHT = 240;
    private static final int MAX_HEIGHT = 440;
    private static final int EDGE_PADDING_X = 8;
    private static final int EDGE_PADDING_Y = 6;
    private static final int PADDING = 5;
    private static final int INVENTORY_WIDTH = 162;
    private static final int INVENTORY_HEIGHT = 76;
    private static final int INVENTORY_X = (WIDTH / 2) - (INVENTORY_WIDTH / 2);
    private static final int INVENTORY_Y_OFF = EDGE_PADDING_Y + INVENTORY_HEIGHT;
    private static final int INVENTORY_U = 7;
    private static final int INVENTORY_V = 139;
    private static final int EXEC_STOP_WIDTH = 60;
    private static final int EXEC_STOP_X = WIDTH - EDGE_PADDING_X - EXEC_STOP_WIDTH;
    private static final int ADD_ENTRY_WIDTH = 90;
    private static final int ADD_ENTRY_X = EXEC_STOP_X - PADDING - ADD_ENTRY_WIDTH;
    private static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_Y = 50;
    private static final int STATE_Y = EDGE_PADDING_Y + 15;
    private static final int ISSUE_Y = EDGE_PADDING_Y + 27;
    private static final int SCHEDULE_Y = EDGE_PADDING_Y + 48;
    private static final int LIST_WIDTH = WIDTH - (EDGE_PADDING_X * 2);
    private static final int LIST_Y = BUTTON_Y + BUTTON_HEIGHT + 3;
    public static final Component BUTTON_ADD_ENTRY = Component.translatable("button.foup.overhead_cart.add_entry");
    public static final Component BUTTON_EXEC = Component.translatable("button.foup.overhead_cart.execute");
    public static final Component BUTTON_STOP = Component.translatable("button.foup.overhead_cart.stop");
    public static final Component LABEL_STATE = Component.translatable("label.foup.overhead_cart.state");
    public static final Component LABEL_ISSUE = Component.translatable("label.foup.overhead_cart.issue");
    public static final Component LABEL_SCHEDULE = Component.translatable("label.foup.overhead_cart.schedule");
    public static final Component BUTTON_MOVE_UP = Component.translatable("button.foup.overhead_cart.schedule.move_up");
    public static final Component BUTTON_MOVE_DOWN = Component.translatable("button.foup.overhead_cart.schedule.move_down");
    public static final Component SELECT_STATION = Component.translatable("select.foup.overhead_cart.schedule.station");
    public static final Component BUTTON_ACTION = Component.translatable("button.foup.overhead_cart.schedule.action");
    public static final Component CHECK_FILTER = Component.translatable("button.foup.overhead_cart.schedule.use_filter");
    public static final Component SLOT_FILTER = Component.translatable("button.foup.overhead_cart.schedule.filter");
    public static final Component CHECK_COUNT = Component.translatable("button.foup.overhead_cart.schedule.use_count");
    public static final Component CYCLE_COUNT = Component.translatable("button.foup.overhead_cart.schedule.count");
    public static final Component BUTTON_EDIT = Component.translatable("button.foup.overhead_cart.schedule.edit");
    public static final Component BUTTON_SAVE = Component.translatable("button.foup.overhead_cart.schedule.save");
    public static final Component BUTTON_DELETE = Component.translatable("button.foup.overhead_cart.schedule.delete");
    public static final Component LABEL_FILTER = Component.translatable("label.foup.overhead_cart.schedule.filter");
    public static final Component LABEL_COUNT = Component.translatable("label.foup.overhead_cart.schedule.count");

    private final OverheadCartEntity cart;
    private final List<Schedule.Entry> scheduleEntries;
    private final Map<String, StationType> stations;
    private Button buttonExecute;
    private Button buttonStop;
    private ScheduleList scheduleList;
    private boolean cartIdle;

    public OverheadCartScreen(OverheadCartMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.cart = menu.getCart();
        this.scheduleEntries = menu.getInitialScheduleEntries();
        this.stations = menu.getInitialStations();
        this.cartIdle = cart.getState() == OverheadCartState.IDLE;
        this.imageHeight = MIN_HEIGHT;
        this.imageWidth = WIDTH;
    }

    @Override
    protected void init()
    {
        imageHeight = Math.min(Math.max(MIN_HEIGHT, height), MAX_HEIGHT);
        super.init();

        addRenderableWidget(Button.builder(BUTTON_ADD_ENTRY, this::addStation)
                .pos(leftPos + ADD_ENTRY_X, topPos + BUTTON_Y)
                .size(ADD_ENTRY_WIDTH, BUTTON_HEIGHT)
                .build()
        );
        buttonExecute = addRenderableWidget(Button.builder(BUTTON_EXEC, this::requestExecute)
                .pos(leftPos + EXEC_STOP_X, topPos + BUTTON_Y)
                .size(EXEC_STOP_WIDTH, BUTTON_HEIGHT)
                .build()
        );
        buttonStop = addRenderableWidget(Button.builder(BUTTON_STOP, this::requestStop)
                .pos(leftPos + EXEC_STOP_X, topPos + BUTTON_Y)
                .size(EXEC_STOP_WIDTH, BUTTON_HEIGHT)
                .build()
        );

        buttonExecute.visible = cartIdle;
        buttonStop.visible = !cartIdle;

        ScheduleList oldList = scheduleList;
        int listHeight = imageHeight - LIST_Y - INVENTORY_Y_OFF - PADDING;
        scheduleList = addRenderableWidget(new ScheduleList(minecraft, leftPos + EDGE_PADDING_X, topPos + LIST_Y, listHeight, scheduleList));
        if (oldList == null)
        {
            rebuiltScheduleList();
        }

        int invTop = topPos + imageHeight - INVENTORY_Y_OFF + 1;
        for (Slot slot : menu.slots)
        {
            slot.x = INVENTORY_X + 1 + (slot.index % 9 * 18);
            slot.y = invTop + (slot.index / 9 * 18) + (slot.index >= 27 ? 4 : 0);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
    }

    @Override
    public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        renderTransparentBackground(graphics);
        graphics.blitSprite(BACKGROUND, leftPos, topPos, WIDTH, imageHeight);
        graphics.blit(INVENTORY, leftPos + INVENTORY_X, topPos + imageHeight - INVENTORY_Y_OFF, INVENTORY_U, INVENTORY_V, INVENTORY_WIDTH, INVENTORY_HEIGHT);
        graphics.drawString(font, title, leftPos + EDGE_PADDING_X, topPos + EDGE_PADDING_Y, 0x404040, false);

        OverheadCartIssue issue = cart.getIssue();
        boolean hasIssue = issue != null;

        graphics.drawString(font, LABEL_STATE, leftPos + EDGE_PADDING_X, topPos + STATE_Y, 0x404040, false);
        if (hasIssue)
        {
            graphics.drawString(font, LABEL_ISSUE, leftPos + EDGE_PADDING_X, topPos + ISSUE_Y, 0x404040, false);
        }

        int offset = Math.max(font.width(LABEL_STATE), hasIssue ? font.width(LABEL_ISSUE) : 0) + 4;
        graphics.drawString(font, cart.getState().getTranslation(), leftPos + EDGE_PADDING_X + offset, topPos + STATE_Y, 0x404040, false);
        if (hasIssue)
        {
            graphics.drawString(font, formatIssue(issue), leftPos + EDGE_PADDING_X + offset, topPos + ISSUE_Y, 0x404040, false);
        }

        graphics.drawString(font, LABEL_SCHEDULE, leftPos + EDGE_PADDING_X, topPos + SCHEDULE_Y, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (getFocused() != null && !getFocused().isMouseOver(mouseX, mouseY))
        {
            setFocused(null);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        boolean anyFocused = false;
        for (ScheduleList.ScheduleEntry child : scheduleList.children())
        {
            if (child.getFocused() instanceof EditBox)
            {
                anyFocused = true;
                break;
            }
        }
        if (!(getFocused() instanceof EditBox) && !anyFocused && Objects.requireNonNull(minecraft).options.keyInventory.matches(keyCode, scanCode))
        {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void containerTick()
    {
        if (cart.isRemoved() || !cart.isUsableByPlayer(Objects.requireNonNull(Minecraft.getInstance().player)))
        {
            onClose();
            return;
        }

        boolean lastIdle = cartIdle;
        cartIdle = cart.getState() == OverheadCartState.IDLE;
        buttonExecute.visible = cartIdle;
        buttonStop.visible = !cartIdle;
        if (lastIdle != cartIdle)
        {
            buttonExecute.active = true;
            buttonStop.active = true;
        }
    }

    @Override // For some reason AbstractContainerScreen doesn't forward dragging to widgets
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (scheduleList.isMouseOver(mouseX, mouseY))
        {
            return scheduleList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private static Component formatIssue(OverheadCartIssue issue)
    {
        if (issue.detail().isPresent())
        {
            return Component.translatable(issue.type().getTranslationKey(), issue.detail().get());
        }
        return issue.type().getTranslation();
    }

    private void requestExecute(Button btn)
    {
        PacketDistributor.sendToServer(new ServerboundExecuteSchedulePayload(cart.getId()));
        buttonExecute.active = false;
    }

    private void requestStop(Button btn)
    {
        PacketDistributor.sendToServer(new ServerboundStopSchedulePayload(cart.getId()));
        buttonStop.active = false;
    }

    private void addStation(Button btn)
    {
        Schedule.Entry entry = new Schedule.Entry(Mth.createInsecureUUID(), "", StationType.UNKNOWN, StationAction.LOAD, Optional.empty(), OptionalInt.empty());
        scheduleEntries.add(entry);
        scheduleList.addEntry(new ScheduleList.ScheduleEntry(this, entry, true));
        PacketDistributor.sendToServer(new ServerboundAddScheduleEntryPayload(cart.getId(), scheduleEntries.size() - 1, entry));
    }

    public void updateStaleSchedule(List<Schedule.Entry> scheduleEntries, Schedule.RejectedAction rejectedAction, Map<String, StationType> stations)
    {
        this.scheduleEntries.clear();
        this.scheduleEntries.addAll(scheduleEntries);
        this.stations.clear();
        this.stations.putAll(stations);

        // TODO: notify user
    }

    private void rebuiltScheduleList()
    {
        scheduleList.children().clear();
        for (Schedule.Entry entry : scheduleEntries)
        {
            scheduleList.addEntry(new ScheduleList.ScheduleEntry(this, entry, false));
        }
    }

    public OverheadCartEntity getCart()
    {
        return cart;
    }

    public boolean setFilterSlotFromDrop(ItemStack stack, int x, int y)
    {
        if (scheduleList.isMouseOver(x, y))
        {
            for (ScheduleList.ScheduleEntry entry : scheduleList.children())
            {
                if (entry.filterSlot.isMouseOver(x, y))
                {
                    return entry.filterSlot.setFilterFromDrop(stack);
                }
            }
        }
        return false;
    }

    private static final class ScheduleList extends ContainerObjectSelectionList<ScheduleList.ScheduleEntry>
    {
        private static final int LIST_ENTRY_WIDTH = LIST_WIDTH - 15;
        private static final int LIST_ENTRY_HEIGHT = 48;

        public ScheduleList(Minecraft minecraft, int x, int y, int height, @Nullable ScheduleList oldList)
        {
            super(minecraft, LIST_WIDTH, height, y, LIST_ENTRY_HEIGHT);
            setX(x);
            if (oldList != null)
            {
                oldList.children().forEach(this::addEntry);
            }
        }

        @Override
        protected int addEntry(ScheduleEntry entry)
        {
            return super.addEntry(entry);
        }

        @Override
        protected boolean removeEntry(ScheduleEntry entry)
        {
            return super.removeEntry(entry);
        }

        @Override
        public int getRowLeft()
        {
            return super.getRowLeft() - 2 - 3;
        }

        @Override
        protected int getRealRowLeft()
        {
            return super.getRealRowLeft() - 3;
        }

        @Override
        public int getRowWidth()
        {
            return LIST_ENTRY_WIDTH;
        }

        @Override
        protected int getListOutlinePadding()
        {
            return 4;
        }

        @Override
        protected boolean isValidMouseClick(int button)
        {
            return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        }

        private static final class ScheduleEntry extends Entry<ScheduleEntry>
        {
            private static final ResourceLocation ICON_UP = ResourceLocation.withDefaultNamespace("statistics/sort_up");
            private static final ResourceLocation ICON_DOWN = ResourceLocation.withDefaultNamespace("statistics/sort_down");
            private static final ResourceLocation ICON_EDIT = Utils.rl("edit");
            private static final ResourceLocation ICON_SAVE = Utils.rl("save");
            private static final ResourceLocation ICON_DELETE = Utils.rl("delete");
            private static final ResourceLocation ICON_ERROR = ResourceLocation.withDefaultNamespace("icon/unseen_notification");
            private static final int SPRITE_SIZE = 18;
            private static final int ELEM_HEIGHT = 18;
            private static final int MOVE_BTN_WIDTH = 18;
            private static final int MOVE_BTN_HEIGHT = 14;
            private static final int LEFT_ELEM_WIDTH = 100;
            private static final int CHECKBOX_SIZE = 18;
            private static final int STACKSIZE_WIDTH = 60;
            private static final int EDGE_PADDING = 2;
            private static final int MOVE_BTN_X = EDGE_PADDING;
            private static final int LEFT_ELEM_X = 22;
            private static final int CHECKBOX_X = 158;
            private static final int FILTER_X = 180;
            private static final int FILTER_LABEL_X = 126;
            private static final int FILTER_LABEL_Y = 7;
            private static final int COUNT_LABEL_Y_OFF = 14;
            private static final int ERROR_SIZE = 10;
            private static final int ERROR_X = EDGE_PADDING + (MOVE_BTN_WIDTH / 2) - (ERROR_SIZE / 2);
            private static final int ERROR_Y = 17;

            private final OverheadCartScreen owner;
            private final Button buttonUp;
            private final Button buttonDown;
            private final EditBox boxStation;
            private final CycleButton<StationAction> buttonAction;
            private final IndicatorButton checkFilter;
            private final FilterSlot filterSlot;
            private final IndicatorButton checkCount;
            private final StackSizeCycleBox boxCount;
            private final Button buttonEdit;
            private final Button buttonSave;
            private final Button buttonDelete;
            private final List<AbstractWidget> children;
            private Schedule.Entry entry;
            private StationType type;
            private boolean useFilter;
            private boolean useCount;
            private boolean mutable;
            private EntryValidity validity;

            public ScheduleEntry(OverheadCartScreen owner, Schedule.Entry entry, boolean mutable)
            {
                this.owner = owner;
                this.entry = entry;
                this.type = entry.type();
                this.useFilter = entry.filter().isPresent();
                this.useCount = entry.count().isPresent();
                this.mutable = mutable;
                this.buttonUp = SpriteIconButton.builder(BUTTON_MOVE_UP, this::moveUp, true)
                        .size(MOVE_BTN_WIDTH, MOVE_BTN_HEIGHT)
                        .sprite(ICON_UP, SPRITE_SIZE, SPRITE_SIZE)
                        .build();
                this.buttonDown = SpriteIconButton.builder(BUTTON_MOVE_DOWN, this::moveDown, true)
                        .size(MOVE_BTN_WIDTH, MOVE_BTN_HEIGHT)
                        .sprite(ICON_DOWN, SPRITE_SIZE, SPRITE_SIZE)
                        .build();
                this.boxStation = new EditBox(owner.font, LEFT_ELEM_WIDTH, ELEM_HEIGHT, SELECT_STATION); // TODO: replace with combo box or provide auto-complete suggestions
                this.boxStation.setValue(entry.station());
                this.boxStation.setResponder(this::onStationChange);
                this.buttonAction = CycleButton.builder(StationAction::getTranslation)
                        .withValues(StationAction.values())
                        .withInitialValue(entry.action())
                        .create(0, 0, LEFT_ELEM_WIDTH, ELEM_HEIGHT, BUTTON_ACTION, this::onActionChange);
                this.checkFilter = new IndicatorButton(0, 0, CHECKBOX_SIZE, CHECKBOX_SIZE, CHECK_FILTER, () -> useFilter, this::onCheckFilterToggle);
                this.filterSlot = new FilterSlot(owner, 0, 0, SLOT_FILTER, FoupItem::canPlaceInFoup);
                this.filterSlot.setFilter(entry.filter().orElse(ItemStack.EMPTY));
                this.checkCount = new IndicatorButton(0, 0, CHECKBOX_SIZE, CHECKBOX_SIZE, CHECK_COUNT, () -> useCount, this::onCheckCountToggle);
                this.boxCount = new StackSizeCycleBox(owner.font, 0, 0, STACKSIZE_WIDTH, ELEM_HEIGHT, CYCLE_COUNT);
                entry.count().ifPresent(this.boxCount::setValue);
                this.buttonEdit = SpriteIconButton.builder(BUTTON_EDIT, this::edit, true)
                        .size(SPRITE_SIZE, SPRITE_SIZE)
                        .sprite(ICON_EDIT, SPRITE_SIZE, SPRITE_SIZE)
                        .build();
                this.buttonSave = SpriteIconButton.builder(BUTTON_SAVE, this::save, true)
                        .size(SPRITE_SIZE, SPRITE_SIZE)
                        .sprite(ICON_SAVE, SPRITE_SIZE, SPRITE_SIZE)
                        .build();
                this.buttonDelete = SpriteIconButton.builder(BUTTON_DELETE, this::delete, true)
                        .size(SPRITE_SIZE, SPRITE_SIZE)
                        .sprite(ICON_DELETE, SPRITE_SIZE, SPRITE_SIZE)
                        .build();
                this.children = List.of(buttonUp, buttonDown, boxStation, buttonAction, checkFilter, filterSlot, checkCount, boxCount, buttonEdit, buttonSave, buttonDelete);
                this.validity = getValidity();
            }

            @Override
            public void renderBack(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick)
            {
                validity = getValidity();
                graphics.renderOutline(left - 1, top - 1, width + 2, height + 2, validity == EntryValidity.VALID ? 0xFF444444 : 0xFFAA0000);
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick)
            {
                buttonUp.active = index != 0;
                buttonUp.setPosition(left + MOVE_BTN_X, top + EDGE_PADDING);

                buttonDown.active = index != list().children().size() - 1;
                buttonDown.setPosition(left + MOVE_BTN_X, top + height - EDGE_PADDING - MOVE_BTN_HEIGHT);

                boxStation.active = mutable;
                boxStation.setEditable(mutable);
                boxStation.setPosition(left + LEFT_ELEM_X, top + EDGE_PADDING);

                buttonAction.active = mutable;
                buttonAction.setPosition(left + LEFT_ELEM_X, top + height - EDGE_PADDING - ELEM_HEIGHT);

                checkFilter.visible = buttonAction.getValue() == StationAction.LOAD;
                checkFilter.active = mutable;
                checkFilter.setPosition(left + CHECKBOX_X, top + EDGE_PADDING);

                filterSlot.visible = buttonAction.getValue() == StationAction.LOAD;
                filterSlot.active = useFilter && mutable;
                filterSlot.setPosition(left + FILTER_X, top + EDGE_PADDING);

                checkCount.visible = type == StationType.LOADER;
                checkCount.active = mutable;
                checkCount.setPosition(left + CHECKBOX_X, top + height - EDGE_PADDING - ELEM_HEIGHT);

                boxCount.visible = type == StationType.LOADER;
                boxCount.active = useCount && mutable;
                boxCount.setEditable(useCount && mutable);
                boxCount.setPosition(left + FILTER_X, top + height - EDGE_PADDING - ELEM_HEIGHT);

                buttonEdit.visible = !mutable;
                buttonEdit.setPosition(left + width - EDGE_PADDING - SPRITE_SIZE, top + EDGE_PADDING);

                buttonSave.visible = mutable;
                buttonSave.active = mutable && validity == EntryValidity.VALID;
                buttonSave.setPosition(left + width - EDGE_PADDING - SPRITE_SIZE, top + EDGE_PADDING);

                buttonDelete.setPosition(left + width - EDGE_PADDING - SPRITE_SIZE, top + height - EDGE_PADDING - ELEM_HEIGHT);

                if (filterSlot.visible)
                {
                    graphics.drawString(owner.font, LABEL_FILTER, left + FILTER_LABEL_X, top + FILTER_LABEL_Y, 0xFFFFFF);
                }
                if (boxCount.visible)
                {
                    graphics.drawString(owner.font, LABEL_COUNT, left + FILTER_LABEL_X, top + height - COUNT_LABEL_Y_OFF, 0xFFFFFF);
                }
                for (AbstractWidget child : children)
                {
                    if (!owner.cartIdle)
                    {
                        child.active = false;
                    }
                    child.render(graphics, mouseX, mouseY, partialTick);
                }
                if (validity != EntryValidity.VALID)
                {
                    graphics.blitSprite(ICON_ERROR, left + ERROR_X, top + ERROR_Y, ERROR_SIZE, ERROR_SIZE);
                    if (mouseX >= left + ERROR_X && mouseX < left + ERROR_X + ERROR_SIZE && mouseY >= top + ERROR_Y && mouseY < top + ERROR_Y + ERROR_SIZE)
                    {
                        owner.setTooltipForNextRenderPass(validity.description);
                    }
                }
            }

            @Override
            public List<? extends GuiEventListener> children()
            {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables()
            {
                return children;
            }

            private EntryValidity getValidity()
            {
                if (type == StationType.UNKNOWN)
                {
                    return EntryValidity.INVALID_STATION;
                }
                if (!owner.stations.containsKey(boxStation.getValue()))
                {
                    return EntryValidity.INVALID_STATION;
                }
                if (type == StationType.LOADER)
                {
                    if (useFilter && filterSlot.getFilter().isEmpty())
                    {
                        return EntryValidity.LOADER_FILTER_EMPTY;
                    }
                    if (useCount && (boxCount.isEmpty() || boxCount.getValue() <= 0 || boxCount.getValue() > Item.ABSOLUTE_MAX_STACK_SIZE))
                    {
                        return EntryValidity.NO_COUNT_SET;
                    }
                }
                return EntryValidity.VALID;
            }

            private void moveUp(Button btn)
            {
                int idx = list().children().indexOf(this);
                if (idx > 0)
                {
                    move(idx, -1);
                }
            }

            private void moveDown(Button btn)
            {
                int idx = list().children().indexOf(this);
                if (idx < list().children().size() - 1)
                {
                    move(idx, 1);
                }
            }

            private void move(int idx, int dir)
            {
                list().children().remove(idx);
                list().children().add(idx + dir, this);

                PacketDistributor.sendToServer(new ServerboundMoveScheduleEntryPayload(owner.cart.getId(), idx, dir > 0, entry.uuid()));
            }

            private void onStationChange(String name)
            {
                type = owner.stations.getOrDefault(name, StationType.UNKNOWN);
                if (type == StationType.STORAGE)
                {
                    useCount = false;
                    boxCount.setEmpty();
                }
            }

            private void onActionChange(CycleButton<StationAction> btn, StationAction action)
            {
                if (action == StationAction.UNLOAD)
                {
                    useFilter = false;
                    filterSlot.setFilter(ItemStack.EMPTY);
                }
            }

            private void onCheckFilterToggle(Button btn)
            {
                useFilter = !useFilter;
                if (!useFilter)
                {
                    filterSlot.setFilter(ItemStack.EMPTY);
                }
            }

            private void onCheckCountToggle(Button btn)
            {
                useCount = !useCount;
                if (!useCount)
                {
                    boxCount.setEmpty();
                }
            }

            private void edit(Button btn)
            {
                mutable = true;
            }

            private void save(Button btn)
            {
                mutable = false;

                String station = boxStation.getValue();
                StationType type = owner.stations.get(station);
                Optional<ItemStack> filter = Optional.empty();
                if (buttonAction.getValue() == StationAction.LOAD && useFilter)
                {
                    filter = Optional.of(filterSlot.getFilter());
                }
                OptionalInt count = OptionalInt.empty();
                if (type == StationType.LOADER && useCount)
                {
                    count = OptionalInt.of(boxCount.getValue());
                }
                entry = new Schedule.Entry(entry.uuid(), station, type, buttonAction.getValue(), filter, count);

                int idx = list().children().indexOf(this);
                PacketDistributor.sendToServer(new ServerboundEditScheduleEntryPayload(owner.cart.getId(), idx, entry));
            }

            private void delete(Button btn)
            {
                list().removeEntry(this);

                int idx = list().children().indexOf(this);
                PacketDistributor.sendToServer(new ServerboundDeleteScheduleEntryPayload(owner.cart.getId(), idx, entry.uuid()));
            }

            @SuppressWarnings("deprecation")
            private ScheduleList list()
            {
                return (ScheduleList) list;
            }
        }
    }

    public enum EntryValidity
    {
        VALID,
        INVALID_STATION,
        LOADER_FILTER_EMPTY,
        NO_COUNT_SET;

        private final Component description = Component.translatable(
                "msg.foup.overhead_cart.schedule.error." + toString().toLowerCase(Locale.ROOT)
        );

        public Component getDescription()
        {
            return description;
        }
    }
}
