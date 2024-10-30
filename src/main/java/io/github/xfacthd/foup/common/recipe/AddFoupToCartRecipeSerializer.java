package io.github.xfacthd.foup.common.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class AddFoupToCartRecipeSerializer implements RecipeSerializer<AddFoupToCartRecipe>
{
    private static final MapCodec<AddFoupToCartRecipe> CODEC = MapCodec.unit(AddFoupToCartRecipe::new);
    private static final StreamCodec<RegistryFriendlyByteBuf, AddFoupToCartRecipe> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public AddFoupToCartRecipe decode(RegistryFriendlyByteBuf buffer)
        {
            return new AddFoupToCartRecipe();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, AddFoupToCartRecipe value) { }
    };

    @Override
    public MapCodec<AddFoupToCartRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AddFoupToCartRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
