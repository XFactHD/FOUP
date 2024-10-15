package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class FoupSpriteSourceProvider extends SpriteSourceProvider
{
    public FoupSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper)
    {
        super(output, lookupProvider, Foup.MOD_ID, fileHelper);
    }

    @Override
    protected void gather()
    {
        atlas(BLOCKS_ATLAS).addSource(new SingleFile(Utils.rl("entity/overhead_cart"), Optional.empty()));
    }
}
