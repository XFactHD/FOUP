package io.github.xfacthd.foup.common.data.capability;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.PropertyHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CapabilitySetup
{
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FoupContent.BE_TYPE_FOUP_LOADER.value(), (be, side) ->
        {
            if (side == Direction.DOWN)
            {
                return be.getExternalOutputItemHandler();
            }
            else if (side == be.getBlockState().getValue(PropertyHolder.FACING_HOR))
            {
                return be.getExternalInputItemHandler();
            }
            return null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FoupContent.BE_TYPE_FOUP_STORAGE_LOCKER.value(), (be, side) ->
        {
            if (side != null && side != Direction.UP && side.getAxis() != be.getBlockState().getValue(PropertyHolder.FACING_HOR).getAxis())
            {
                return be.getExternalItemHandler();
            }
            return null;
        });
    }

    private CapabilitySetup() { }
}
