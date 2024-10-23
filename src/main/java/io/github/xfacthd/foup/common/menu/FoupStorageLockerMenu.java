package io.github.xfacthd.foup.common.menu;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public final class FoupStorageLockerMenu extends AbstractContainerMenu
{
    private final DataSlot lockedSlot;
    private final Predicate<Player> stillValid;
    private final IntSupplier lockedSlotSupplier;

    public FoupStorageLockerMenu(int containerId, Inventory inventory)
    {
        this(containerId, inventory, new ItemStackHandler(8), p -> true, () -> -1);
    }

    public FoupStorageLockerMenu(int containerId, Inventory inventory, ItemStackHandler beInv, Predicate<Player> stillValid, IntSupplier lockedSlotSupplier)
    {
        super(FoupContent.MENU_TYPE_FOUP_STORAGE_LOCKER.value(), containerId);
        this.stillValid = stillValid;
        this.lockedSlotSupplier = lockedSlotSupplier;
        for (int i = 0; i < 8; i++)
        {
            int x = 44 + (i % 2 * 18) + (i >= 4 ? 54 : 0);
            int y = 30 + (i % 4 >= 2 ? 0 : 18);
            addSlot(new LockerSlot(beInv, i, x, y));
        }
        Utils.addPlayerInvSlots(this::addSlot, inventory, 8, 92);
        this.lockedSlot = addDataSlot(DataSlot.standalone());
        this.lockedSlot.set(lockedSlotSupplier.getAsInt());
    }

    @Override
    public void broadcastChanges()
    {
        lockedSlot.set(lockedSlotSupplier.getAsInt());
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack remainder = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem())
        {
            ItemStack stack = slot.getItem();
            remainder = stack.copy();
            if (index < 8)
            {
                if (!moveItemStackTo(stack, 8, slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!moveItemStackTo(stack, 0, 8, false))
            {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }
        return remainder;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return stillValid.test(player);
    }

    public final class LockerSlot extends SlotItemHandler
    {
        public LockerSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
        {
            super(itemHandler, index, xPosition, yPosition);
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
            return lockedSlot.get() == index;
        }
    }
}
