package io.github.xfacthd.foup.common.util.registration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public final class DeferredEntityRegister extends DeferredRegister<EntityType<?>>
{
    private DeferredEntityRegister(String namespace)
    {
        super(Registries.ENTITY_TYPE, namespace);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> createHolder(
            ResourceKey<? extends Registry<EntityType<?>>> registryKey, ResourceLocation key
    )
    {
        return (DeferredHolder<EntityType<?>, I>) DeferredEntity.createBlockEntity(ResourceKey.create(registryKey, key));
    }

    public <T extends Entity> DeferredEntity<T> registerEntity(
            String name, EntityType.EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> builder
    )
    {
        String dfuKey = getNamespace() + ":" + name;
        //noinspection ConstantConditions
        return (DeferredEntity<T>) register(name, () -> builder.apply(EntityType.Builder.of(factory, category)).build(dfuKey));
    }



    public static DeferredEntityRegister create(String namespace)
    {
        return new DeferredEntityRegister(namespace);
    }
}
