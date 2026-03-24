package io.github.xfacthd.foup.common.item;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public final class OverheadCartItem extends Item
{
    public OverheadCartItem(Properties props)
    {
        super(props.component(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx)
    {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(FoupContent.BLOCK_RAIL) || state.is(FoupContent.BLOCK_RAIL_STATION))
        {
            OverheadCartEntity cart = FoupContent.ENTITY_TYPE_CART.value().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (cart == null) return InteractionResult.FAIL;

            cart.setPos(Vec3.atBottomCenterOf(pos).add(0, OverheadCartEntity.PLACEMENT_Y_OFFSET, 0));
            cart.setYRot(state.getValue(PropertyHolder.FACING_HOR).toYRot());
            if (!level.noCollision(cart)) return InteractionResult.FAIL;

            ItemStack stack = ctx.getItemInHand();
            stack.getOrDefault(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY).applyToCart(cart);
            cart.getSchedule().applySnapshot(stack.get(FoupContent.DC_TYPE_SCHEDULE));

            level.addFreshEntity(cart);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canFitInsideContainerItems(ItemStack stack)
    {
        return !stack.getOrDefault(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY).stack().isEmpty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canFitInsideContainerItems()
    {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> appender, TooltipFlag flag)
    {
        stack.addToTooltip(FoupContent.DC_TYPE_HELD_FOUP, ctx, display, appender, flag);
    }
}
