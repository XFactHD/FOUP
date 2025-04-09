package io.github.xfacthd.foup.common.data.railnet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.component.ScheduleSnapshot;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.network.FoupStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

public final class Schedule
{
    private final OverheadCartEntity owner;
    private final List<Entry> entries = new ArrayList<>();
    private final Set<String> stations = new HashSet<>();
    private int activeEntry = 0;

    public Schedule(OverheadCartEntity owner)
    {
        this.owner = owner;
    }

    public Entry getActiveEntry()
    {
        return entries.get(activeEntry);
    }

    public boolean isEmpty()
    {
        return entries.isEmpty();
    }

    public boolean containsStation(String station)
    {
        return stations.contains(station);
    }

    public void advance()
    {
        activeEntry = (activeEntry + 1) % entries.size();
    }

    public List<Entry> getEntriesCopy()
    {
        return List.copyOf(entries);
    }

    public boolean isValid(RailNetwork network)
    {
        return entries.stream().allMatch(e -> e.isValid(network));
    }

    public boolean addEntry(int idx, Entry entry)
    {
        if (owner.isIdle() && idx == entries.size())
        {
            entries.add(entry);
            return true;
        }
        return false;
    }

    public boolean updateEntry(int idx, Entry entry)
    {
        if (owner.isIdle() && inRange(idx) && entries.get(idx).uuid.equals(entry.uuid))
        {
            entries.set(idx, entry);
            return true;
        }
        return false;
    }

    public boolean moveEntry(int idx, boolean down, UUID entryUid)
    {
        int newIdx = down ? idx + 1 : idx - 1;
        if (owner.isIdle() && inRange(idx) && inRange(newIdx) && entries.get(idx).uuid.equals(entryUid))
        {
            entries.add(newIdx, entries.remove(idx));
            return true;
        }
        return false;
    }

    public boolean removeEntry(int idx, UUID entryUid)
    {
        if (owner.isIdle() && inRange(idx) && entries.get(idx).uuid.equals(entryUid))
        {
            entries.remove(idx);
            return true;
        }
        return false;
    }

    private boolean inRange(int idx)
    {
        return idx >= 0 && idx < entries.size();
    }

    public void applySnapshot(@Nullable ScheduleSnapshot snapshot)
    {
        if (snapshot == null || snapshot.entries().isEmpty()) return;

        entries.clear();
        stations.clear();
        entries.addAll(snapshot.entries());
        snapshot.entries()
                .stream()
                .map(Entry::station)
                .distinct()
                .forEach(stations::add);
    }

    public CompoundTag save(RegistryAccess registryAccess)
    {
        CompoundTag tag = new CompoundTag();
        ListTag entriesTag = new ListTag();
        for (Entry entry : entries)
        {
            entriesTag.add(entry.save(registryAccess));
        }
        tag.put("entries", entriesTag);
        tag.putInt("active_entry", activeEntry);
        return tag;
    }

    public void load(CompoundTag tag, RegistryAccess registryAccess)
    {
        ListTag entriesTag = tag.getListOrEmpty("entries");
        for (int i = 0; i < entriesTag.size(); i++)
        {
            Entry entry = Entry.load(entriesTag.getCompoundOrEmpty(i), registryAccess);
            entries.add(entry);
            stations.add(entry.station);
        }
        activeEntry = tag.getIntOr("active_entry", 0);
    }

