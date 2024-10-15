package io.github.xfacthd.foup.common.data.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record ItemContents(ItemStack stack) implements TooltipProvider
{
    public static final Codec<ItemContents> CODEC = ItemStack.OPTIONAL_CODEC.xmap(ItemContents::new, ItemContents::stack);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
            .map(ItemContents::new, ItemContents::stack);
    public static final ItemContents EMPTY = new ItemContents(ItemStack.EMPTY);

    @Override
    public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> tooltipAdder, TooltipFlag flag)
    {
        if (!stack.isEmpty())
        {
            tooltipAdder.accept(Component.translatable("desc.foup.component.item_contents.contents", stack.getCount(), stack.toString()));
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof ItemContents other && ItemStack.isSameItemSameComponents(stack, other.stack);
    }

    @Override
    public int hashCode()
    {
        return ItemStack.hashItemAndComponents(stack);
    }
}
