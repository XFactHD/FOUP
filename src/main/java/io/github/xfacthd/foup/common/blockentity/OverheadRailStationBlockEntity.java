package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.RenameResult;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.RailNetworkSavedData;
import io.github.xfacthd.foup.common.data.railnet.TrackNode;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.util.FoupCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class OverheadRailStationBlockEntity extends AbstractOverheadRailBlockEntity {
    private static final int MAX_HEIGHT_DIFF = 6;

    private String name;
    @Nullable
    private BlockPos linkedPos = null;
    @Nullable
    private AbstractCartInteractorBlockEntity linkedBlock = null;
    @Nullable
    private StationType linkedType;
    private boolean aboutToBeDestroyed = false;

    public OverheadRailStationBlockEntity(BlockPos pos, BlockState state) {
        super(FoupContent.BE_TYPE_RAIL_STATION.value(), pos, state);
        this.name = "(" + pos.toShortString() + ")";
    }

    @Override
    public void notifyArrival(OverheadCartEntity cart, Schedule.Entry scheduleEntry) {
        AbstractCartInteractorBlockEntity linked = getLinkedBlock();
        if (linked != null) {
            linked.notifyArrival(cart, scheduleEntry);
        }
    }

    public @Nullable AbstractCartInteractorBlockEntity getLinkedBlock() {
        if (linkedBlock == null && linkedPos != null) {
            //noinspection ConstantConditions
            linkedBlock = level().getBlockEntity(linkedPos) instanceof AbstractCartInteractorBlockEntity be ? be : null;
            if (linkedBlock == null) {
                clearLinkedBlock();
            }
        }
        if (linkedBlock != null && linkedBlock.isRemoved()) {
            clearLinkedBlock();
        }
        return linkedBlock;
    }

    private void clearLinkedBlock() {
        linkedBlock = null;
        linkedPos = null;
        linkedType = null;
        Objects.requireNonNull(getTrackNode()).setLinkedStationType(null);
        if (!aboutToBeDestroyed) {
            level().setBlockAndUpdate(worldPosition, getBlockState().setValue(PropertyHolder.LINKED, false));
            sendUpdatePacket();
        }
        setChangedWithoutSignalUpdate();
    }

    public RenameResult setName(String name) {
        TrackNode node = getTrackNode();
        if (node != null && level instanceof ServerLevel serverLevel) {
            RenameResult result = RailNetworkSavedData.setStationName(serverLevel, node, name);
            if (result == RenameResult.SUCCESS) {
                this.name = name;
                setChangedWithoutSignalUpdate();
                sendUpdatePacket();
            }
            return result;
        }
        return RenameResult.UNKNOWN;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected boolean isStation() {
        return true;
    }

    public @Nullable StationType getLinkedType() {
        return linkedType;
    }

    @Override
    public int getStationHeightDifference() {
        if (linkedPos != null) {
            return worldPosition.getY() - linkedPos.getY() - 1;
        }
        return -1;
    }

    public TriState tryLink() {
        if (getLinkedBlock() != null) {
            return TriState.DEFAULT;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 2; i <= MAX_HEIGHT_DIFF; i++) {
            pos.setWithOffset(worldPosition, 0, -i, 0);
            if (level().getBlockEntity(pos) instanceof AbstractCartInteractorBlockEntity be) {
                linkedPos = pos.immutable();
                linkedBlock = be;
                linkedType = be.getStationType();

                be.notifyLinked(worldPosition);
                Objects.requireNonNull(getTrackNode()).setLinkedStationType(be.getStationType());

                level().setBlockAndUpdate(worldPosition, getBlockState().setValue(PropertyHolder.LINKED, true));
                setChangedWithoutSignalUpdate();
                return TriState.TRUE;
            }
        }
        return TriState.FALSE;
    }

    public void unlink(boolean destroy) {
        aboutToBeDestroyed = destroy;
        AbstractCartInteractorBlockEntity linked = getLinkedBlock();
        if (linked != null) {
            linked.clearLink();
            clearLinkedBlock();
        }
    }

    @Override
    protected void postProcessNewNode(TrackNode node) {
        if (linkedType != null) {
            node.setLinkedStationType(linkedType);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        handleUpdateTag(valueInput);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putInt("linked_type", linkedType != null ? linkedType.ordinal() : -1);
        return tag;
    }

    @Override
    public void handleUpdateTag(ValueInput valueInput) {
        name = valueInput.getStringOr("name", "");
        linkedType = StationType.byId(valueInput.getIntOr("linked_type", -1));
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        name = valueInput.getStringOr("name", "");
        linkedPos = valueInput.read("linked_pos", FoupCodecs.POS_AS_LONG).orElse(null);
        linkedType = StationType.byName(valueInput.getStringOr("linked_type", ""));
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putString("name", name);
        valueOutput.storeNullable("linked_pos", FoupCodecs.POS_AS_LONG, linkedPos);
        valueOutput.storeNullable("linked_type", StationType.CODEC, linkedType);
    }
}
