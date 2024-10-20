package io.github.xfacthd.foup.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BaseBlockEntity extends BlockEntity
{
    // Keep around the chunk holding this BE to avoid having to look it up every tick to mark it as unsaved
    @Nullable
    private LevelChunk owningChunk = null;

    protected BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    protected final Level level()
    {
        return Objects.requireNonNull(level, "Level missing");
    }

    public final void setChangedWithoutSignalUpdate()
    {
        if (owningChunk != null)
        {
            owningChunk.setUnsaved(true);
        }
    }

    @Override
    public final void setChanged()
    {
        setChangedWithoutSignalUpdate();

        BlockState state = getBlockState();
        if (!state.isAir())
        {
            level().updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        }
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        owningChunk = level().getChunkAt(worldPosition);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        owningChunk = null;
    }
}
