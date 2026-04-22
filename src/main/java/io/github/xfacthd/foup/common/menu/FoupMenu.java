package io.github.xfacthd.foup.common.menu;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import io.github.xfacthd.foup.common.item.FoupItem;
import io.github.xfacthd.foup.common.menu.slot.LockableInventorySlot;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.world.inventory.StackCopySlot;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class FoupMenu extends AbstractContainerMenu {
    private static final int FOUP_SLOTS = 1;

    private final int hotbarSlot;
    @Nullable
    private final ItemStack foupStack;

    public FoupMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ClientSlot::new, null);
    }

    public FoupMenu(int containerId, Inventory inventory, ItemStack foupStack) {
        this(containerId, inventory, (x, y) -> new ServerSlot(x, y, foupStack), foupStack);
    }

    private FoupMenu(int containerId, Inventory inventory, FoupSlotFactory foupSlotFactory, @Nullable ItemStack foupStack) {
        super(FoupContent.MENU_TYPE_FOUP.value(), containerId);
        this.hotbarSlot = inventory.getSelectedSlot();
        this.foupStack = foupStack;
        addSlot(foupSlotFactory.create(80, 35));
        Utils.addPlayerInvSlots(this::addSlot, inventory, 8, 84, this::makeInventorySlot);
    }

    private Slot makeInventorySlot(Container container, int idx, int x, int y) {
        if (idx < 9) {
            return new LockableInventorySlot(container, idx, x, y, i -> i == hotbarSlot);
        }
        return new Slot(container, idx, x, y);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack remainder = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            remainder = stack.copy();
            if (index < FOUP_SLOTS) {
                if (!moveItemStackTo(stack, FOUP_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, FOUP_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return remainder;
    }

    @Override
    public boolean stillValid(Player player) {
        Objects.requireNonNull(foupStack, "FoupMenu#stillValid() called on client");

        Inventory inv = player.getInventory();
        return inv.getSelectedSlot() == hotbarSlot && inv.getSelectedItem() == foupStack;
    }

    @FunctionalInterface
    private interface FoupSlotFactory {
        Slot create(int x, int y);
    }

    private static final class ServerSlot extends StackCopySlot {
        private final ItemStack foup;

        public ServerSlot(int x, int y, ItemStack foup) {
            super(0, x, y);
            this.foup = foup;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FoupItem.canPlaceInFoup(stack);
        }

        @Override
        protected ItemStack getStackCopy() {
            return foup.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY).stack();
        }

        @Override
        protected void setStackCopy(ItemStack stack) {
            foup.set(FoupContent.DC_TYPE_ITEM_CONTENTS, new ItemContents(stack));
        }
    }

    private static final class ClientSlot extends Slot {
        public ClientSlot(int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FoupItem.canPlaceInFoup(stack);
        }
    }
}
