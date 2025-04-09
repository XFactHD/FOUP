package io.github.xfacthd.foup.common.blockentity;

import com.google.common.base.Preconditions;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class FoupStorageInterfaceBlockEntity extends AbstractCartInteractorBlockEntity
{
    @Nullable
    private FoupStorageLockerBlockEntity locker = null;
    @Nullable
    private ItemStack transferBuffer = null;
    private long actionStart = -1;
    private int loadingTarget = -1;
    private float cartRotation = 0;

    public FoupStorageInterfaceBlockEntity(BlockPos pos, BlockState state)
    {
        super(FoupContent.BE_TYPE_FOUP_STORAGE_INTERFACE.value(), pos, state, StationType.STORAGE);
    }

    @Override
    protected StartCheck canStartAction(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        FoupStorageLockerBlockEntity locker = getLocker();
        if (locker == null) return StartCheck.RETRY;

        ItemStack foup = cart.getFoupContent();
        return switch (scheduleEntry.action())
        {
            case LOAD ->
            {
                if (foup != null) yield StartCheck.SKIP;

                loadingTarget = locker.findMatching(scheduleEntry.filter());
                yield loadingTarget > -1 ? StartCheck.EXECUTE : StartCheck.WAIT;
            }
            case UNLOAD ->
            {
                if (foup == null) yield StartCheck.SKIP;
                if (locker.isFull()) yield StartCheck.WAIT;
                yield StartCheck.EXECUTE;
            }
        };
    }

    @Override
    protected void startInteraction(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        switch (scheduleEntry.action())
        {
            case LOAD ->
            {
                FoupStorageLockerBlockEntity locker = getLocker();
                if (locker != null)
                {
                    Preconditions.checkState(loadingTarget > -1, "No loading target present");
                    transferBuffer = locker.removeFrom(loadingTarget);
                    loadingTarget = -1;
                    cartRotation = cart.getYRot();
                    setChangedWithoutSignalUpdate();
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
                    cartRotation = cart.getYRot();
                    setChangedWithoutSignalUpdate();
                }
            }
        }
        actionStart = level().getGameTime();
        sendUpdatePacket();
    }

    @Override
    protected void finishInteraction(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        switch (scheduleEntry.action())
        {
            case LOAD ->
            {
                if (transferBuffer != null)
                {
                    ItemContents contents = transferBuffer.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY);
                    cart.setFoupContent(contents.stack());
                    transferBuffer = null;
                    cartRotation = 0;
                    setChangedWithoutSignalUpdate();
                }
            }
            case UNLOAD ->
            {
                FoupStorageLockerBlockEntity locker = getLocker();
                if (locker != null && transferBuffer != null)
                {
                    locker.insertReserved(transferBuffer);
                    transferBuffer = null;
                    cartRotation = 0;
                    setChangedWithoutSignalUpdate();
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

    @Nullable
    public ItemStack getFoupInFlight()
    {
        return transferBuffer;
    }

    public float getCartRotation()
    {
        return cartRotation;
    }

    @Override
    public void dropContents(Consumer<ItemStack> dropper)
    {
        if (transferBuffer != null)
        {
            dropper.accept(transferBuffer);
            transferBuffer = null;
        }
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        CompoundTag tag = new CompoundTag();
        if (currAction != null)
        {
            tag.putInt("action", currAction.ordinal());
        }
        tag.putLong("action_start", actionStart);
        RegistryOps<Tag> regOps = registries.createSerializationContext(NbtOps.INSTANCE);
        tag.storeNullable("transfer_buf", ItemStack.OPTIONAL_CODEC, regOps, transferBuffer);
        tag.putFloat("cart_rotation", cartRotation);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries)
    {
        currAction = StationAction.byId(tag.getIntOr("action", -1));
        actionStart = tag.getLongOr("action_start", 0);
        transferBuffer = tag.read("transfer_buf", ItemStack.OPTIONAL_CODEC).orElse(null);
        cartRotation = tag.getFloatOr("cart_rotation", 0);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        transferBuffer = tag.read("transfer_buf", ItemStack.OPTIONAL_CODEC).orElse(null);
        actionStart = tag.getLongOr("action_start", -1);
        cartRotation = tag.getFloatOr("cart_rotation", 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        RegistryOps<Tag> regOps = registries.createSerializationContext(NbtOps.INSTANCE);
        tag.storeNullable("transfer_buf", ItemStack.OPTIONAL_CODEC, regOps, transferBuffer);
        tag.putLong("action_start", actionStart);
        tag.putFloat("cart_rotation", cartRotation);
    }
}
