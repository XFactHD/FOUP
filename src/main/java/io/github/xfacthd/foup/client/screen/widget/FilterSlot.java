package io.github.xfacthd.foup.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// TODO: implement interaction
public final class FilterSlot extends AbstractWidget
{
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("container/slot");

    private ItemStack filter = ItemStack.EMPTY;

    public FilterSlot(int x, int y, Component message)
    {
        super(x, y, 18, 18, message);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.blitSprite(BACKGROUND, getX(), getY(), width, height);
        graphics.renderFakeItem(filter, getX() + 1, getY() + 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    public void setFilter(ItemStack filter)
    {
        this.filter = filter;
    }

    public ItemStack getFilter()
    {
        return filter;
    }
}
