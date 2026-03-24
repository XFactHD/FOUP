package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.common.menu.FoupStorageLockerMenu;
import io.github.xfacthd.foup.common.menu.slot.LockableSlot;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class FoupStorageLockerScreen extends AbstractContainerScreen<FoupStorageLockerMenu>
{
    private static final Identifier BACKGROUND = Utils.rl("textures/gui/foup_storage_locker.png");
    private static final Identifier LOCK_ICON = Identifier.withDefaultNamespace("container/cartography_table/locked");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 174;
    private static final int SLOT_SIZE_INNER = 16;

    public FoupStorageLockerScreen(FoupStorageLockerMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title, WIDTH, HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, WIDTH, HEIGHT, 256, 256);
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, @Nullable String countString)
    {
        super.renderSlotContents(graphics, stack, slot, countString);
        if (slot instanceof LockableSlot lockableSlot && lockableSlot.isLocked())
        {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOCK_ICON, slot.x + SLOT_SIZE_INNER - 5, slot.y + SLOT_SIZE_INNER - 7, 5, 7);
        }
    }
}
