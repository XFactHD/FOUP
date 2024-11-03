package io.github.xfacthd.foup.common.data.component;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.xfacthd.foup.common.blockentity.FoupStorageLockerBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class LockerContents
{
    public static final Codec<LockerContents> CODEC = ItemStack.OPTIONAL_CODEC
            .listOf(FoupStorageLockerBlockEntity.SLOT_COUNT, FoupStorageLockerBlockEntity.SLOT_COUNT)
            .xmap(LockerContents::new, contents -> contents.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockerContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
            .apply(ByteBufCodecs.list(FoupStorageLockerBlockEntity.SLOT_COUNT))
            .map(LockerContents::new, contents -> contents.items);

    private final NonNullList<ItemStack> items;
    private final int hashCode;

    private LockerContents(List<ItemStack> items)
    {
        Preconditions.checkArgument(items.size() == FoupStorageLockerBlockEntity.SLOT_COUNT);
        this.items = NonNullList.copyOf(items);
        this.hashCode = ItemStack.hashStackList(items);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof LockerContents other && ItemStack.listMatches(items, other.items);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    public void applyTo(ItemStackHandler inventory)
    {
        for (int i = 0; i < FoupStorageLockerBlockEntity.SLOT_COUNT; i++)
        {
            inventory.setStackInSlot(i, items.get(i).copy());
        }
    }

    public static LockerContents of(ItemStackHandler inventory)
    {
        Preconditions.checkArgument(inventory.getSlots() == FoupStorageLockerBlockEntity.SLOT_COUNT);

        List<ItemStack> stacks = new ArrayList<>(FoupStorageLockerBlockEntity.SLOT_COUNT);
        for (int i = 0; i < FoupStorageLockerBlockEntity.SLOT_COUNT; i++)
        {
            stacks.add(inventory.getStackInSlot(i).copy());
        }
        return new LockerContents(stacks);
    }
}
