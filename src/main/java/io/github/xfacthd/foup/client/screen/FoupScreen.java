package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.common.menu.FoupMenu;
import io.github.xfacthd.foup.common.menu.slot.LockableInventorySlot;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class FoupScreen extends AbstractContainerScreen<FoupMenu>
{
    private static final Identifier BACKGROUND = Utils.rl("textures/gui/foup.png");
    private static final Identifier LOCK_ICON = Identifier.withDefaultNamespace("container/cartography_table/locked");
    private static final int SLOT_SIZE_INNER = 16;

    public FoupScreen(FoupMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
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
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString)
    {
        super.renderSlotContents(graphics, stack, slot, countString);
        if (slot instanceof LockableInventorySlot lockableSlot && lockableSlot.isLocked())
        {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOCK_ICON, slot.x + SLOT_SIZE_INNER - 5, slot.y + SLOT_SIZE_INNER - 7, 5, 7);
        }
    }
}
