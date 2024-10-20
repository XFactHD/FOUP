package io.github.xfacthd.foup.common.data.capability.itemhandler;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.IntPredicate;

public final class ExternalItemHandler implements IItemHandler
{
    private final IItemHandler wrapped;
    private final IntPredicate canInsert;
    private final IntPredicate canExtract;

    public ExternalItemHandler(IItemHandler wrapped, IntPredicate canInsert, IntPredicate canExtract)
    {
        this.wrapped = wrapped;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int getSlots()
    {
        return wrapped.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return wrapped.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!canInsert.test(slot)) return stack;
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (!canExtract.test(slot)) return ItemStack.EMPTY;
        return wrapped.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return wrapped.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return wrapped.isItemValid(slot, stack);
    }
}
