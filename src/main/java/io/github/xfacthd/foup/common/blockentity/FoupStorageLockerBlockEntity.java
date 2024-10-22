package io.github.xfacthd.foup.common.blockentity;

import com.google.common.base.Preconditions;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

// TODO: lock reserved slot in UI
public final class FoupStorageLockerBlockEntity extends BaseBlockEntity
{
    private final ItemStackHandler inventory = new ItemStackHandler(8)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            FoupStorageLockerBlockEntity.this.onInventoryChanged(slot);
        }
    };
    private int occupationState = 0;
    private int reservedSlot = -1;

    public FoupStorageLockerBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(FoupContent.BE_TYPE_FOUP_STORAGE_LOCKER.value(), pos, blockState);
    }

    private void onInventoryChanged(int slot)
    {
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

    boolean isEmpty()
    {
        return occupationState == 0;
    }

    boolean isFull()
    {
        return occupationState == 0b11111111;
    }

    boolean reserveSlot()
    {
        int slot = -1;
        for (int i = 0; i < 8; i++)
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

    ItemStack removeFirst()
    {
        int idx = Integer.numberOfTrailingZeros(occupationState);
        if (idx > inventory.getSlots()) throw new IllegalStateException("No full slots");

        ItemStack stack = inventory.getStackInSlot(idx);
        inventory.setStackInSlot(idx, ItemStack.EMPTY);
        return stack;
    }

    public ItemStackHandler getInventory()
    {
        return inventory;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        reservedSlot = tag.contains("reserved_slot") ? tag.getInt("reserved_slot") : -1;
        for (int i = 0; i < 8; i++)
        {
            if (!inventory.getStackInSlot(i).isEmpty())
            {
                occupationState |= 1 << i;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("reserved_slot", reservedSlot);
    }
}
