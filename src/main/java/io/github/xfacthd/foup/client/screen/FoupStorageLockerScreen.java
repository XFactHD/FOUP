package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.common.menu.FoupStorageLockerMenu;
import io.github.xfacthd.foup.common.menu.slot.LockableSlot;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FoupStorageLockerScreen extends AbstractContainerScreen<FoupStorageLockerMenu>
{
    private static final ResourceLocation BACKGROUND = Utils.rl("textures/gui/foup_storage_locker.png");
    private static final ResourceLocation LOCK_ICON = ResourceLocation.withDefaultNamespace("container/cartography_table/locked");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 174;
    private static final int SLOT_SIZE_INNER = 16;

    public FoupStorageLockerScreen(FoupStorageLockerMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY += 9;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, WIDTH, HEIGHT);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString)
    {
        super.renderSlotContents(graphics, stack, slot, countString);
        if (slot instanceof LockableSlot lockableSlot && lockableSlot.isLocked())
        {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.blitSprite(LOCK_ICON, slot.x + SLOT_SIZE_INNER - 5, slot.y + SLOT_SIZE_INNER - 7, 5, 7);
            graphics.pose().popPose();
        }
    }
}
