package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public final class FoupItemTagsProvider extends ItemTagsProvider
{
    public FoupItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries, Foup.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {

    }
}
