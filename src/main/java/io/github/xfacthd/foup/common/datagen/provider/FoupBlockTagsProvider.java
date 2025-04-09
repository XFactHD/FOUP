package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.FoupContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public final class FoupBlockTagsProvider extends BlockTagsProvider
{
    public FoupBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider, Foup.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries)
    {
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .add(FoupContent.BLOCK_RAIL.value())
                .add(FoupContent.BLOCK_RAIL_CURVE.value())
                .add(FoupContent.BLOCK_RAIL_SWITCH.value())
                .add(FoupContent.BLOCK_RAIL_STATION.value())
                .add(FoupContent.BLOCK_FOUP_LOADER.value())
                .add(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value())
                .add(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(FoupContent.BLOCK_RAIL.value())
                .add(FoupContent.BLOCK_RAIL_CURVE.value())
                .add(FoupContent.BLOCK_RAIL_SWITCH.value())
                .add(FoupContent.BLOCK_RAIL_STATION.value())
                .add(FoupContent.BLOCK_FOUP_LOADER.value())
                .add(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value())
                .add(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value());
    }
}
