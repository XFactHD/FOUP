package io.github.xfacthd.foup.common.datagen;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.datagen.provider.FoupBlockStateProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupBlockTagsProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupEntityTypeTagsProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupLanguageProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupSpriteSourceProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@Mod(value = Foup.MOD_ID)
public final class GeneratorHandler
{
    public GeneratorHandler(IEventBus modBus)
    {
        modBus.addListener(GeneratorHandler::onGatherData);
    }

    private static void onGatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        boolean client = event.includeClient();
        boolean server = event.includeServer();

        gen.addProvider(client, new FoupSpriteSourceProvider(output, lookupProvider, fileHelper));
        gen.addProvider(client, new FoupBlockStateProvider(output, fileHelper));
        gen.addProvider(client, new FoupLanguageProvider(output));

        gen.addProvider(server, new FoupBlockTagsProvider(output, lookupProvider, fileHelper));
        gen.addProvider(server, new FoupEntityTypeTagsProvider(output, lookupProvider, fileHelper));
    }
}
