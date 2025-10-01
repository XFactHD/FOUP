package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.RenameResult;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundRequestStationLinkPayload;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundRequestStationRenamePayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public final class OverheadRailStationScreen extends Screen
{
    public static final Component SCREEN_TITLE = Component.translatable("screen.foup.rail_station");
    private static final ResourceLocation BACKGROUND = Utils.rl("textures/gui/overhead_rail_station.png");
    private static final int WIDTH = 186;
    private static final int HEIGHT = 90;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    public static final Component TITLE_NAME_EDIT = Component.translatable("title.foup.overhead_rail_station.name_edit");
    public static final Component LABEL_NAME = Component.translatable("label.foup.overhead_rail_station.name");
    public static final Component LABEL_STATE = Component.translatable("label.foup.overhead_rail_station.state");
    public static final Component LABEL_ERROR = Component.translatable("label.foup.overhead_rail_station.error");
    public static final Component VALUE_UNLINKED = Component.translatable("value.foup.overhead_rail_station.link.unlinked").withStyle(ChatFormatting.DARK_RED);
    public static final Component VALUE_LINKED_LOADER = Component.translatable("value.foup.overhead_rail_station.link.loader").withStyle(ChatFormatting.DARK_GREEN);
    public static final Component VALUE_LINKED_STORAGE = Component.translatable("value.foup.overhead_rail_station.link.storage").withStyle(ChatFormatting.DARK_GREEN);
    public static final Component MSG_NAME_INVALID = Component.translatable("msg.foup.overhead_rail_station.name.invalid").withStyle(ChatFormatting.DARK_RED);
    public static final Component MSG_NAME_TAKEN = Component.translatable("msg.foup.overhead_rail_station.name.taken").withStyle(ChatFormatting.DARK_RED);
    public static final Component MSG_RENAME_FAILED = Component.translatable("msg.foup.overhead_rail_station.name.rename_failed").withStyle(ChatFormatting.DARK_RED);
    public static final Component MSG_LINK_FAILED = Component.translatable("msg.foup.overhead_rail_station.link.failed").withStyle(ChatFormatting.DARK_RED);
    public static final Component MSG_ALREADY_LINKED = Component.translatable("msg.foup.overhead_rail_station.link.already_linked").withStyle(ChatFormatting.DARK_RED);
    private static final long ERROR_DISPLAY_DURATION = 5000;

    private final BlockPos blockPos;
    private final OverheadRailStationBlockEntity station;
    private int leftPos;
    private int topPos;
    @UnknownNullability
    private EditBox nameEditBox;
    @UnknownNullability
    private Button setNameButton;
    @UnknownNullability
    private Button linkButton;
    private String lastName;
    private boolean renameInProgress = false;
    private boolean linkInProgress = false;
    @Nullable
    private Component lastError = null;
    private long lastErrorStart = 0;

    public OverheadRailStationScreen(BlockPos blockPos, OverheadRailStationBlockEntity station)
    {
        super(SCREEN_TITLE);
        this.blockPos = blockPos;
        this.station = station;
        this.lastName = station.getName();
    }

    @Override
    protected void init()
    {
        leftPos = (width - WIDTH) / 2;
        topPos = (height - HEIGHT) / 2;

        String nameValue = nameEditBox != null ? nameEditBox.getValue() : lastName;
        nameEditBox = addRenderableWidget(new EditBox(font, leftPos + 45, topPos + 18, 95, 20, null, LABEL_NAME));
        nameEditBox.setValue(nameValue);

        setNameButton = addRenderableWidget(Button.builder(Component.literal("Save"), this::requestNameChange)
                .pos(leftPos + WIDTH - 8 - 30, topPos + 18)
                .size(30, 20)
                .build()
        );
        linkButton = addRenderableWidget(Button.builder(Component.literal("Link"), this::requestLink)
                .pos(leftPos + WIDTH - 8 - 30, topPos + 42)
                .size(30, 20)
                .build()
        );

        checkButtonStates();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderTransparentBackground(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, WIDTH, HEIGHT, 256, 256);
        graphics.drawString(font, title, leftPos + TITLE_X, topPos + TITLE_Y, 0xFF404040, false);

        graphics.drawString(font, LABEL_NAME, leftPos + 8, topPos + 24, 0xFF404040, false);

        graphics.drawString(font, LABEL_STATE, leftPos + 8, topPos + 48, 0xFF404040, false);
        Component linkState = switch (station.getLinkedType())
        {
            case LOADER -> VALUE_LINKED_LOADER;
            case STORAGE -> VALUE_LINKED_STORAGE;
            case null, default -> VALUE_UNLINKED;
        };
        graphics.drawString(font, linkState, leftPos + 45, topPos + 48, 0xFF404040, false);

        if (lastError != null)
        {
            graphics.drawString(font, LABEL_ERROR, leftPos + 8, topPos + 72, 0xFF404040, false);
            graphics.drawString(font, lastError, leftPos + 45, topPos + 72, 0xFF404040, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        if (nameEditBox.isFocused() && !nameEditBox.isMouseOver(event.x(), event.y()))
        {
            setFocused(null);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (!nameEditBox.isFocused() && Objects.requireNonNull(minecraft).options.keyInventory.matches(event))
        {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void tick()
    {
        if (station.isRemoved() || !station.isUsableByPlayer(Objects.requireNonNull(Objects.requireNonNull(minecraft).player)))
        {
            onClose();
            return;
        }

        String name = station.getName();
        if (!name.equals(lastName))
        {
            lastName = name;
            nameEditBox.setValue(name);
        }
        checkButtonStates();

        if (lastError != null && System.currentTimeMillis() - lastErrorStart > ERROR_DISPLAY_DURATION)
        {
            lastError = null;
        }
    }

    private void checkButtonStates()
    {
        boolean canEdit = Objects.requireNonNull(Minecraft.getInstance().player).mayBuild();
        String editValue = nameEditBox.getValue();
        nameEditBox.active = canEdit;
        setNameButton.active = !renameInProgress && canEdit && !lastName.equals(editValue) && RailNetwork.isValidStationName(editValue);
        linkButton.active = station.getLinkedType() == null && !linkInProgress && canEdit;
    }

    private void requestNameChange(Button button)
    {
        renameInProgress = true;
        ClientPacketDistributor.sendToServer(new ServerboundRequestStationRenamePayload(blockPos, nameEditBox.getValue()));
    }

    private void requestLink(Button button)
    {
        linkInProgress = true;
        ClientPacketDistributor.sendToServer(new ServerboundRequestStationLinkPayload(blockPos));
    }

    public void onRenameAck(BlockPos pos, RenameResult result)
    {
        if (blockPos.equals(pos))
        {
            renameInProgress = false;
            setError(switch (result)
            {
                case SUCCESS -> null;
                case NAME_INVALID -> MSG_NAME_INVALID;
                case NAME_TAKEN -> MSG_NAME_TAKEN;
                case UNKNOWN -> MSG_RENAME_FAILED;
            });
        }
    }

    public void onLinkAck(BlockPos pos, TriState result)
    {
        if (blockPos.equals(pos))
        {
            linkInProgress = false;
            setError(switch (result)
            {
                case TRUE -> null;
                case DEFAULT -> MSG_ALREADY_LINKED;
                case FALSE -> MSG_LINK_FAILED;
            });
        }
    }

    private void setError(@Nullable Component error)
    {
        if (error != null)
        {
            lastError = error;
            lastErrorStart = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
