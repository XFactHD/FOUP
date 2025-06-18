package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.client.screen.widget.IndicatorButton;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.menu.FoupLoaderMenu;
import io.github.xfacthd.foup.common.menu.slot.LockableSlot;
import io.github.xfacthd.foup.common.network.payload.serverbound.ServerboundToggleLoaderAutoEjectPayload;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FoupLoaderScreen extends AbstractContainerScreen<FoupLoaderMenu>
{
    private static final ResourceLocation BACKGROUND = Utils.rl("textures/gui/foup_loader.png");
    private static final ResourceLocation LOCK_ICON = ResourceLocation.withDefaultNamespace("container/cartography_table/locked");
    private static final ResourceLocation PROGRESS_ICON = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final ResourceLocation CROSS_ICON = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 174;
    private static final int SLOT_SIZE_INNER = 16;
    private static final int FOUP_X = WIDTH / 2 - 24;
    private static final int FOUP_Y = 16;
    private static final int ARROW_LOAD_X = 46;
    private static final int ARROW_UNLOAD_X = 108;
    private static final int ARROW_Y = 39;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int CROSS_OFFSET_X = 3;
    private static final int CROSS_OFFSET_Y = -2;
    private static final int CROSS_SIZE = 18;
    private static final int AUTO_EJECT_BUTTON_WIDTH = 80;
    public static final Component MSG_LOADING_BLOCKED = Component.translatable("msg.foup.foup_loader.loading_blocked");
    public static final Component MSG_UNLOADING_BLOCKED = Component.translatable("msg.foup.foup_loader.unloading_blocked");
    public static final Component BUTTON_AUTO_EJECT = Component.translatable("button.foup.foup_loader.auto_eject");
    private static final int INTERACT_DURATION = AbstractCartInteractorBlockEntity.State.INTERACTING.getDuration(StationType.LOADER);

    private Button autoEjectButton;

    public FoupLoaderScreen(FoupLoaderMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY += 9;
    }

    @Override
    protected void init()
    {
        super.init();

        autoEjectButton = addRenderableWidget(new IndicatorButton(
                leftPos + WIDTH - 5 - AUTO_EJECT_BUTTON_WIDTH,
                topPos + 5,
                AUTO_EJECT_BUTTON_WIDTH,
                Button.DEFAULT_HEIGHT,
                BUTTON_AUTO_EJECT,
                menu::isAutoEject,
                this::toggleAutoEject
        ));

        checkButtonState();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, WIDTH, HEIGHT, 256, 256);

        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + FOUP_X, topPos + FOUP_Y);
        graphics.pose().scale(3F, 3F);
        graphics.renderFakeItem(FoupContent.ITEM_FOUP.toStack(), 0, 0, 0);
        graphics.pose().popMatrix();

        StationAction action = menu.getActiveAction();
        if (action == null) return;

        AbstractCartInteractorBlockEntity.State state = menu.getState();
        int x = action == StationAction.LOAD ? ARROW_LOAD_X : ARROW_UNLOAD_X;
        if (state == AbstractCartInteractorBlockEntity.State.INTERACTING)
        {
            float factor = 1F - (menu.getRemainingDuration() / (float) INTERACT_DURATION);
            int width = (int) (ARROW_WIDTH * factor);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ICON, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, leftPos + x, topPos + ARROW_Y, width, ARROW_HEIGHT);
        }
        else if (state == AbstractCartInteractorBlockEntity.State.BLOCKED)
        {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CROSS_ICON, leftPos + x + CROSS_OFFSET_X, topPos + ARROW_Y + CROSS_OFFSET_Y, CROSS_SIZE, CROSS_SIZE);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (menu.getState() != AbstractCartInteractorBlockEntity.State.BLOCKED) return;

        StationAction action = menu.getActiveAction();
        if (action == null) return;

        boolean load = action == StationAction.LOAD;
        int minX = leftPos + (load ? ARROW_LOAD_X : ARROW_UNLOAD_X) + CROSS_OFFSET_X;
        int minY = topPos + ARROW_Y + CROSS_OFFSET_Y;
        if (mouseX >= minX && mouseX < minX + CROSS_SIZE && mouseY >= minY && mouseY < minY + CROSS_SIZE)
        {
            graphics.setTooltipForNextFrame(font, load ? MSG_LOADING_BLOCKED : MSG_UNLOADING_BLOCKED, mouseX, mouseY);
        }
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString)
    {
        super.renderSlotContents(graphics, stack, slot, countString);
        if (slot instanceof LockableSlot lockableSlot && lockableSlot.isLocked())
        {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOCK_ICON, slot.x + SLOT_SIZE_INNER - 5, slot.y + SLOT_SIZE_INNER - 7, 5, 7);
        }
    }

    @Override
    protected void containerTick()
    {
        checkButtonState();
    }

    private void checkButtonState()
    {
        autoEjectButton.active = Objects.requireNonNull(Minecraft.getInstance().player).mayBuild();
    }

    private void toggleAutoEject(Button button)
    {
        PacketDistributor.sendToServer(new ServerboundToggleLoaderAutoEjectPayload(menu.containerId, !menu.isAutoEject()));
    }
}
