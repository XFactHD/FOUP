package io.github.xfacthd.foup.client.util;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("ConstantConditions")
public record SingleBlockFakeLevel(BlockAndTintGetter realLevel, BlockPos pos, BlockState state) implements BlockAndTintGetter
{
    @Override
    public CardinalLighting cardinalLighting()
    {
        return realLevel.cardinalLighting();
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return realLevel.getLightEngine();
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos)
    {
        return 15;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver)
    {
        return realLevel.getBlockTint(pos, resolver);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos)
    {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos)
    {
        if (pos.equals(this.pos))
        {
            return state;
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos)
    {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight()
    {
        return realLevel.getHeight();
    }

    @Override
    public int getMinY()
    {
        return realLevel.getMinY();
    }
}
