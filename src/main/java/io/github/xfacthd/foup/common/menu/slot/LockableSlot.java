package io.github.xfacthd.foup.common.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.function.IntPredicate;

public final class LockableSlot extends SlotItemHandler
{
    private final IntPredicate isLocked;

    public LockableSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, IntPredicate isLocked)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.isLocked = isLocked;
    }

    @Override
    public boolean mayPickup(Player player)
    {
        return !isLocked() && super.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return !isLocked() && super.mayPlace(stack);
    }

    public boolean isLocked()
    {
        return isLocked.test(index);
    }
}
