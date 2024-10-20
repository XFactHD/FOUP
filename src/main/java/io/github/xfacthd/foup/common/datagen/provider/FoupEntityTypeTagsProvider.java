package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.FoupContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class FoupEntityTypeTagsProvider extends EntityTypeTagsProvider
{
    public FoupEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper)
    {
        super(output, provider, Foup.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries)
    {
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(FoupContent.ENTITY_TYPE_CART.value());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(FoupContent.ENTITY_TYPE_CART.value());
    }
}
