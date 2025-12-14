package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.capability.itemhandler.ExternalItemResourceHandler;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.item.FoupItem;
import io.github.xfacthd.foup.common.menu.FoupLoaderMenu;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class FoupLoaderBlockEntity extends AbstractCartInteractorBlockEntity implements MenuProvider, FoupLoaderMenu.LoaderStateProvider
{
    public static final Component MENU_TITLE = Component.translatable("foup.container.foup_loader");
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUTPUT = 1;
    private static final int PUSH_INTERNAL = 5;
    private static final int PUSH_COUNT = 8;

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(2)
    {
        @Override
        public boolean isValid(int slot, ItemResource resource)
        {
            return FoupItem.canPlaceInFoup(resource.toStack());
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack prevStack)
        {
            FoupLoaderBlockEntity.this.setChangedWithoutSignalUpdate();
        }
    };
    private final ExternalItemResourceHandler inputItemHandler = new ExternalItemResourceHandler(
            inventory, slot -> slot == SLOT_INPUT && (getActiveAction() != StationAction.LOAD || isBlocked()), slot -> false
    );
    private final ExternalItemResourceHandler outputItemHandler = new ExternalItemResourceHandler(
            inventory, slot -> false, slot -> slot == SLOT_OUTPUT
    );
    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> outputTargetCache = null;
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
                ItemResource resource = inventory.getResource(SLOT_INPUT);
                if (resource.isEmpty())
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
                    if (!resource.equals(ItemResource.of(foup)))
                    {
                        yield StartCheck.RETRY;
                    }
                }
                if (scheduleEntry.count().isPresent())
                {
                    int count = Math.min(scheduleEntry.count().getAsInt(), Utils.getMaxStackSize(resource));
                    if (foup.getCount() >= count)
                    {
                        yield StartCheck.SKIP;
                    }
                    if (inventory.getAmountAsInt(SLOT_INPUT) < count)
                    {
                        yield StartCheck.WAIT;
                    }
                }
                if (!scheduleEntry.matchesFilter(resource.toStack()))
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
                ItemResource resource = inventory.getResource(SLOT_OUTPUT);
                if (!resource.isEmpty())
                {
                    if (!resource.equals(ItemResource.of(foup)))
                    {
                        yield StartCheck.WAIT;
                    }
                    int count = Math.min(scheduleEntry.getCount(), foup.getCount());
                    if (resource.getMaxStackSize() - inventory.getAmountAsInt(SLOT_OUTPUT) < count)
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
                int invCount = inventory.getAmountAsInt(SLOT_INPUT);

                int count = Math.min(Math.min(invCount, Utils.getMaxStackSize(foup) - foup.getCount()), scheduleEntry.getCount());
                try (Transaction tx = Transaction.openRoot())
                {
                    ItemResource resource = inventory.getResource(SLOT_INPUT);
                    cart.setFoupContent(resource.toStack(count + foup.getCount()));
                    inventory.extract(SLOT_INPUT, resource, count, tx);
                    tx.commit();
                }
            }
            case UNLOAD ->
            {
                ItemStack foup = Objects.requireNonNull(cart.getFoupContent());
                ItemResource resource = inventory.getResource(SLOT_OUTPUT);

                int count = Math.min(Math.min(foup.getCount(), Utils.getMaxStackSize(resource)), scheduleEntry.getCount());
                try (Transaction tx = Transaction.openRoot())
                {
                    inventory.insert(SLOT_OUTPUT, ItemResource.of(foup), count, tx);
                    tx.commit();
                }
                cart.setFoupContent(count == foup.getCount() ? ItemStack.EMPTY : foup.copyWithCount(foup.getCount() - count));
            }
        }
    }

    @Override
    protected void tickInternal()
    {
        if (!autoEject || level().getGameTime() % PUSH_INTERNAL != 0) return;

        ItemResource resource = inventory.getResource(SLOT_OUTPUT);
        if (resource.isEmpty()) return;

        ResourceHandler<ItemResource> itemHandler = Objects.requireNonNull(outputTargetCache).getCapability();
        if (itemHandler == null) return;

        try (Transaction tx = Transaction.openRoot())
        {
            int count = Math.min(inventory.getAmountAsInt(SLOT_OUTPUT), PUSH_COUNT);
            int inserted = itemHandler.insert(resource, count, tx);
            if (inserted > 0)
            {
                inventory.extract(SLOT_OUTPUT, resource, inserted, tx);
                tx.commit();
            }
        }
    }

    public ItemStacksResourceHandler getInventory()
    {
        return inventory;
    }

    public ResourceHandler<ItemResource> getExternalInputItemHandler()
    {
        return inputItemHandler;
    }

    public ExternalItemResourceHandler getExternalOutputItemHandler()
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
    public void onLoad()
    {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel)
        {
            outputTargetCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, serverLevel, worldPosition.below(), Direction.UP, () -> !isRemoved(), () -> {});
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
    protected void loadAdditional(ValueInput valueInput)
    {
        super.loadAdditional(valueInput);
        inventory.deserialize(valueInput.childOrEmpty("inventory"));
        autoEject = valueInput.getBooleanOr("auto_eject", false);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput)
    {
        super.saveAdditional(valueOutput);
        inventory.serialize(valueOutput.child("inventory"));
        valueOutput.putBoolean("auto_eject", autoEject);
    }
}
