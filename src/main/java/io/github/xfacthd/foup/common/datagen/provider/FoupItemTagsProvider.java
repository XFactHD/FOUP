package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class FoupItemTagsProvider extends ItemTagsProvider
{
    public FoupItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper fileHelper)
    {
        super(output, registries, blockTags, Foup.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        copy(Utils.RAIL_BLOCKS, Utils.RAIL_ITEMS);
    }
}
