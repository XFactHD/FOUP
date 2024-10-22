package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

public final class FoupStorageInterfaceBlockEntity extends AbstractCartInteractorBlockEntity
{
    @Nullable
    private FoupStorageLockerBlockEntity locker = null;
    @Nullable
    private ItemStack transferBuffer = null;
    private long actionStart = -1;

    public FoupStorageInterfaceBlockEntity(BlockPos pos, BlockState state)
    {
        super(FoupContent.BE_TYPE_FOUP_STORAGE_INTERFACE.value(), pos, state, Type.STORAGE);
    }

    @Override
    protected TriState canStartAction(OverheadCartEntity cart, Action action)
    {
        FoupStorageLockerBlockEntity locker = getLocker();
        if (locker == null) return TriState.DEFAULT;

        ItemStack foup = cart.getFoupContent();
        return switch (action)
        {
            case LOAD ->
            {
                if (foup != null) yield TriState.DEFAULT;
                if (locker.isEmpty()) yield TriState.FALSE;
                yield TriState.TRUE;
            }
            case UNLOAD ->
            {
                if (foup == null) yield TriState.DEFAULT;
                if (locker.isFull()) yield TriState.FALSE;
                yield TriState.TRUE;
            }
        };
    }

    @Override
    protected void startInteraction(OverheadCartEntity cart, Action action)
    {
        switch (action)
        {
            case LOAD ->
            {
                FoupStorageLockerBlockEntity locker = getLocker();
                if (locker != null)
                {
                    transferBuffer = locker.removeFirst();
                    setChanged();
                }
            }
            case UNLOAD ->
            {
                FoupStorageLockerBlockEntity locker = getLocker();
                if (locker != null && locker.reserveSlot())
                {
                    transferBuffer = FoupContent.ITEM_FOUP.toStack();
                    transferBuffer.set(FoupContent.DC_TYPE_ITEM_CONTENTS, new ItemContents(cart.getFoupContent()));
                    cart.setFoupContent(null);
                    setChanged();
                }
            }
        }
        actionStart = level().getGameTime();
        sendUpdatePacket();
    }

    @Override
    protected void finishInteraction(OverheadCartEntity cart, Action action)
    {
        switch (action)
        {
            case LOAD ->
            {
                if (transferBuffer != null)
                {
                    ItemContents contents = transferBuffer.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY);
                    cart.setFoupContent(contents.stack());
                    transferBuffer = null;
                    setChanged();
                }
            }
            case UNLOAD ->
            {
                FoupStorageLockerBlockEntity locker = getLocker();
                if (locker != null && transferBuffer != null)
                {
                    locker.insertReserved(transferBuffer);
                    transferBuffer = null;
                    setChanged();
                }
            }
        }
        actionStart = -1;
        sendUpdatePacket();
    }

    @Nullable
    private FoupStorageLockerBlockEntity getLocker()
    {
        if (locker == null)
        {
            locker = level().getBlockEntity(worldPosition.below()) instanceof FoupStorageLockerBlockEntity be ? be : null;
        }
        return locker;
    }

    public long getActionStart()
    {
        return actionStart;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        CompoundTag tag = new CompoundTag();
        if (currAction != null)
        {
            tag.putInt("action", currAction.ordinal());
        }
        tag.putLong("action_start", actionStart);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries)
    {
        handleUpdateTag(pkt.getTag(), registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider)
    {
        currAction = tag.contains("action") ? Action.BY_ID.apply(tag.getInt("action")) : null;
        actionStart = tag.getLong("action_start");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        transferBuffer = tag.contains("transfer_buf") ? ItemStack.parseOptional(registries, tag.getCompound("transfer_buf")) : null;
        actionStart = tag.contains("action_start") ? tag.getLong("action_start") : -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if (transferBuffer != null)
        {
            tag.put("transfer_buf", transferBuffer.saveOptional(registries));
        }
        tag.putLong("action_start", actionStart);
    }
}
