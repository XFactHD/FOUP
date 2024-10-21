package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.railnet.RailNetworkSavedData;
import io.github.xfacthd.foup.common.data.railnet.TrackNode;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

public final class OverheadRailStationBlockEntity extends AbstractOverheadRailBlockEntity
{
    private static final int MAX_HEIGHT_DIFF = 6;

    private String name;
    @Nullable
    private BlockPos linkedPos = null;
    @Nullable
    private AbstractCartInteractorBlockEntity linkedBlock = null;
    private boolean aboutToBeDestroyed = false;

    public OverheadRailStationBlockEntity(BlockPos pos, BlockState state)
    {
        super(FoupContent.BE_TYPE_RAIL_STATION.value(), pos, state);
        this.name = "(" + pos.toShortString() + ")";
    }

    @Override
    public void notifyArrival(OverheadCartEntity cart, AbstractCartInteractorBlockEntity.Action action)
    {
        AbstractCartInteractorBlockEntity linked = getLinkedBlock();
        if (linked != null)
        {
            linked.notifyArrival(cart, action);
        }
    }

    @Nullable
    private AbstractCartInteractorBlockEntity getLinkedBlock()
    {
        if (linkedBlock == null && linkedPos != null)
        {
            //noinspection ConstantConditions
            linkedBlock = level().getBlockEntity(linkedPos) instanceof AbstractCartInteractorBlockEntity be ? be : null;
            if (linkedBlock == null)
            {
                clearLinkedBlock();
            }
        }
        if (linkedBlock != null && linkedBlock.isRemoved())
        {
            clearLinkedBlock();
        }
        return linkedBlock;
    }

    private void clearLinkedBlock()
    {
        linkedBlock = null;
        linkedPos = null;
        if (!aboutToBeDestroyed)
        {
            level().setBlockAndUpdate(worldPosition, getBlockState().setValue(PropertyHolder.LINKED, false));
        }
        setChangedWithoutSignalUpdate();
    }

    // TODO: implement naming in UI
    public boolean setName(String name)
    {
        TrackNode node = getTrackNode();
        if (node != null && level instanceof ServerLevel serverLevel && RailNetworkSavedData.setStationName(serverLevel, node, name))
        {
            this.name = name;
            setChangedWithoutSignalUpdate();
            return true;
        }
        return false;
    }

    @Override
    protected String getName()
    {
        return name;
    }

    @Override
    protected boolean isStation()
    {
        return true;
    }

    @Override
    public int getStationHeightDifference()
    {
        if (linkedPos != null)
        {
            return worldPosition.getY() - linkedPos.getY() - 1;
        }
        return -1;
    }

    public TriState tryLink()
    {
        if (getLinkedBlock() != null) return TriState.DEFAULT;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 2; i <= MAX_HEIGHT_DIFF; i++)
        {
            pos.setWithOffset(worldPosition, 0, -i, 0);
            if (level().getBlockEntity(pos) instanceof AbstractCartInteractorBlockEntity be)
            {
                linkedPos = pos.immutable();
                linkedBlock = be;
                level().setBlockAndUpdate(worldPosition, getBlockState().setValue(PropertyHolder.LINKED, true));
                setChangedWithoutSignalUpdate();
                return TriState.TRUE;
            }
        }
        return TriState.FALSE;
    }

    public void unlink(boolean destroy)
    {
        aboutToBeDestroyed = destroy;
        AbstractCartInteractorBlockEntity linked = getLinkedBlock();
        if (linked != null)
        {
            linked.clearLink();
            clearLinkedBlock();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        name = tag.getString("name");
        linkedPos = tag.contains("linked_pos") ? BlockPos.of(tag.getLong("linked_pos")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putString("name", name);
        if (linkedPos != null)
        {
            tag.putLong("linked_pos", linkedPos.asLong());
        }
    }
}
