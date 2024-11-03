package io.github.xfacthd.foup.common.datagen;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.datagen.provider.FoupBlockLootProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupBlockStateProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupBlockTagsProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupEntityTypeTagsProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupItemTagsProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupLanguageProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupRecipeProvider;
import io.github.xfacthd.foup.common.datagen.provider.FoupSpriteSourceProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
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

        FoupBlockTagsProvider blockTags = new FoupBlockTagsProvider(output, lookupProvider, fileHelper);
        gen.addProvider(server, blockTags);
        gen.addProvider(server, new FoupItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), fileHelper));
        gen.addProvider(server, new FoupEntityTypeTagsProvider(output, lookupProvider, fileHelper));
        gen.addProvider(server, new FoupRecipeProvider(output, lookupProvider));
        gen.addProvider(server, new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(FoupBlockLootProvider::new, LootContextParamSets.BLOCK)
        ), lookupProvider));
    }
}
