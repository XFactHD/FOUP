package io.github.xfacthd.foup.common.recipe;

import com.mojang.serialization.MapCodec;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public final class AddFoupToCartRecipe extends CustomRecipe
{
    public static final MapCodec<AddFoupToCartRecipe> CODEC = MapCodec.unit(AddFoupToCartRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AddFoupToCartRecipe> STREAM_CODEC = NeoForgeStreamCodecs.uncheckedUnit(new AddFoupToCartRecipe());
    private static final int SIZE = 2;

    public AddFoupToCartRecipe()
    {
        super(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        if (input.ingredientCount() != SIZE) return false;

        ItemStack cart = ItemStack.EMPTY;
        ItemStack foup = ItemStack.EMPTY;
        for (ItemStack stack : input.items())
        {
            if (stack.is(FoupContent.ITEM_CART))
            {
                if (stack.getOrDefault(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY).hasFoup())
                {
                    return false;
                }
                cart = stack;
            }
            else if (stack.is(FoupContent.ITEM_FOUP))
            {
                foup = stack;
            }
        }
        return !cart.isEmpty() && !foup.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries)
    {
        if (input.ingredientCount() != SIZE) return ItemStack.EMPTY;

        ItemStack cart = ItemStack.EMPTY;
        ItemStack foup = ItemStack.EMPTY;
        for (ItemStack stack : input.items())
        {
            if (stack.is(FoupContent.ITEM_CART))
            {
                if (stack.getOrDefault(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY).hasFoup())
                {
                    return ItemStack.EMPTY;
                }
                cart = stack;
            }
            else if (stack.is(FoupContent.ITEM_FOUP))
            {
                foup = stack;
            }
        }
        if (!cart.isEmpty() && !foup.isEmpty())
        {
            ItemStack result = cart.copy();
            ItemStack foupContent = foup.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY).stack().copy();
            result.set(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.of(foupContent));
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<AddFoupToCartRecipe> getSerializer()
    {
        return FoupContent.RECIPE_SERIALIZER_ADD_FOUP_TO_CART.value();
    }
}
