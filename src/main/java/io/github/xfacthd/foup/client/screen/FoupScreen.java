package io.github.xfacthd.foup.client.screen;

import io.github.xfacthd.foup.common.menu.FoupMenu;
import io.github.xfacthd.foup.common.menu.slot.LockableInventorySlot;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FoupScreen extends AbstractContainerScreen<FoupMenu>
{
    private static final ResourceLocation BACKGROUND = Utils.rl("textures/gui/foup.png");
    private static final ResourceLocation LOCK_ICON = ResourceLocation.withDefaultNamespace("container/cartography_table/locked");
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
        graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString)
    {
        super.renderSlotContents(graphics, stack, slot, countString);
        if (slot instanceof LockableInventorySlot lockableSlot && lockableSlot.isLocked())
        {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.blitSprite(RenderType::guiTextured, LOCK_ICON, slot.x + SLOT_SIZE_INNER - 5, slot.y + SLOT_SIZE_INNER - 7, 5, 7);
            graphics.pose().popPose();
        }
    }
}
