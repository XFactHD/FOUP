package io.github.xfacthd.foup.common.data;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StationType implements StringRepresentable
{
    LOADER,
    STORAGE,
    ;

    private static final StationType[] VALUES = values();
    private static final Map<String, StationType> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(t -> t.name, Function.identity()));

    private final String name = toString().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName()
    {
        return name;
    }

    @Nullable
    public static StationType byName(String name)
    {
        return LOOKUP.get(name);
    }

    @Nullable
    public static StationType byId(int id)
    {
        return id >= 0 && id < VALUES.length ? VALUES[id] : null;
    }
}
