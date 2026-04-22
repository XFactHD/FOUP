package io.github.xfacthd.foup.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.lwjgl.glfw.GLFW;

public final class StackSizeCycleBox extends AbstractWidget {
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/text_field"),
            Identifier.withDefaultNamespace("widget/text_field_highlighted")
    );
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_disabled"),
            Identifier.withDefaultNamespace("widget/button_highlighted")
    );
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int TEXT_COLOR_UNEDITABLE = 0xFF707070;
    private static final int MIN = 1;
    private static final int MAX = Item.ABSOLUTE_MAX_STACK_SIZE;
    private static final Identifier ICON_SORT_UP = Identifier.withDefaultNamespace("statistics/sort_up");
    private static final Identifier ICON_SORT_DOWN = Identifier.withDefaultNamespace("statistics/sort_down");

    private final Font font;
    private int value;
    private boolean editable = true;
    private boolean empty = true;

    public StackSizeCycleBox(Font font, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.font = font;
        this.value = MIN;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Identifier background = BACKGROUND_SPRITES.get(isActive(), isFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, background, getX(), getY(), width - 13, height);

        boolean overButtons = isActive() && isHovered && mouseX >= getX() + width - 13;
        boolean hoveredUp = overButtons && mouseY < getY() + height / 2;
        boolean hoveredDown = overButtons && mouseY >= getY() + height / 2;

        Identifier buttonUp = BUTTON_SPRITES.get(isActive() && editable && value < MAX, hoveredUp);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, buttonUp, getX() + width - 13, getY(), 13, height / 2);
        Identifier buttonDown = BUTTON_SPRITES.get(isActive() && editable && value > MIN, hoveredDown);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, buttonDown, getX() + width - 13, getY() + height / 2, 13, height / 2);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_SORT_UP, getX() + width - 14, getY() + (height / 4) - 6, 14, 14);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON_SORT_DOWN, getX() + width - 14, getY() + (height / 2) + (height / 4) - 7, 14, 14);

        if (!empty) {
            String text = Integer.toString(getValue());
            int x = getX() + width - 13 - 4 - font.width(text);
            int color = editable ? TEXT_COLOR : TEXT_COLOR_UNEDITABLE;
            graphics.text(font, text, x, getY() + (height - 8) / 2, color, true);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean overButtons = active && editable && isHovered && event.x() >= getX() + width - 13;
        if (overButtons && value < MAX && event.y() < getY() + height / 2F) {
            if (empty) {
                empty = false;
            } else {
                value++;
            }
        } else if (overButtons && value > MIN && event.y() >= getY() + height / 2F) {
            value--;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (isActive() && editable && isFocused() && Character.isDigit(event.codepoint())) {
            int digit = Character.digit(event.codepoint(), 10);
            if (empty) {
                if (digit >= MIN) {
                    value = digit;
                    empty = false;
                }
            } else {
                int newValue = value * 10 + digit;
                if (newValue <= MAX) {
                    value = newValue;
                }
            }
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isActive() && editable && isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!empty) {
                    if (value < 10) {
                        setEmpty();
                    } else {
                        value /= 10;
                    }
                }
                return true;
            } else if (event.key() == GLFW.GLFW_KEY_DELETE) {
                if (!empty) {
                    if (value < 10) {
                        setEmpty();
                    } else {
                        int mult = 1;
                        int toDel = value;
                        while (toDel > 10) {
                            toDel /= 10;
                            mult *= 10;
                        }
                        value = value - (toDel * mult);
                    }
                }
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        this.empty = false;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty() {
        this.empty = true;
        this.value = MIN;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
