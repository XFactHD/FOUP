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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

// TODO: add description tooltips for filter and count
public final class OverheadCartScreen extends Screen
{
    public static final Component SCREEN_TITLE = Component.translatable("screen.foup.overhead_hoist_cart");
    private static final ResourceLocation BACKGROUND = Utils.rl("background");
    private static final int WIDTH = 300;
    private static final int HEIGHT = 240;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
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
    private int leftPos;
    private int topPos;
    private Button buttonExecute;
    private Button buttonStop;
    private ScheduleList scheduleList;
    private boolean cartIdle;

    public OverheadCartScreen(OverheadCartEntity cart, List<Schedule.Entry> scheduleEntries, Map<String, StationType> stations)
    {
        super(SCREEN_TITLE);
        this.cart = cart;
        this.scheduleEntries = scheduleEntries;
        this.stations = stations;
        this.cartIdle = cart.getState() == OverheadCartState.IDLE;
    }

    @Override
    protected void init()
    {
        leftPos = (width - WIDTH) / 2;
        topPos = (height - HEIGHT) / 2;

        addRenderableWidget(Button.builder(BUTTON_ADD_ENTRY, this::addStation)
                .pos(leftPos + WIDTH - 8 - 60 - 5 - 90, topPos + 26 + 24)
                .size(90, 14)
                .build()
        );
        buttonExecute = addRenderableWidget(Button.builder(BUTTON_EXEC, this::requestExecute)
                .pos(leftPos + WIDTH - 8 - 60, topPos + 26 + 24)
                .size(60, 14)
                .build()
        );
        buttonStop = addRenderableWidget(Button.builder(BUTTON_STOP, this::requestStop)
                .pos(leftPos + WIDTH - 8 - 60, topPos + 26 + 24)
                .size(60, 14)
                .build()
        );

        buttonExecute.visible = cartIdle;
        buttonStop.visible = !cartIdle;

        ScheduleList oldList = scheduleList;
        scheduleList = addRenderableWidget(new ScheduleList(minecraft, leftPos + 8, topPos + 26 + 41, HEIGHT - 14 - 20 - 41, scheduleList));
        if (oldList == null)
        {
            rebuiltScheduleList();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderTransparentBackground(graphics);
        graphics.blitSprite(BACKGROUND, leftPos, topPos, WIDTH, HEIGHT);
        graphics.drawString(font, title, leftPos + TITLE_X, topPos + TITLE_Y, 0x404040, false);

        OverheadCartIssue issue = cart.getIssue();
        boolean hasIssue = issue != null;

        graphics.drawString(font, LABEL_STATE, leftPos + TITLE_X, topPos + TITLE_Y + 15, 0x404040, false);
        if (hasIssue)
        {
            graphics.drawString(font, LABEL_ISSUE, leftPos + TITLE_X, topPos + TITLE_Y + 27, 0x404040, false);
        }

        int offset = Math.max(font.width(LABEL_STATE), hasIssue ? font.width(LABEL_ISSUE) : 0) + 4;
        graphics.drawString(font, cart.getState().getTranslation(), leftPos + TITLE_X + offset, topPos + TITLE_Y + 15, 0x404040, false);
        if (hasIssue)
        {
            graphics.drawString(font, formatIssue(issue), leftPos + TITLE_X + offset, topPos + TITLE_Y + 15, 0x404040, false);
        }

        graphics.drawString(font, LABEL_SCHEDULE, leftPos + TITLE_X, topPos + 26 + 28, 0x404040, false);
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
    public void tick()
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

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private static final class ScheduleList extends ContainerObjectSelectionList<ScheduleList.ScheduleEntry>
    {
        public ScheduleList(Minecraft minecraft, int x, int y, int height, @Nullable ScheduleList oldList)
        {
            super(minecraft, WIDTH - 16, height, y, 48);
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
            return width - 15;
        }

        @Override
        protected int getListOutlinePadding()
        {
            return 4;
        }

        private static final class ScheduleEntry extends Entry<ScheduleEntry>
        {
            private static final ResourceLocation ICON_UP = ResourceLocation.withDefaultNamespace("statistics/sort_up");
            private static final ResourceLocation ICON_DOWN = ResourceLocation.withDefaultNamespace("statistics/sort_down");
            private static final ResourceLocation ICON_EDIT = Utils.rl("edit");
            private static final ResourceLocation ICON_SAVE = Utils.rl("save");
            private static final ResourceLocation ICON_DELETE = Utils.rl("delete");
            private static final ResourceLocation ICON_ERROR = ResourceLocation.withDefaultNamespace("icon/unseen_notification");

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
                        .size(18, 14)
                        .sprite(ICON_UP, 18, 18)
                        .build();
                this.buttonDown = SpriteIconButton.builder(BUTTON_MOVE_DOWN, this::moveDown, true)
                        .size(17, 14)
                        .sprite(ICON_DOWN, 18, 18)
                        .build();
                this.boxStation = new EditBox(owner.font, 100, 18, SELECT_STATION); // TODO: replace with combo box or provide auto-complete suggestions
                this.boxStation.setValue(entry.station());
                this.boxStation.setResponder(this::onStationChange);
                this.buttonAction = CycleButton.builder(StationAction::getTranslation)
                        .withValues(StationAction.values())
                        .withInitialValue(entry.action())
                        .create(0, 0, 100, 18, BUTTON_ACTION, this::onActionChange);
                this.checkFilter = new IndicatorButton(0, 0, 18, 18, CHECK_FILTER, () -> useFilter, this::onCheckFilterToggle);
                this.filterSlot = new FilterSlot(0, 0, SLOT_FILTER);
                this.filterSlot.setFilter(entry.filter().orElse(ItemStack.EMPTY));
                this.checkCount = new IndicatorButton(0, 0, 18, 18, CHECK_COUNT, () -> useCount, this::onCheckCountToggle);
                this.boxCount = new StackSizeCycleBox(owner.font, 0, 0, 60, 18, CYCLE_COUNT);
                entry.count().ifPresent(this.boxCount::setValue);
                this.buttonEdit = SpriteIconButton.builder(BUTTON_EDIT, this::edit, true)
                        .size(18, 18)
                        .sprite(ICON_EDIT, 18, 18)
                        .build();
                this.buttonSave = SpriteIconButton.builder(BUTTON_SAVE, this::save, true)
                        .size(18, 18)
                        .sprite(ICON_SAVE, 18, 18)
                        .build();
                this.buttonDelete = SpriteIconButton.builder(BUTTON_DELETE, this::delete, true).size(18, 18)
                        .sprite(ICON_DELETE, 18, 18)
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
                buttonUp.setPosition(left + 2, top + 2);

                buttonDown.active = index != list().children().size() - 1;
                buttonDown.setPosition(left + 2, top + height - 16);

                boxStation.active = mutable;
                boxStation.setEditable(mutable);
                boxStation.setPosition(left + 22, top + 2);

                buttonAction.active = mutable;
                buttonAction.setPosition(left + 22, top + height - 20);

                checkFilter.visible = buttonAction.getValue() == StationAction.LOAD;
                checkFilter.active = mutable;
                checkFilter.setPosition(left + 158, top + 2);

                filterSlot.visible = buttonAction.getValue() == StationAction.LOAD;
                filterSlot.active = useFilter && mutable;
                filterSlot.setPosition(left + 180, top + 2);

                checkCount.visible = type == StationType.LOADER;
                checkCount.active = mutable;
                checkCount.setPosition(left + 158, top + height - 20);

                boxCount.visible = type == StationType.LOADER;
                boxCount.active = useCount && mutable;
                boxCount.setEditable(useCount && mutable);
                boxCount.setPosition(left + 180, top + height - 20);

                buttonEdit.visible = !mutable;
                buttonEdit.setPosition(left + width - 20, top + 2);

                buttonSave.visible = mutable;
                buttonSave.active = mutable && validity == EntryValidity.VALID;
                buttonSave.setPosition(left + width - 20, top + 2);

                buttonDelete.setPosition(left + width - 20, top + height - 20);

                if (filterSlot.visible)
                {
                    graphics.drawString(owner.font, LABEL_FILTER, left + 126, top + 7, 0xFFFFFF);
                }
                if (boxCount.visible)
                {
                    graphics.drawString(owner.font, LABEL_COUNT, left + 126, top + height - 14, 0xFFFFFF);
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
                    graphics.blitSprite(ICON_ERROR, left + 6, top + 17, 10, 10);
                    if (mouseX >= left + 6 && mouseX < left + 16 && mouseY >= top + 17 && mouseY < top + 27)
                    {
                        graphics.renderTooltip(owner.font, validity.description, mouseX, mouseY);
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
