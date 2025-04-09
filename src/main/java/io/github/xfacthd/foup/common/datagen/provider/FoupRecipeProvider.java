package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.recipe.AddFoupToCartRecipe;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public final class FoupRecipeProvider extends RecipeProvider
{
    private FoupRecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
    {
        super(registries, output);
    }

    @Override
    protected void buildRecipes()
    {
        shaped(FoupContent.BLOCK_RAIL, 3)
                .pattern("III")
                .pattern("RRR")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Items.RAIL)
                .unlockedBy("hasRail", has(Items.RAIL))
                .save(output);

        shaped(FoupContent.BLOCK_RAIL_CURVE, 3)
                .pattern("RR")
                .pattern(" R")
                .define('R', FoupContent.BLOCK_RAIL.value())
                .unlockedBy("hasOverheadRail", has(FoupContent.BLOCK_RAIL.value()))
                .save(output);

        shaped(FoupContent.BLOCK_RAIL_SWITCH, 4)
                .pattern("RRR")
                .pattern(" R ")
                .define('R', FoupContent.BLOCK_RAIL.value())
                .unlockedBy("hasOverheadRail", has(FoupContent.BLOCK_RAIL.value()))
                .save(output);

        shaped(FoupContent.BLOCK_RAIL_STATION, 1)
                .pattern("DGD")
                .pattern("RRR")
                .define('D', Tags.Items.DUSTS_REDSTONE)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('R', FoupContent.BLOCK_RAIL.value())
                .unlockedBy("hasOverheadRail", has(FoupContent.BLOCK_RAIL.value()))
                .save(output);

        shaped(FoupContent.BLOCK_FOUP_LOADER, 1)
                .pattern("IFI")
                .pattern("IHI")
                .pattern("ICI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('F', FoupContent.ITEM_FOUP)
                .define('H', Items.HOPPER)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("hasHopper", has(Items.HOPPER))
                .save(output);

        shaped(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE, 1)
                .pattern("IDI")
                .pattern("IHI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', Items.IRON_TRAPDOOR)
                .define('H', Items.HOPPER)
                .unlockedBy("hasHopper", has(Items.HOPPER))
                .save(output);

        shaped(FoupContent.BLOCK_FOUP_STORAGE_LOCKER, 1)
                .pattern("IFI")
                .pattern("FCF")
                .pattern("IFI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('F', FoupContent.ITEM_FOUP)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("hasChest", has(Tags.Items.CHESTS_WOODEN))
                .save(output);

        shaped(FoupContent.ITEM_CART, 1)
                .pattern("NRN")
                .pattern("III")
                .pattern("ICI")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("hasOverheadRail", has(FoupContent.BLOCK_RAIL.value()))
                .save(output);

        shaped(FoupContent.ITEM_FOUP, 1)
                .pattern("III")
                .pattern("GCG")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS_CHEAP)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("hasChest", has(Tags.Items.CHESTS_WOODEN))
                .save(output);

        shaped(FoupContent.ITEM_RAIL_INSPECTOR, 1)
                .pattern(" NG")
                .pattern(" IN")
                .pattern("I  ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('G', Tags.Items.GLASS_PANES)
                .unlockedBy("hasGlassPane", has(Tags.Items.GLASS_PANES))
                .save(output);

        output.accept(key("add_foup_to_cart"), new AddFoupToCartRecipe(), null);
    }

    private ShapedRecipeBuilder shaped(Holder<? extends ItemLike> result, int count)
    {
        return ShapedRecipeBuilder.shaped(items, RecipeCategory.TRANSPORTATION, result.value(), count);
    }

    private static ResourceKey<Recipe<?>> key(String path)
    {
        return ResourceKey.create(Registries.RECIPE, Utils.rl(path));
    }

    public static final class Runner extends RecipeProvider.Runner
    {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries)
        {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
        {
            return new FoupRecipeProvider(registries, output);
        }

        @Override
        public String getName()
        {
            return "FOUP Recipes";
        }
    }
}
