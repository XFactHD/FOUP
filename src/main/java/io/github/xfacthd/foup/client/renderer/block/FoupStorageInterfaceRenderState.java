package io.github.xfacthd.foup.client.renderer.block;

import io.github.xfacthd.foup.common.data.StationAction;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jetbrains.annotations.Nullable;

public final class FoupStorageInterfaceRenderState extends BlockEntityRenderState
{
    long gameTime;
    long actionStart;
    @Nullable
    StationAction activeAction;
    float cartRotation;
    final ItemStackRenderState inflightFoup = new ItemStackRenderState();
    final ItemStackRenderState inflightFoupContent = new ItemStackRenderState();
    int inflightFoupContentSize;
}