    public record Entry(UUID uuid, String station, StationType type, StationAction action, Optional<ItemStack> filter, OptionalInt count)
    {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(Entry::uuid),
                Codec.STRING.fieldOf("station").forGetter(Entry::station),
                StationType.CODEC.fieldOf("type").forGetter(Entry::type),
                StationAction.CODEC.fieldOf("action").forGetter(Entry::action),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("filter").forGetter(Entry::filter),
                Codec.intRange(1, Item.ABSOLUTE_MAX_STACK_SIZE).optionalFieldOf("count").xmap(
                        opt -> opt.map(OptionalInt::of).orElse(OptionalInt.empty()),
                        opt -> opt.isPresent() ? Optional.of(opt.getAsInt()) : Optional.empty()
                ).forGetter(Entry::count)
        ).apply(inst, Entry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                Entry::uuid,
                ByteBufCodecs.STRING_UTF8,
                Entry::station,
                StationType.STREAM_CODEC,
                Entry::type,
                StationAction.STREAM_CODEC,
                Entry::action,
                ByteBufCodecs.optional(ItemStack.OPTIONAL_STREAM_CODEC),
                Entry::filter,
                FoupStreamCodecs.OPTIONAL_VAR_INT,
                Entry::count,
                Entry::new
        );

        public boolean matchesFilter(ItemStack stack)
        {
            return filter.isEmpty() || ItemStack.isSameItemSameComponents(stack, filter.get());
        }

        public int getCount()
        {
            return count.orElse(Item.ABSOLUTE_MAX_STACK_SIZE);
        }

        public boolean isValid(RailNetwork network)
        {
            if (type == StationType.UNKNOWN)
            {
                return false;
            }
            TrackNode node = network.getStation(station);
            if (node == null)
            {
                return false;
            }
            StationType nodeType = node.getStationType();
            if (nodeType != type)
            {
                return false;
            }
            if (nodeType == StationType.LOADER)
            {
                if (action == StationAction.UNLOAD && filter.isPresent())
                {
                    return false;
                }
                if (filter.isPresent() && filter.get().isEmpty())
                {
                    return false;
                }
                if (count.isPresent() && (count.getAsInt() < 1 || count.getAsInt() > Item.ABSOLUTE_MAX_STACK_SIZE))
                {
                    return false;
                }
            }
            if (nodeType == StationType.STORAGE)
            {
                if (count.isPresent())
                {
                    return false;
                }
                if (action == StationAction.UNLOAD && filter.isPresent())
                {
                    return false;
                }
            }
            return true;
        }

        public CompoundTag save(HolderLookup.Provider registries)
        {
            CompoundTag tag = new CompoundTag();
            tag.store("uuid", UUIDUtil.CODEC, uuid);
            tag.putString("station", station);
            tag.putString("type", type.getSerializedName());
            tag.putInt("action", action.ordinal());
            filter.ifPresent(filter ->
            {
                RegistryOps<Tag> regOps = registries.createSerializationContext(NbtOps.INSTANCE);
                tag.store("filter", ItemStack.OPTIONAL_CODEC, regOps, filter);
            });
            count.ifPresent(count -> tag.putInt("count", count));
            return tag;
        }

        public static Entry load(CompoundTag tag, HolderLookup.Provider registries)
        {
            UUID uuid = tag.read("uuid", UUIDUtil.CODEC).orElseThrow();
            String station = tag.getStringOr("station", "");
            StationType type = Objects.requireNonNull(StationType.byName(tag.getStringOr("type", "")));
            StationAction action = StationAction.byId(tag.getIntOr("action", 0));
            RegistryOps<Tag> regOps = registries.createSerializationContext(NbtOps.INSTANCE);
            Optional<ItemStack> filter = tag.read("filter", ItemStack.OPTIONAL_CODEC, regOps);
            OptionalInt count = OptionalInt.empty();
            if (tag.contains("count"))
            {
                count = OptionalInt.of(tag.getIntOr("count", 0));
            }
            return new Entry(uuid, station, type, action, filter, count);
        }

        @Override
        public String toString()
        {
            return "Entry[" + "station=" + station + ", type=" + type + ", action=" + action + ", filter=" + filter + ", count=" + count + ']';
        }
    }

    public enum RejectedAction
    {
        ADD,
        EDIT,
        MOVE,
        DELETE,
        EXECUTE;

        public static final StreamCodec<ByteBuf, RejectedAction> STREAM_CODEC = ByteBufCodecs.idMapper(
                ByIdMap.continuous(RejectedAction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO),
                RejectedAction::ordinal
        );
    }
}
