package io.github.xfacthd.foup.common.item;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class FoupItem extends Item
{
    public FoupItem(Properties props)
    {
        super(props.component(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag)
    {
        lines.add(Component.translatable("item.foup.foup.desc").withStyle(ChatFormatting.GRAY));
        stack.addToTooltip(FoupContent.DC_TYPE_ITEM_CONTENTS, ctx, lines::add, flag);
    }
}
