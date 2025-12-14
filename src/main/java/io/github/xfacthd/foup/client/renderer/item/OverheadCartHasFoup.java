package io.github.xfacthd.foup.client.renderer.item;

import com.mojang.serialization.MapCodec;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class OverheadCartHasFoup implements ConditionalItemModelProperty
{
    public static final OverheadCartHasFoup INSTANCE = new OverheadCartHasFoup();
    public static final MapCodec<OverheadCartHasFoup> CODEC = MapCodec.unit(INSTANCE);

    private OverheadCartHasFoup() { }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext ctx)
    {
        return stack.getOrDefault(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.EMPTY).hasFoup();
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type()
    {
        return CODEC;
    }
}
