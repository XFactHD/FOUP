package io.github.xfacthd.foup.client.screen.widget;

import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;

public final class IndicatorButton extends Button.Plain
{
    private static final Identifier INDICATOR_TEXTURE = Utils.rl("indicator");
    private static final Identifier INDICATOR_CHECKED_TEXTURE = Utils.rl("indicator_checked");
    private static final int INDICATOR_SIZE = 14;

    private final BooleanSupplier checkedSupplier;

    public IndicatorButton(int x, int y, int w, int h, Component text, BooleanSupplier checkedSupplier, OnPress onPress)
    {
        super(x, y, w, h, text, onPress, Button.DEFAULT_NARRATION);
        this.checkedSupplier = checkedSupplier;
    }

    @Override
    public void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderContents(graphics, mouseX, mouseY, partialTick);
        int x = getX() + width - INDICATOR_SIZE - Math.min(3, (width - INDICATOR_SIZE) / 2);
        int y = getY() + (height - INDICATOR_SIZE) / 2;
        boolean checked = checkedSupplier.getAsBoolean();
        Identifier tex = checked ? INDICATOR_CHECKED_TEXTURE : INDICATOR_TEXTURE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, tex, x, y, INDICATOR_SIZE, INDICATOR_SIZE);
    }

    @Override
    protected void renderDefaultLabel(ActiveTextCollector textCollector)
    {
        int minX = getX() + 3;
        int maxX = getX() + getWidth() - INDICATOR_SIZE - 6;
        textCollector.acceptScrollingWithDefaultCenter(getMessage(), minX, maxX, getY(), getY() + getHeight());
    }
}
