package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.common.FoupContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

public final class FoupBlockLootProvider extends BlockLootSubProvider
{
    public FoupBlockLootProvider(HolderLookup.Provider registries)
    {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected void generate()
    {
        dropSelf(FoupContent.BLOCK_RAIL.value());
        dropSelf(FoupContent.BLOCK_RAIL_CURVE.value());
        dropSelf(FoupContent.BLOCK_RAIL_SWITCH.value());
        dropSelf(FoupContent.BLOCK_RAIL_STATION.value());
        dropSelf(FoupContent.BLOCK_FOUP_LOADER.value());
        dropSelf(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value());
        dropSelfWithComponents(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), List.of(
                FoupContent.DC_TYPE_STORAGE_CONTENTS.value()
        ));
    }

    private void dropSelfWithComponents(Block block, List<DataComponentType<?>> components)
    {
        CopyComponentsFunction.Builder function = CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY);
        components.forEach(function::include);
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(block).apply(function))
        )));
    }

    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        return FoupContent.getAllBlocks();
    }
}
