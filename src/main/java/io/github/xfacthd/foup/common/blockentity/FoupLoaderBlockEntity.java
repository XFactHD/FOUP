package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.capability.itemhandler.ExternalItemHandler;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FoupLoaderBlockEntity extends AbstractCartInteractorBlockEntity
{
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUTPUT = 1;
    private static final int PUSH_INTERNAL = 5;
    private static final int PUSH_COUNT = 8;

    private final ItemStackHandler inventory = new ItemStackHandler(2);
    private final ExternalItemHandler inputItemHandler = new ExternalItemHandler(
            inventory, slot -> slot == SLOT_INPUT && (getActiveAction() != Action.LOAD || isBlocked()), slot -> false
    );
    private final ExternalItemHandler outputItemHandler = new ExternalItemHandler(
            inventory, slot -> false, slot -> slot == SLOT_OUTPUT
    );
    @Nullable
    private BlockCapabilityCache<IItemHandler, Direction> outputTargetCache = null;

    public FoupLoaderBlockEntity(BlockPos pos, BlockState state)
    {
        super(FoupContent.BE_TYPE_FOUP_LOADER.value(), pos, state);
    }

    @Override
    protected TriState canStartAction(OverheadCartEntity cart, Action action)
    {
        ItemStack foup = cart.getFoupContent();
        if (foup == null)
        {
            // No FOUP -> skip interaction
            return TriState.DEFAULT;
        }
        return switch (action)
        {
            case LOAD ->
            {
                if (!foup.isEmpty()) yield TriState.DEFAULT;
                if (inventory.getStackInSlot(SLOT_INPUT).isEmpty()) yield TriState.FALSE;
                yield TriState.TRUE;
            }
            case UNLOAD ->
            {
                if (foup.isEmpty()) yield TriState.DEFAULT;
                if (!inventory.getStackInSlot(SLOT_OUTPUT).isEmpty()) yield TriState.FALSE;
                yield TriState.TRUE;
            }
        };
    }

    @Override
    protected void startInteraction() { }

    @Override
    protected void finishInteraction()
    {
        switch (Objects.requireNonNull(getActiveAction()))
        {
            case LOAD ->
            {
                OverheadCartEntity cart = getCart();
                if (cart != null)
                {
                    cart.setFoupContent(inventory.getStackInSlot(SLOT_INPUT));
                    inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
                    setChanged();
                }
            }
            case UNLOAD ->
            {
                OverheadCartEntity cart = getCart();
                if (cart != null)
                {
                    inventory.setStackInSlot(SLOT_OUTPUT, Objects.requireNonNull(cart.getFoupContent()));
                    cart.setFoupContent(ItemStack.EMPTY);
                    setChanged();
                }
            }
        }
    }

    @Override
    protected void tickInternal()
    {
        if (level().getGameTime() % PUSH_INTERNAL != 0) return;

        ItemStack stack = inventory.getStackInSlot(SLOT_OUTPUT);
        if (stack.isEmpty()) return;

        IItemHandler itemHandler = Objects.requireNonNull(outputTargetCache).getCapability();
        if (itemHandler == null) return;

        ItemStack toInsert = stack.getCount() <= PUSH_COUNT ? stack : stack.copyWithCount(PUSH_COUNT);
        ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, toInsert, false);
        if (remainder.getCount() < toInsert.getCount())
        {
            inventory.extractItem(SLOT_OUTPUT, toInsert.getCount() - remainder.getCount(), false);
            setChanged();
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
    public void onLoad()
    {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel)
        {
            outputTargetCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, worldPosition, Direction.DOWN, () -> !isRemoved(), () -> {});
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }
}
