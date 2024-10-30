package io.github.xfacthd.foup.common.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import io.github.xfacthd.foup.client.screen.OverheadCartScreen;
import io.github.xfacthd.foup.common.recipe.AddFoupToCartRecipe;
import net.minecraft.world.item.crafting.RecipeType;

@EmiEntrypoint
public final class FoupEmiPlugin implements EmiPlugin
{
    @Override
    public void register(EmiRegistry registry)
    {
        registry.addDragDropHandler(OverheadCartScreen.class, new OverheadCartDragDropHandler());

        registry.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING)
                .stream()
                .filter(holder -> holder.value() instanceof AddFoupToCartRecipe)
                .forEach(holder -> registry.addRecipe(new AddFoupToCartEmiRecipe(holder.id())));
    }
}
