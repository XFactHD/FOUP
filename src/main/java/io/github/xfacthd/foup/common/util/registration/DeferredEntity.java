package io.github.xfacthd.foup.common.util.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredEntity<T extends Entity> extends DeferredHolder<EntityType<?>, EntityType<T>>
{
    private DeferredEntity(ResourceKey<EntityType<?>> key)
    {
        super(key);
    }



    public static <T extends Entity> DeferredEntity<T> createEntity(ResourceLocation name)
    {
        return createBlockEntity(ResourceKey.create(Registries.ENTITY_TYPE, name));
    }

    public static <T extends Entity> DeferredEntity<T> createBlockEntity(ResourceKey<EntityType<?>> key)
    {
        return new DeferredEntity<>(key);
    }
}
