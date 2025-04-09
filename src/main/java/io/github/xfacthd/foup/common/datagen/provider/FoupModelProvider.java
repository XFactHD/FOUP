package io.github.xfacthd.foup.common.datagen.provider;

import com.mojang.math.Quadrant;
import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.client.renderer.item.OverheadCartHasFoup;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.jetbrains.annotations.Nullable;

public final class FoupModelProvider extends ModelProvider
{
    public FoupModelProvider(PackOutput output)
    {
        super(output, Foup.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels)
    {
        ResourceLocation straightModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL.value());
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(FoupContent.BLOCK_RAIL.value(), BlockModelGenerators.plainVariant(straightModel))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
        );
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_RAIL.value().asItem(), ItemModelUtils.plainModel(straightModel));

        ResourceLocation curveModelRight = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_CURVE.value(), "_right");
        ResourceLocation curveModelLeft = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_CURVE.value(), "_left");
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(FoupContent.BLOCK_RAIL_CURVE.value())
                        .with(
                                PropertyDispatch.initial(PropertyHolder.RIGHT)
                                        .select(false, BlockModelGenerators.plainVariant(curveModelLeft))
                                        .select(true, BlockModelGenerators.plainVariant(curveModelRight))
                        )
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
        );
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_RAIL_CURVE.value().asItem(), ItemModelUtils.plainModel(curveModelRight));

        ResourceLocation switchRightInModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_SWITCH.value(), "_right_in");
        ResourceLocation switchRightOutModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_SWITCH.value(), "_right_out");
        ResourceLocation switchLeftInModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_SWITCH.value(), "_left_in");
        ResourceLocation switchLeftOutModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_SWITCH.value(), "_left_out");
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(FoupContent.BLOCK_RAIL_SWITCH.value())
                        .with(
                                PropertyDispatch.initial(PropertyHolder.RIGHT, PropertyHolder.OUTWARD)
                                        .select(false, false, BlockModelGenerators.plainVariant(switchLeftInModel))
                                        .select(false, true, BlockModelGenerators.plainVariant(switchLeftOutModel))
                                        .select(true, false, BlockModelGenerators.plainVariant(switchRightInModel))
                                        .select(true, true, BlockModelGenerators.plainVariant(switchRightOutModel))
                        )
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
        );
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_RAIL_SWITCH.value().asItem(), ItemModelUtils.plainModel(switchRightOutModel));

        TextureSlot texIndicator = TextureSlot.create("indicator");
        ModelTemplate stationTemplate = ExtendedModelTemplateBuilder.builder()
                .parent(ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_STATION.value()))
                .requiredTextureSlot(texIndicator)
                .renderType("minecraft:cutout")
                .build();
        ResourceLocation stationModelLinked = ExtendedModelTemplateBuilder.builder()
                .parent(mcLocation("block/block"))
                .renderType("minecraft:cutout")
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .customLoader(CompositeModelBuilder::new, builder -> builder
                        .child("rail", straightModel)
                        .inlineChild(
                                "shell",
                                stationTemplate,
                                TextureMapping.singleSlot(texIndicator, modLocation("block/station_indicator_linked"))
                        )
                )
                .build()
                .create(
                        ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_STATION.value(), "_linked"),
                        TextureMapping.particle(mcLocation("block/iron_block")),
                        blockModels.modelOutput
                );
        ResourceLocation stationModelUnlinked = ExtendedModelTemplateBuilder.builder()
                .parent(mcLocation("block/block"))
                .renderType("minecraft:cutout")
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .customLoader(CompositeModelBuilder::new, builder -> builder
                        .child("rail", straightModel)
                        .inlineChild(
                                "shell",
                                stationTemplate,
                                TextureMapping.singleSlot(texIndicator, modLocation("block/station_indicator_unlinked"))
                        )
                )
                .build()
                .create(
                        ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_STATION.value(), "_unlinked"),
                        TextureMapping.particle(mcLocation("block/iron_block")),
                        blockModels.modelOutput
                );
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(FoupContent.BLOCK_RAIL_STATION.value())
                        .with(
                                PropertyDispatch.initial(PropertyHolder.LINKED)
                                        .select(false, BlockModelGenerators.plainVariant(stationModelUnlinked))
                                        .select(true, BlockModelGenerators.plainVariant(stationModelLinked))
                        )
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
        );
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_RAIL_STATION.value().asItem(), ItemModelUtils.plainModel(stationModelLinked));

        ModelLocationUtils.getModelLocation(FoupContent.BLOCK_RAIL_STATION.value(), "_unlinked");

        ResourceLocation loaderModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_LOADER.value());
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(FoupContent.BLOCK_FOUP_LOADER.value(), BlockModelGenerators.plainVariant(loaderModel))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING_ALT)
        );
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_FOUP_LOADER.value().asItem(), ItemModelUtils.plainModel(loaderModel));

        ResourceLocation interfaceModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value());
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value(),
                BlockModelGenerators.plainVariant(interfaceModel)
        ));
        ResourceLocation interfaceItemModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value().asItem());
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value().asItem(), ItemModelUtils.plainModel(interfaceItemModel));

        registerStorageLocker(blockModels);
        ResourceLocation lockerItemModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value().asItem());
        itemModels.itemModelOutput.accept(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value().asItem(), ItemModelUtils.plainModel(lockerItemModel));

        ResourceLocation foupItemModel = ModelLocationUtils.getModelLocation(FoupContent.ITEM_FOUP.asItem());
        itemModels.itemModelOutput.accept(FoupContent.ITEM_FOUP.asItem(), ItemModelUtils.plainModel(foupItemModel));

        itemModels.itemModelOutput.accept(
                FoupContent.ITEM_CART.asItem(),
                ItemModelUtils.conditional(
                        OverheadCartHasFoup.INSTANCE,
                        ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(FoupContent.ITEM_CART.asItem(), "_with_foup")),
                        ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(FoupContent.ITEM_CART.asItem()))
                )
        );

        itemModels.generateFlatItem(FoupContent.ITEM_RAIL_INSPECTOR.value(), ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    private static void registerStorageLocker(BlockModelGenerators blockModels)
    {
        ResourceLocation caseModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_case");
        ResourceLocation botLeftEmptyModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_bottom_left_empty");
        ResourceLocation botRightEmptyModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_bottom_right_empty");
        ResourceLocation topLeftEmptyModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_top_left_empty");
        ResourceLocation topRightEmptyModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_top_right_empty");
        ResourceLocation botLeftFullModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_bottom_left_full");
        ResourceLocation botRightFullModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_bottom_right_full");
        ResourceLocation topLeftFullModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_top_left_full");
        ResourceLocation topRightFullModel = ModelLocationUtils.getModelLocation(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value(), "_top_right_full");

        MultiPartGenerator generator = MultiPartGenerator.multiPart(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value());
        addLockerPart(generator, caseModel, null, false, false);
        addLockerPart(generator, botLeftEmptyModel, PropertyHolder.LOCKER_FRONT_BL, false, false);
        addLockerPart(generator, botLeftFullModel, PropertyHolder.LOCKER_FRONT_BL, true, false);
        addLockerPart(generator, botRightEmptyModel, PropertyHolder.LOCKER_FRONT_BR, false, false);
        addLockerPart(generator, botRightFullModel, PropertyHolder.LOCKER_FRONT_BR, true, false);
        addLockerPart(generator, topLeftEmptyModel, PropertyHolder.LOCKER_FRONT_TL, false, false);
        addLockerPart(generator, topLeftFullModel, PropertyHolder.LOCKER_FRONT_TL, true, false);
        addLockerPart(generator, topRightEmptyModel, PropertyHolder.LOCKER_FRONT_TR, false, false);
        addLockerPart(generator, topRightFullModel, PropertyHolder.LOCKER_FRONT_TR, true, false);
        addLockerPart(generator, botLeftEmptyModel, PropertyHolder.LOCKER_BACK_BL, false, true);
        addLockerPart(generator, botLeftFullModel, PropertyHolder.LOCKER_BACK_BL, true, true);
        addLockerPart(generator, botRightEmptyModel, PropertyHolder.LOCKER_BACK_BR, false, true);
        addLockerPart(generator, botRightFullModel, PropertyHolder.LOCKER_BACK_BR, true, true);
        addLockerPart(generator, topLeftEmptyModel, PropertyHolder.LOCKER_BACK_TL, false, true);
        addLockerPart(generator, topLeftFullModel, PropertyHolder.LOCKER_BACK_TL, true, true);
        addLockerPart(generator, topRightEmptyModel, PropertyHolder.LOCKER_BACK_TR, false, true);
        addLockerPart(generator, topRightFullModel, PropertyHolder.LOCKER_BACK_TR, true, true);
        blockModels.blockStateOutput.accept(generator);
    }

    private static void addLockerPart(MultiPartGenerator generator, ResourceLocation model, @Nullable BooleanProperty prop, boolean value, boolean mirror)
    {
        for (Direction dir : PropertyHolder.FACING_HOR.getPossibleValues())
        {
            ConditionBuilder condition = new ConditionBuilder()
                    .term(PropertyHolder.FACING_HOR, dir);
            if (prop != null)
            {
                condition.term(prop, value);
            }
            Quadrant quadrant = switch (dir)
            {
                case NORTH -> mirror ? Quadrant.R0 : Quadrant.R180;
                case EAST -> mirror ? Quadrant.R90 : Quadrant.R270;
                case SOUTH -> mirror ? Quadrant.R180 : Quadrant.R0;
                case WEST -> mirror ? Quadrant.R270 : Quadrant.R90;
                default -> throw new AssertionError();
            };
            MultiVariant variant = BlockModelGenerators.plainVariant(model)
                    .with(VariantMutator.Y_ROT.withValue(quadrant));
            generator.with(condition, variant);
        }
    }
}
