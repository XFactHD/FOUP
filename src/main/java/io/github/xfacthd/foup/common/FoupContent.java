package io.github.xfacthd.foup.common;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.block.FoupLoaderBlock;
import io.github.xfacthd.foup.common.block.FoupStorageInterfaceBlock;
import io.github.xfacthd.foup.common.block.FoupStorageLockerBlock;
import io.github.xfacthd.foup.common.block.OverheadRailBlock;
import io.github.xfacthd.foup.common.block.OverheadRailCurveBlock;
import io.github.xfacthd.foup.common.block.OverheadRailStationBlock;
import io.github.xfacthd.foup.common.block.OverheadRailSwitchBlock;
import io.github.xfacthd.foup.common.blockentity.FoupLoaderBlockEntity;
import io.github.xfacthd.foup.common.blockentity.FoupStorageInterfaceBlockEntity;
import io.github.xfacthd.foup.common.blockentity.FoupStorageLockerBlockEntity;
import io.github.xfacthd.foup.common.blockentity.OverheadRailBlockEntity;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import io.github.xfacthd.foup.common.entity.OverheadCartAction;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.item.FoupItem;
import io.github.xfacthd.foup.common.item.OverheadCartItem;
import io.github.xfacthd.foup.common.menu.FoupLoaderMenu;
import io.github.xfacthd.foup.common.menu.FoupStorageLockerMenu;
import io.github.xfacthd.foup.common.util.registration.DeferredBlockEntity;
import io.github.xfacthd.foup.common.util.registration.DeferredBlockEntityRegister;
import io.github.xfacthd.foup.common.util.registration.DeferredDataComponentType;
import io.github.xfacthd.foup.common.util.registration.DeferredDataComponentTypeRegister;
import io.github.xfacthd.foup.common.util.registration.DeferredEntity;
import io.github.xfacthd.foup.common.util.registration.DeferredEntityRegister;
import io.github.xfacthd.foup.common.util.registration.DeferredMenuType;
import io.github.xfacthd.foup.common.util.registration.DeferredMenuTypeRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FoupContent
{
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Foup.MOD_ID);
    private static final DeferredDataComponentTypeRegister DATA_COMPONENTS = DeferredDataComponentTypeRegister.create(Foup.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Foup.MOD_ID);
    private static final DeferredBlockEntityRegister BLOCK_ENTITIES = DeferredBlockEntityRegister.create(Foup.MOD_ID);
    private static final DeferredEntityRegister ENTITIES = DeferredEntityRegister.create(Foup.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Foup.MOD_ID);
    private static final DeferredMenuTypeRegister MENU_TYPES = DeferredMenuTypeRegister.create(Foup.MOD_ID);
    private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Foup.MOD_ID);

    // region Blocks
    public static final Holder<Block> BLOCK_RAIL = registerBlock("overhead_rail", OverheadRailBlock::new);
    public static final Holder<Block> BLOCK_RAIL_CURVE = registerBlock("overhead_rail_curve", OverheadRailCurveBlock::new);
    public static final Holder<Block> BLOCK_RAIL_SWITCH = registerBlock("overhead_rail_switch", OverheadRailSwitchBlock::new);
    public static final Holder<Block> BLOCK_RAIL_STATION = registerBlock("overhead_rail_station", OverheadRailStationBlock::new);
    public static final Holder<Block> BLOCK_FOUP_LOADER = registerBlock("foup_loader", FoupLoaderBlock::new);
    public static final Holder<Block> BLOCK_FOUP_STORAGE_INTERFACE = registerBlock("foup_storage_interface", FoupStorageInterfaceBlock::new);
    public static final Holder<Block> BLOCK_FOUP_STORAGE_LOCKER = registerBlock("foup_storage_locker", FoupStorageLockerBlock::new);
    // endregion

    // region Data Components
    public static final DeferredDataComponentType<HeldFoup> DC_TYPE_HELD_FOUP = DATA_COMPONENTS.registerComponentType(
            "held_foup", builder -> builder.persistent(HeldFoup.CODEC).networkSynchronized(HeldFoup.STREAM_CODEC).cacheEncoding()
    );
    public static final DeferredDataComponentType<ItemContents> DC_TYPE_ITEM_CONTENTS = DATA_COMPONENTS.registerComponentType(
            "item_contents", builder -> builder.persistent(ItemContents.CODEC).networkSynchronized(ItemContents.STREAM_CODEC).cacheEncoding()
    );
    // endregion

    // region Items
    public static final DeferredItem<Item> ITEM_FOUP = ITEMS.registerItem("foup", FoupItem::new);
    public static final DeferredItem<Item> ITEM_CART = ITEMS.registerItem("overhead_cart", OverheadCartItem::new);
    // endregion

    // region Block Entities
    public static final Holder<BlockEntityType<?>> BE_TYPE_RAIL = registerBlockEntity(
            "overhead_rail", OverheadRailBlockEntity::new, BLOCK_RAIL, BLOCK_RAIL_CURVE, BLOCK_RAIL_SWITCH
    );
    public static final Holder<BlockEntityType<?>> BE_TYPE_RAIL_STATION = registerBlockEntity(
            "overhead_rail_station", OverheadRailStationBlockEntity::new, BLOCK_RAIL_STATION
    );
    public static final DeferredBlockEntity<FoupLoaderBlockEntity> BE_TYPE_FOUP_LOADER = registerBlockEntity(
            "foup_loader", FoupLoaderBlockEntity::new, BLOCK_FOUP_LOADER
    );
    public static final DeferredBlockEntity<FoupStorageInterfaceBlockEntity> BE_TYPE_FOUP_STORAGE_INTERFACE = registerBlockEntity(
            "foup_storage_interface", FoupStorageInterfaceBlockEntity::new, BLOCK_FOUP_STORAGE_INTERFACE
    );
    public static final Holder<BlockEntityType<?>> BE_TYPE_FOUP_STORAGE_LOCKER = registerBlockEntity(
            "foup_storage_locker", FoupStorageLockerBlockEntity::new, BLOCK_FOUP_STORAGE_LOCKER
    );
    // endregion

    // region Entities
    public static final DeferredEntity<OverheadCartEntity> ENTITY_TYPE_CART = ENTITIES.registerEntity(
            "overhead_cart", OverheadCartEntity::new, MobCategory.MISC, builder -> builder.sized(1F, 1F)
                    .setUpdateInterval(2)
                    .noSummon()
    );
    // endregion

    // region Creative Tabs
    public static final Holder<CreativeModeTab> TAB_MAIN = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("foup.itemGroup.main"))
            .icon(ITEM_CART::toStack)
            .displayItems((params, output) -> ITEMS.getEntries().stream().map(Holder::value).forEach(output::accept))
            .build()
    );
    // endregion

    // region Menu Types
    public static final DeferredMenuType<FoupLoaderMenu> MENU_TYPE_FOUP_LOADER = MENU_TYPES.registerMenuType(
            "foup_loader", FoupLoaderMenu::new
    );
    public static final DeferredMenuType<FoupStorageLockerMenu> MENU_TYPE_FOUP_STORAGE_LOCKER = MENU_TYPES.registerMenuType(
            "foup_storage_locker", FoupStorageLockerMenu::new
    );
    // endregion

    // region Entity Data Serializers
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<OverheadCartAction>> ENTITY_DATA_SERIALIER_CART_ACTION =
            ENTITY_DATA_SERIALIZERS.register("cart_action", () -> EntityDataSerializer.forValueType(OverheadCartAction.STREAM_CODEC));
    // endregion

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory)
    {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, BlockBehaviour.Properties.of());
        ITEMS.registerSimpleBlockItem(block);
        return block;
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
        CREATIVE_TABS.register(modBus);
        MENU_TYPES.register(modBus);
        ENTITY_DATA_SERIALIZERS.register(modBus);
    }

    private FoupContent() { }
}
