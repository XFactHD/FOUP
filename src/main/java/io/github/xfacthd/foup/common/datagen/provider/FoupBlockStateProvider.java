package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public final class FoupBlockStateProvider extends BlockStateProvider
{
    public FoupBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, Foup.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        ModelFile straightModel = models().getExistingFile(modLoc("overhead_rail"));
        getVariantBuilder(FoupContent.BLOCK_RAIL.value()).forAllStates(state ->
        {
            int yRot = ((int) state.getValue(PropertyHolder.FACING_HOR).toYRot() + 180) % 360;
            return ConfiguredModel.builder().modelFile(straightModel).rotationY(yRot).build();
        });
        simpleBlockItem(FoupContent.BLOCK_RAIL.value(), straightModel);

        ModelFile curveModelRight = models().getExistingFile(modLoc("overhead_rail_curve_right"));
        ModelFile curveModelLeft = models().getExistingFile(modLoc("overhead_rail_curve_left"));
        getVariantBuilder(FoupContent.BLOCK_RAIL_CURVE.value()).forAllStates(state ->
        {
            int yRot = ((int) state.getValue(PropertyHolder.FACING_HOR).toYRot() + 180) % 360;
            boolean right = state.getValue(PropertyHolder.RIGHT);
            ModelFile model = right ? curveModelRight : curveModelLeft;
            return ConfiguredModel.builder().modelFile(model).rotationY(yRot).build();
        });
        simpleBlockItem(FoupContent.BLOCK_RAIL_CURVE.value(), curveModelRight);

        ModelFile switchRightInModel = models().getExistingFile(modLoc("overhead_rail_switch_right_in"));
        ModelFile switchRightOutModel = models().getExistingFile(modLoc("overhead_rail_switch_right_out"));
        ModelFile switchLeftInModel = models().getExistingFile(modLoc("overhead_rail_switch_left_in"));
        ModelFile switchLeftOutModel = models().getExistingFile(modLoc("overhead_rail_switch_left_out"));
        getVariantBuilder(FoupContent.BLOCK_RAIL_SWITCH.value()).forAllStates(state ->
        {
            int yRot = ((int) state.getValue(PropertyHolder.FACING_HOR).toYRot() + 180) % 360;
            boolean right = state.getValue(PropertyHolder.RIGHT);
            boolean out = state.getValue(PropertyHolder.OUTWARD);
            ModelFile model;
            if (right)
            {
                model = out ? switchRightOutModel : switchRightInModel;
            }
            else
            {
                model = out ? switchLeftOutModel : switchLeftInModel;
            }
            return ConfiguredModel.builder().modelFile(model).rotationY(yRot).build();
        });
        simpleBlockItem(FoupContent.BLOCK_RAIL_SWITCH.value(), switchRightOutModel);

        ModelFile stationModel = models().getExistingFile(modLoc("overhead_rail")); // TODO: add actual station model and link state
        getVariantBuilder(FoupContent.BLOCK_RAIL_STATION.value()).forAllStates(state ->
        {
            int yRot = ((int) state.getValue(PropertyHolder.FACING_HOR).toYRot() + 180) % 360;
            return ConfiguredModel.builder().modelFile(stationModel).rotationY(yRot).build();
        });
        simpleBlockItem(FoupContent.BLOCK_RAIL_STATION.value(), stationModel);

        ModelFile loaderModel = models().getExistingFile(modLoc("foup_loader"));
        getVariantBuilder(FoupContent.BLOCK_FOUP_LOADER.value()).forAllStates(state ->
        {
            int yRot = (int) state.getValue(PropertyHolder.FACING_HOR).toYRot() % 360;
            return ConfiguredModel.builder().modelFile(loaderModel).rotationY(yRot).build();
        });
        simpleBlockItem(FoupContent.BLOCK_FOUP_LOADER.value(), loaderModel);

        ModelFile interfaceModel = models().getExistingFile(modLoc("foup_storage_interface"));
        getVariantBuilder(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(interfaceModel).build()
        );
        simpleBlockItem(FoupContent.BLOCK_FOUP_STORAGE_INTERFACE.value(), interfaceModel); // TODO: replace with model including the door

        registerStorageLocker();
    }

    private void registerStorageLocker()
    {
        ModelFile caseModel = models().getExistingFile(modLoc("foup_storage_locker_case"));
        ModelFile botLeftEmptyModel = models().getExistingFile(modLoc("foup_storage_locker_bottom_left_empty"));
        ModelFile botRightEmptyModel = models().getExistingFile(modLoc("foup_storage_locker_bottom_right_empty"));
        ModelFile topLeftEmptyModel = models().getExistingFile(modLoc("foup_storage_locker_top_left_empty"));
        ModelFile topRightEmptyModel = models().getExistingFile(modLoc("foup_storage_locker_top_right_empty"));
        ModelFile botLeftFullModel = models().getExistingFile(modLoc("foup_storage_locker_bottom_left_full"));
        ModelFile botRightFullModel = models().getExistingFile(modLoc("foup_storage_locker_bottom_right_full"));
        ModelFile topLeftFullModel = models().getExistingFile(modLoc("foup_storage_locker_top_left_full"));
        ModelFile topRightFullModel = models().getExistingFile(modLoc("foup_storage_locker_top_right_full"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(FoupContent.BLOCK_FOUP_STORAGE_LOCKER.value());
        addLockerPart(builder, caseModel, null, false, 0);
        addLockerPart(builder, botLeftEmptyModel, PropertyHolder.LOCKER_FRONT_BL, false, 0);
        addLockerPart(builder, botLeftFullModel, PropertyHolder.LOCKER_FRONT_BL, true, 0);
        addLockerPart(builder, botRightEmptyModel, PropertyHolder.LOCKER_FRONT_BR, false, 0);
        addLockerPart(builder, botRightFullModel, PropertyHolder.LOCKER_FRONT_BR, true, 0);
        addLockerPart(builder, topLeftEmptyModel, PropertyHolder.LOCKER_FRONT_TL, false, 0);
        addLockerPart(builder, topLeftFullModel, PropertyHolder.LOCKER_FRONT_TL, true, 0);
        addLockerPart(builder, topRightEmptyModel, PropertyHolder.LOCKER_FRONT_TR, false, 0);
        addLockerPart(builder, topRightFullModel, PropertyHolder.LOCKER_FRONT_TR, true, 0);
        addLockerPart(builder, botLeftEmptyModel, PropertyHolder.LOCKER_BACK_BL, false, 180);
        addLockerPart(builder, botLeftFullModel, PropertyHolder.LOCKER_BACK_BL, true, 180);
        addLockerPart(builder, botRightEmptyModel, PropertyHolder.LOCKER_BACK_BR, false, 180);
        addLockerPart(builder, botRightFullModel, PropertyHolder.LOCKER_BACK_BR, true, 180);
        addLockerPart(builder, topLeftEmptyModel, PropertyHolder.LOCKER_BACK_TL, false, 180);
        addLockerPart(builder, topLeftFullModel, PropertyHolder.LOCKER_BACK_TL, true, 180);
        addLockerPart(builder, topRightEmptyModel, PropertyHolder.LOCKER_BACK_TR, false, 180);
        addLockerPart(builder, topRightFullModel, PropertyHolder.LOCKER_BACK_TR, true, 180);
    }

    private static void addLockerPart(MultiPartBlockStateBuilder builder, ModelFile model, @Nullable BooleanProperty prop, boolean value, float baseRot)
    {
        for (Direction dir : PropertyHolder.FACING_HOR.getPossibleValues())
        {
            MultiPartBlockStateBuilder.PartBuilder partBuilder = builder.part().modelFile(model).rotationY((int) (dir.toYRot() + baseRot) % 360).addModel();
            partBuilder.condition(PropertyHolder.FACING_HOR, dir);
            if (prop != null)
            {
                partBuilder.condition(prop, value);
            }
        }
    }
}
