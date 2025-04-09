package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.capability.itemhandler.ExternalItemHandler;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.item.FoupItem;
import io.github.xfacthd.foup.common.menu.FoupLoaderMenu;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class FoupLoaderBlockEntity extends AbstractCartInteractorBlockEntity implements MenuProvider, FoupLoaderMenu.LoaderStateProvider
{
    public static final Component MENU_TITLE = Component.translatable("foup.container.foup_loader");
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUTPUT = 1;
    private static final int PUSH_INTERNAL = 5;
    private static final int PUSH_COUNT = 8;

    private final ItemStackHandler inventory = new ItemStackHandler(2)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return FoupItem.canPlaceInFoup(stack);
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            FoupLoaderBlockEntity.this.setChangedWithoutSignalUpdate();
        }
    };
    private final ExternalItemHandler inputItemHandler = new ExternalItemHandler(
            inventory, slot -> slot == SLOT_INPUT && (getActiveAction() != StationAction.LOAD || isBlocked()), slot -> false
    );
    private final ExternalItemHandler outputItemHandler = new ExternalItemHandler(
            inventory, slot -> false, slot -> slot == SLOT_OUTPUT
    );
    @Nullable
    private BlockCapabilityCache<IItemHandler, Direction> outputTargetCache = null;
    private boolean autoEject = true;

    public FoupLoaderBlockEntity(BlockPos pos, BlockState state)
    {
        super(FoupContent.BE_TYPE_FOUP_LOADER.value(), pos, state, StationType.LOADER);
    }

    @Override
    protected StartCheck canStartAction(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        ItemStack foup = cart.getFoupContent();
        if (foup == null)
        {
            // No FOUP -> skip interaction
            return StartCheck.SKIP;
        }
        return switch (scheduleEntry.action())
        {
            case LOAD ->
            {
                ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);
                if (stack.isEmpty())
                {
                    yield StartCheck.WAIT;
                }
                if (!foup.isEmpty())
                {
                    if (foup.getCount() >= foup.getMaxStackSize())
                    {
                        yield StartCheck.SKIP;
                    }
                    if (!scheduleEntry.matchesFilter(foup))
                    {
                        yield StartCheck.SKIP;
                    }
                    if (!ItemStack.isSameItemSameComponents(stack, foup))
                    {
                        yield StartCheck.RETRY;
                    }
                }
                if (scheduleEntry.count().isPresent())
                {
                    int count = Math.min(scheduleEntry.count().getAsInt(), Utils.getMaxStackSize(stack));
                    if (foup.getCount() >= count)
                    {
                        yield StartCheck.SKIP;
                    }
                    if (stack.getCount() < count)
                    {
                        yield StartCheck.WAIT;
                    }
                }
                if (!scheduleEntry.matchesFilter(stack))
                {
                    yield StartCheck.RETRY;
                }
                yield StartCheck.EXECUTE;
            }
            case UNLOAD ->
            {
                if (foup.isEmpty())
                {
                    yield StartCheck.SKIP;
                }
                ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT);
                if (!stack.isEmpty())
                {
                    if (!ItemStack.isSameItemSameComponents(foup, stack))
                    {
                        yield StartCheck.WAIT;
                    }
                    int count = Math.min(scheduleEntry.getCount(), foup.getCount());
                    if (stack.getMaxStackSize() - stack.getCount() < count)
                    {
                        yield StartCheck.WAIT;
                    }
                }
                yield StartCheck.EXECUTE;
            }
        };
    }

    @Override
    protected void startInteraction(OverheadCartEntity cart, Schedule.Entry scheduleEntry) { }

    @Override
    protected void finishInteraction(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        switch (scheduleEntry.action())
        {
            case LOAD ->
            {
                ItemStack foup = Objects.requireNonNull(cart.getFoupContent());
                ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);

                int count = Math.min(Math.min(stack.getCount(), Utils.getMaxStackSize(foup) - foup.getCount()), scheduleEntry.getCount());
                cart.setFoupContent(stack.copyWithCount(count + foup.getCount()));
                inventory.extractItem(SLOT_INPUT, count, false);
            }
            case UNLOAD ->
            {
                ItemStack foup = Objects.requireNonNull(cart.getFoupContent());
                ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT);

                int count = Math.min(Math.min(foup.getCount(), Utils.getMaxStackSize(stack)), scheduleEntry.getCount());
                inventory.insertItem(SLOT_OUTPUT, foup.copyWithCount(count), false);
                cart.setFoupContent(count == foup.getCount() ? ItemStack.EMPTY : foup.copyWithCount(foup.getCount() - count));
            }
        }
    }

    @Override
    protected void tickInternal()
    {
        if (!autoEject || level().getGameTime() % PUSH_INTERNAL != 0) return;

        ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT);
        if (stack.isEmpty()) return;

        IItemHandler itemHandler = Objects.requireNonNull(outputTargetCache).getCapability();
        if (itemHandler == null) return;

        ItemStack toInsert = stack.getCount() <= PUSH_COUNT ? stack : stack.copyWithCount(PUSH_COUNT);
        ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, toInsert, false);
        if (remainder.getCount() < toInsert.getCount())
        {
            inventory.extractItem(SLOT_OUTPUT, toInsert.getCount() - remainder.getCount(), false);
        }
    }

    public ItemStackHandler getInventory()
    {
        return inventory;
    }

    public IItemHandler getExternalInputItemHandler()
    {
        return inputItemHandler;
    }

    public ExternalItemHandler getExternalOutputItemHandler()
    {
        return outputItemHandler;
    }

    @Override
    public boolean isAutoEject()
    {
        return autoEject;
    }

    @Override
    public void setAutoEject(boolean autoExtract)
    {
        this.autoEject = autoExtract;
        setChangedWithoutSignalUpdate();
    }

    @Override
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
    public void onLoad()
    {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel)
        {
            outputTargetCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, worldPosition.below(), Direction.UP, () -> !isRemoved(), () -> {});
        }
    }

    @Override
    public Component getDisplayName()
    {
        return MENU_TITLE;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        return new FoupLoaderMenu(containerId, inventory, this.inventory, this::isUsableByPlayer, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompoundOrEmpty("inventory"));
        autoEject = tag.getBooleanOr("auto_eject", false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putBoolean("auto_eject", autoEject);
    }
}
