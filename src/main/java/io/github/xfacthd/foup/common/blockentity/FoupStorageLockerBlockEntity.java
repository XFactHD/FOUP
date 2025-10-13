package io.github.xfacthd.foup.common.blockentity;

import com.google.common.base.Preconditions;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.capability.itemhandler.ExternalItemResourceHandler;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.Optional;
import java.util.function.Consumer;

public final class FoupStorageLockerBlockEntity extends BaseBlockEntity implements MenuProvider
{
    public static final Component MENU_TITLE = Component.translatable("foup.container.foup_storage_locker");
    public static final int SLOT_COUNT = 8;

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(SLOT_COUNT)
    {
        @Override
        public boolean isValid(int slot, ItemResource resource)
        {
            return canPlaceInStorage(resource);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource)
        {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack prevStack)
        {
            FoupStorageLockerBlockEntity.this.onInventoryChanged(slot);
        }
    };
    private final ExternalItemResourceHandler externalItemHandler = new ExternalItemResourceHandler(
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

        ItemResource resource = inventory.getResource(slot);
        boolean wasOccupied = (occupationState & (1 << slot)) != 0;
        if (wasOccupied == resource.isEmpty())
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
        inventory.set(reservedSlot, ItemResource.of(stack), stack.getCount());
        reservedSlot = -1;
    }

    int findMatching(Optional<ItemStack> filter)
    {
        if (filter.isEmpty())
        {
            int idx = Integer.numberOfTrailingZeros(occupationState);
            return idx >= inventory.size() ? -1 : idx;
        }
        for (int i = 0; i < SLOT_COUNT; i++)
        {
            if ((occupationState & (1 << i)) == 0)
            {
                continue;
            }

            ItemResource resource = inventory.getResource(i);
            ItemStack contents = resource.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY).stack();
            if (ItemStack.isSameItemSameComponents(filter.get(), contents))
            {
                return i;
            }
        }
        return -1;
    }

    ItemStack removeFrom(int idx)
    {
        ItemStack stack = inventory.getResource(idx).toStack();
        inventory.set(idx, ItemResource.EMPTY, 0);
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

    public ResourceHandler<ItemResource> getExternalItemHandler()
    {
        return externalItemHandler;
    }

    public void dropContents(Consumer<ItemStack> dropper)
    {
        for (int i = 0; i < inventory.size(); i++)
        {
            ItemResource resource = inventory.getResource(i);
            if (!resource.isEmpty())
            {
                dropper.accept(resource.toStack(inventory.getAmountAsInt(i)));
                inventory.set(i, ItemResource.EMPTY, 0);
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
            if (!inventory.getResource(i).isEmpty())
            {
                occupationState |= 1 << i;
            }
        }
    }

    public static boolean canPlaceInStorage(ItemResource stack)
    {
        return stack.is((ItemLike) FoupContent.ITEM_FOUP);
    }
}
