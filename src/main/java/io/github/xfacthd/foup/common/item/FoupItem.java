package io.github.xfacthd.foup.common.item;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import io.github.xfacthd.foup.common.menu.FoupMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class FoupItem extends Item
{
    public static final Component MENU_TITLE = Component.translatable("foup.container.foup");

    public FoupItem(Properties props)
    {
        super(props.component(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND)
        {
            if (!level.isClientSide())
            {
                player.openMenu(new FoupMenuProvider(stack));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean canFitInsideContainerItems()
    {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag)
    {
        lines.add(Component.translatable("item.foup.foup.desc").withStyle(ChatFormatting.GRAY));
        stack.addToTooltip(FoupContent.DC_TYPE_ITEM_CONTENTS, ctx, lines::add, flag);
    }

    public static boolean canPlaceInFoup(ItemStack stack)
    {
        return stack.getItem().canFitInsideContainerItems();
    }

    private record FoupMenuProvider(ItemStack foupStack) implements MenuProvider
    {
        @Override
        public Component getDisplayName()
        {
            return MENU_TITLE;
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
        {
            return new FoupMenu(containerId, inventory, foupStack);
        }
    }
}
