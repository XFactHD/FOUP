package io.github.xfacthd.foup.common.datagen.provider;

import io.github.xfacthd.foup.Foup;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

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
    }
}
