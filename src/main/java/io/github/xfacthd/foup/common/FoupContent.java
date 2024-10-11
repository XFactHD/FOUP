package io.github.xfacthd.foup.common;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.util.registration.DeferredBlockEntity;
import io.github.xfacthd.foup.common.util.registration.DeferredBlockEntityRegister;
import io.github.xfacthd.foup.common.util.registration.DeferredDataComponentTypeRegister;
import io.github.xfacthd.foup.common.util.registration.DeferredEntityRegister;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FoupContent
{
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Foup.MODID);
    private static final DeferredDataComponentTypeRegister DATA_COMPONENTS = DeferredDataComponentTypeRegister.create(Foup.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Foup.MODID);
    private static final DeferredBlockEntityRegister BLOCK_ENTITIES = DeferredBlockEntityRegister.create(Foup.MODID);
    private static final DeferredEntityRegister ENTITIES = DeferredEntityRegister.create(Foup.MODID);

    // region Blocks
    // endregion

    // region Data Components
    // endregion

    // region Items
    // endregion

    // region Block Entities
    // endregion

    // region Entities
    // endregion

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory)
    {
        return BLOCKS.registerBlock(name, factory, BlockBehaviour.Properties.of());
    }

    @SafeVarargs
    private static <T extends BlockEntity>DeferredBlockEntity<T> registerBlockEntity(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, Holder<Block>... blockHolders
    )
    {
        Supplier<Block[]> blocks = () -> Arrays.stream(blockHolders).map(Holder::value).toArray(Block[]::new);
        return BLOCK_ENTITIES.registerBlockEntity(name, factory, blocks);
    }

    public static void init(IEventBus modBus)
    {
        BLOCKS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITIES.register(modBus);
    }

    private FoupContent() { }
}
