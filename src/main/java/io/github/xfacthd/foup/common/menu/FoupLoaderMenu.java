package io.github.xfacthd.foup.common.menu;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.item.FoupItem;
import io.github.xfacthd.foup.common.menu.slot.LockableSlot;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public final class FoupLoaderMenu extends AbstractCartInteractorMenu {
    private static final int LOADER_SLOTS = 2;
    private static final LoaderStateProvider DUMMY = new LoaderStateProvider() {
        @Override
        public boolean isAutoEject() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAutoEject(boolean autoEject) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbstractCartInteractorBlockEntity.State getState() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable StationAction getActiveAction() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getRemainingDuration() {
            throw new UnsupportedOperationException();
        }
    };

    private final LoaderStateProvider stateProvider;
    private final DataSlot autoEjectSlot;

    public FoupLoaderMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new ClientItemHandler(LOADER_SLOTS), p -> true, DUMMY);
    }

    public FoupLoaderMenu(int containerId, Inventory inventory, ItemStacksResourceHandler beInv, Predicate<Player> stillValid, LoaderStateProvider stateProvider) {
        super(FoupContent.MENU_TYPE_FOUP_LOADER.value(), containerId, stillValid, stateProvider);
        this.stateProvider = stateProvider;
        this.autoEjectSlot = addDataSlot(DataSlot.standalone());
        addSlot(new LockableSlot(beInv, 0, 25, 39, i -> isInputLocked()));
        addSlot(new OutputSlot(beInv, 1, 135, 39));
        Utils.addPlayerInvSlots(this::addSlot, inventory, 8, 92);
    }

    @Override
    public void broadcastChanges() {
        autoEjectSlot.set(stateProvider.isAutoEject() ? 1 : 0);
        super.broadcastChanges();
    }

    public boolean isAutoEject() {
        return autoEjectSlot.get() > 0;
    }

    public void setAutoEject(boolean autoEject) {
        stateProvider.setAutoEject(autoEject);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack remainder = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            remainder = stack.copy();
            if (index < LOADER_SLOTS) {
                if (!moveItemStackTo(stack, LOADER_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
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

    private boolean isInputLocked() {
        return getActiveAction() == StationAction.LOAD &&
                getState() != AbstractCartInteractorBlockEntity.State.BLOCKED;
    }

    public interface LoaderStateProvider extends StateProvider {
        boolean isAutoEject();

        void setAutoEject(boolean autoExtract);
    }

    private static final class OutputSlot extends ResourceHandlerSlot {
        public OutputSlot(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, itemHandler::set, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static final class ClientItemHandler extends ItemStacksResourceHandler {
        public ClientItemHandler(int slots) {
            super(slots);
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return FoupItem.canPlaceInFoup(resource.toStack());
        }
    }
}
