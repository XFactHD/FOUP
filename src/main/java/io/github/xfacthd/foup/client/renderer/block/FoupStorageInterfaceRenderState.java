package io.github.xfacthd.foup.client.renderer.block;

import io.github.xfacthd.foup.common.data.StationAction;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public final class FoupStorageInterfaceRenderState extends BlockEntityRenderState
{
    long actionStart;
    float actionTime;
    @Nullable
    StationAction activeAction;
    float cartRotation;
    final ItemStackRenderState inflightFoup = new ItemStackRenderState();
    final ItemStackRenderState inflightFoupContent = new ItemStackRenderState();
    int inflightFoupContentSize;
}
