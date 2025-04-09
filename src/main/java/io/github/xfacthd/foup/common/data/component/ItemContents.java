package io.github.xfacthd.foup.common.data.component;

import com.mojang.serialization.Codec;
import io.github.xfacthd.foup.common.item.FoupItem;
import net.minecraft.core.component.DataComponentGetter;
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
    public static final Codec<ItemContents> CODEC = ItemStack.OPTIONAL_CODEC.validate(FoupItem::validateCanPlaceInFoup)
            .xmap(ItemContents::new, ItemContents::stack);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
            .map(ItemContents::new, ItemContents::stack);
    public static final ItemContents EMPTY = new ItemContents(ItemStack.EMPTY);

    @Override
    public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter)
    {
        if (!stack.isEmpty())
        {
            tooltipAdder.accept(Component.translatable("desc.foup.component.item_contents.contents", stack.getCount(), stack.getHoverName()));
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof ItemContents(ItemStack otherStack) &&
                ItemStack.isSameItemSameComponents(stack, otherStack) &&
                otherStack.getCount() == stack.getCount();
    }

    @Override
    public int hashCode()
    {
        return ItemStack.hashItemAndComponents(stack) * 31 + stack.getCount();
    }
}
