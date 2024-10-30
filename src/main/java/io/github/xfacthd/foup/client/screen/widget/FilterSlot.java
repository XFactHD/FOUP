package io.github.xfacthd.foup.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

public final class FilterSlot extends AbstractWidget
{
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("container/slot");

    private final AbstractContainerScreen<?> owner;
    private final Predicate<ItemStack> preFilter;
    private ItemStack filter = ItemStack.EMPTY;

    public FilterSlot(AbstractContainerScreen<?> owner, int x, int y, Component message, Predicate<ItemStack> preFilter)
    {
        super(x, y, 18, 18, message);
        this.owner = owner;
        this.preFilter = preFilter;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.blitSprite(BACKGROUND, getX(), getY(), width, height);
        graphics.renderFakeItem(filter, getX() + 1, getY() + 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (active)
        {
            ItemStack carried = owner.getMenu().getCarried();
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && carried.isEmpty())
            {
                filter = ItemStack.EMPTY;
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !carried.isEmpty() && preFilter.test(carried))
            {
                filter = carried.copy();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    public boolean setFilterFromDrop(ItemStack stack)
    {
        if (active && preFilter.test(stack))
        {
            setFilter(stack);
            return true;
        }
        return false;
    }

    public void setFilter(ItemStack filter)
    {
        this.filter = filter;
    }

    public ItemStack getFilter()
    {
        return filter;
    }
}
