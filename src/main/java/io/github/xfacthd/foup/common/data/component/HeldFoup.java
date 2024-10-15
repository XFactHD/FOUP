package io.github.xfacthd.foup.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public record HeldFoup(boolean hasFoup, ItemStack stack) implements TooltipProvider
{
    public static final Codec<HeldFoup> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("has_foup").forGetter(HeldFoup::hasFoup),
            ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(HeldFoup::stack)
    ).apply(inst, HeldFoup::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeldFoup> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            HeldFoup::hasFoup,
            ItemStack.OPTIONAL_STREAM_CODEC,
            HeldFoup::stack,
            HeldFoup::new
    );
    public static final HeldFoup EMPTY = new HeldFoup(false, ItemStack.EMPTY);

    public static HeldFoup of(@Nullable ItemStack stack)
    {
        return new HeldFoup(stack != null, Objects.requireNonNullElse(stack, ItemStack.EMPTY));
    }

    @Override
    public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> tooltipAdder, TooltipFlag flag)
    {
        if (!hasFoup) return;

        if (!stack.isEmpty())
        {
            tooltipAdder.accept(Component.translatable("desc.foup.component.held_foup.contents", stack.getCount(), stack.toString()));
        }
        else
        {
            tooltipAdder.accept(Component.translatable("desc.foup.component.held_foup.contents.empty"));
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof HeldFoup other && ItemStack.isSameItemSameComponents(stack, other.stack);
    }

    @Override
    public int hashCode()
    {
        return ItemStack.hashItemAndComponents(stack);
    }
}
