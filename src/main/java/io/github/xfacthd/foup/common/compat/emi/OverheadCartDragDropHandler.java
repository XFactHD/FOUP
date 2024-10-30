package io.github.xfacthd.foup.common.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.xfacthd.foup.client.screen.OverheadCartScreen;
import net.minecraft.world.item.ItemStack;

final class OverheadCartDragDropHandler implements EmiDragDropHandler<OverheadCartScreen>
{
    @Override
    public boolean dropStack(OverheadCartScreen screen, EmiIngredient stack, int x, int y)
    {
        if (stack instanceof EmiStack emiStack)
        {
            ItemStack itemStack = emiStack.getItemStack();
            if (!itemStack.isEmpty())
            {
                return screen.setFilterSlotFromDrop(itemStack, x, y);
            }
        }
        return false;
    }
}
