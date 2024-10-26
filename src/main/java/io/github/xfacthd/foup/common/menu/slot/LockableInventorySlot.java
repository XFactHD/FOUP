package io.github.xfacthd.foup.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntPredicate;

public final class LockableInventorySlot extends Slot
{
    private final IntPredicate isLocked;

    public LockableInventorySlot(Container container, int slot, int xPosition, int yPosition, IntPredicate isLocked)
    {
        super(container, slot, xPosition, yPosition);
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
        return isLocked.test(getSlotIndex());
    }
}
