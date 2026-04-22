// FIXME: RL->ID
/*package io.github.xfacthd.foup.common.compat.emi;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class AddFoupToCartEmiRecipe extends EmiPatternCraftingRecipe {
    public AddFoupToCartEmiRecipe(Identifier id) {
        super(List.of(EmiStack.of(FoupContent.ITEM_CART), EmiStack.of(FoupContent.ITEM_FOUP)), createOutput(), id, true);
    }

    @Override
    public SlotWidget getInputWidget(int slot, int x, int y) {
        if (slot == 0) {
            ItemStack stack = FoupContent.ITEM_CART.toStack();
            return new SlotWidget(EmiStack.of(stack, 1), x, y);
        }
        if (slot == 1) {
            ItemStack stack = FoupContent.ITEM_FOUP.toStack();
            return new SlotWidget(EmiStack.of(stack, 1), x, y);
        }
        return new SlotWidget(EmiStack.EMPTY, x, y);
    }

    @Override
    public SlotWidget getOutputWidget(int x, int y) {
        return new SlotWidget(createOutput(), x, y);
    }

    private static EmiStack createOutput() {
        ItemStack stack = FoupContent.ITEM_CART.toStack();
        stack.set(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.of(ItemStack.EMPTY));
        return EmiStack.of(stack);
    }
}
*/