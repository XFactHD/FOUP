package io.github.xfacthd.foup.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

public final class FilterSlot extends AbstractWidget {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("container/slot");

    private final AbstractContainerScreen<?> owner;
    private final Predicate<ItemStack> preFilter;
    private ItemStack filter = ItemStack.EMPTY;

    public FilterSlot(AbstractContainerScreen<?> owner, int x, int y, Component message, Predicate<ItemStack> preFilter) {
        super(x, y, 18, 18, message);
        this.owner = owner;
        this.preFilter = preFilter;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX(), getY(), width, height);
        graphics.fakeItem(filter, getX() + 1, getY() + 1);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (active && isMouseOver(event.x(), event.y())) {
            ItemStack carried = owner.getMenu().getCarried();
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && carried.isEmpty()) {
                filter = ItemStack.EMPTY;
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && !carried.isEmpty() && preFilter.test(carried)) {
                filter = carried.copy();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    public boolean setFilterFromDrop(ItemStack stack) {
        if (active && preFilter.test(stack)) {
            setFilter(stack);
            return true;
        }
        return false;
    }

    public void setFilter(ItemStack filter) {
        this.filter = filter;
    }

    public ItemStack getFilter() {
        return filter;
    }
}
