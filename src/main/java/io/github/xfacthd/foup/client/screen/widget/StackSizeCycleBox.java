package io.github.xfacthd.foup.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.lwjgl.glfw.GLFW;

public final class StackSizeCycleBox extends AbstractWidget
{
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/text_field"),
            ResourceLocation.withDefaultNamespace("widget/text_field_highlighted")
    );
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/button"),
            ResourceLocation.withDefaultNamespace("widget/button_disabled"),
            ResourceLocation.withDefaultNamespace("widget/button_highlighted")
    );
    private static final int TEXT_COLOR = 14737632;
    private static final int TEXT_COLOR_UNEDITABLE = 7368816;
    private static final int MIN = 1;
    private static final int MAX = Item.ABSOLUTE_MAX_STACK_SIZE;
    private static final ResourceLocation ICON_SORT_UP = ResourceLocation.withDefaultNamespace("statistics/sort_up");
    private static final ResourceLocation ICON_SORT_DOWN = ResourceLocation.withDefaultNamespace("statistics/sort_down");

    private final Font font;
    private int value;
    private boolean editable = true;
    private boolean empty = true;

    public StackSizeCycleBox(Font font, int x, int y, int width, int height, Component message)
    {
        super(x, y, width, height, message);
        this.font = font;
        this.value = MIN;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        ResourceLocation background = BACKGROUND_SPRITES.get(isActive(), isFocused());
        graphics.blitSprite(RenderType::guiTextured, background, getX(), getY(), width - 13, height);

        boolean overButtons = isActive() && isHovered && mouseX >= getX() + width - 13;
        boolean hoveredUp = overButtons && mouseY < getY() + height / 2;
        boolean hoveredDown = overButtons && mouseY >= getY() + height / 2;

        ResourceLocation buttonUp = BUTTON_SPRITES.get(isActive() && editable && value < MAX, hoveredUp);
        graphics.blitSprite(RenderType::guiTextured, buttonUp, getX() + width - 13, getY(), 13, height / 2);
        ResourceLocation buttonDown = BUTTON_SPRITES.get(isActive() && editable && value > MIN, hoveredDown);
        graphics.blitSprite(RenderType::guiTextured, buttonDown, getX() + width - 13, getY() + height / 2, 13, height / 2);

        graphics.blitSprite(RenderType::guiTextured, ICON_SORT_UP, getX() + width - 14, getY() + (height / 4) - 6, 14, 14);
        graphics.blitSprite(RenderType::guiTextured, ICON_SORT_DOWN, getX() + width - 14, getY() + (height / 2) + (height / 4) - 7, 14, 14);

        if (!empty)
        {
            String text = Integer.toString(getValue());
            int x = getX() + width - 13 - 4 - font.width(text);
            int color = editable ? TEXT_COLOR : TEXT_COLOR_UNEDITABLE;
            graphics.drawString(font, text, x, getY() + (height - 8) / 2, color, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        boolean overButtons = active && editable && isHovered && mouseX >= getX() + width - 13;
        if (overButtons && value < MAX && mouseY < getY() + height / 2F)
        {
            if (empty)
            {
                empty = false;
            }
            else
            {
                value++;
            }
        }
        else if (overButtons && value > MIN && mouseY >= getY() + height / 2F)
        {
            value--;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if (isActive() && editable && isFocused() && Character.isDigit(codePoint))
        {
            int digit = Character.digit(codePoint, 10);
            if (empty)
            {
                if (digit >= MIN)
                {
                    value = digit;
                    empty = false;
                }
            }
            else
            {
                int newValue = value * 10 + digit;
                if (newValue <= MAX)
                {
                    value = newValue;
                }
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (isActive() && editable && isFocused())
        {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
            {
                if (!empty)
                {
                    if (value < 10)
                    {
                        setEmpty();
                    }
                    else
                    {
                        value /= 10;
                    }
                }
                return true;
            }
            else if (keyCode == GLFW.GLFW_KEY_DELETE)
            {
                if (!empty)
                {
                    if (value < 10)
                    {
                        setEmpty();
                    }
                    else
                    {
                        int mult = 1;
                        int toDel = value;
                        while (toDel > 10)
                        {
                            toDel /= 10;
                            mult *= 10;
                        }
                        value = value - (toDel * mult);
                    }
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
        this.empty = false;
    }

    public boolean isEmpty()
    {
        return empty;
    }

    public void setEmpty()
    {
        this.empty = true;
        this.value = MIN;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }
}
