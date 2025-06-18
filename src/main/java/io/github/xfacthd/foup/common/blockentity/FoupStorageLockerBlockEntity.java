package io.github.xfacthd.foup.common.blockentity;

import com.google.common.base.Preconditions;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.capability.itemhandler.ExternalItemHandler;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import io.github.xfacthd.foup.common.data.component.LockerContents;
import io.github.xfacthd.foup.common.menu.FoupStorageLockerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;
import java.util.function.Consumer;

public final class FoupStorageLockerBlockEntity extends BaseBlockEntity implements MenuProvider
{
    public static final Component MENU_TITLE = Component.translatable("foup.container.foup_storage_locker");
    public static final int SLOT_COUNT = 8;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return canPlaceInStorage(stack);
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            FoupStorageLockerBlockEntity.this.onInventoryChanged(slot);
        }
    };
    private final ExternalItemHandler externalItemHandler = new ExternalItemHandler(
            inventory, this::isSlotUnlocked, this::isSlotUnlocked
    );
    private int occupationState = 0;
    private int reservedSlot = -1;

    public FoupStorageLockerBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(FoupContent.BE_TYPE_FOUP_STORAGE_LOCKER.value(), pos, blockState);
    }

    private void onInventoryChanged(int slot)
    {
        if (level().isClientSide()) return;

        ItemStack stack = inventory.getStackInSlot(slot);
        boolean wasOccupied = (occupationState & (1 << slot)) != 0;
        if (wasOccupied == stack.isEmpty())
        {
            occupationState = (occupationState & ~(1 << slot)) | (wasOccupied ? 0 : (1 << slot));
            BlockState newState = getBlockState().setValue(PropertyHolder.LOCKER_PROPERTIES[slot], !wasOccupied);
            level().setBlockAndUpdate(worldPosition, newState);
        }
        setChanged();
    }

    private boolean isSlotUnlocked(int slot)
    {
        return slot != reservedSlot;
    }

    boolean isFull()
    {
        return occupationState == 0b11111111;
    }

    boolean reserveSlot()
    {
        int slot = -1;
        for (int i = 0; i < SLOT_COUNT; i++)
        {
            if ((occupationState & (1 << i)) == 0)
            {
                slot = i;
                break;
            }
        }
        reservedSlot = slot;
        setChanged();
        return slot != -1;
    }

    void insertReserved(ItemStack stack)
    {
        Preconditions.checkState(reservedSlot != -1, "No slot reserved");
        inventory.setStackInSlot(reservedSlot, stack);
        reservedSlot = -1;
    }

    int findMatching(Optional<ItemStack> filter)
    {
        if (filter.isEmpty())
        {
            int idx = Integer.numberOfTrailingZeros(occupationState);
            return idx >= inventory.getSlots() ? -1 : idx;
        }
        for (int i = 0; i < SLOT_COUNT; i++)
        {
            if ((occupationState & (1 << i)) == 0)
            {
                continue;
            }

            ItemStack stack = inventory.getStackInSlot(i);
            ItemStack contents = stack.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY).stack();
            if (ItemStack.isSameItemSameComponents(filter.get(), contents))
            {
                return i;
            }
        }
        return -1;
    }

    ItemStack removeFrom(int idx)
    {
        ItemStack stack = inventory.getStackInSlot(idx);
        inventory.setStackInSlot(idx, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        return new FoupStorageLockerMenu(containerId, inventory, this.inventory, this::isUsableByPlayer, () -> reservedSlot);
    }

    @Override
    public Component getDisplayName()
    {
        return MENU_TITLE;
    }

    public int getAnalogSignal()
    {
        return Integer.bitCount(occupationState) * 15 / SLOT_COUNT;
    }

    public IItemHandler getExternalItemHandler()
    {
        return externalItemHandler;
    }

    public void dropContents(Consumer<ItemStack> dropper)
    {
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty())
            {
                dropper.accept(stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder)
    {
        if (occupationState > 0)
        {
            builder.set(FoupContent.DC_TYPE_STORAGE_CONTENTS, LockerContents.of(inventory));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input)
    {
        LockerContents contents = input.get(FoupContent.DC_TYPE_STORAGE_CONTENTS);
        if (contents != null)
        {
            contents.applyTo(inventory);
            computeOccupationState();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(ValueOutput valueOutput)
    {
        valueOutput.discard("inventory");
        valueOutput.discard("reserved_slot");
    }

    @Override
    protected void loadAdditional(ValueInput valueInput)
    {
        super.loadAdditional(valueInput);
        inventory.deserialize(valueInput.childOrEmpty("inventory"));
        reservedSlot = valueInput.getIntOr("reserved_slot", -1);
        computeOccupationState();
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput)
    {
        super.saveAdditional(valueOutput);
        inventory.serialize(valueOutput.child("inventory"));
        valueOutput.putInt("reserved_slot", reservedSlot);
    }

    private void computeOccupationState()
    {
        for (int i = 0; i < SLOT_COUNT; i++)
        {
            if (!inventory.getStackInSlot(i).isEmpty())
            {
                occupationState |= 1 << i;
            }
        }
    }

    public static boolean canPlaceInStorage(ItemStack stack)
    {
        return stack.is(FoupContent.ITEM_FOUP);
    }
}
