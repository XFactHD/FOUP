package io.github.xfacthd.foup.client.renderer.entity;

import io.github.xfacthd.foup.common.entity.OverheadCartState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemStack;

public final class OverheadCartRenderState extends EntityRenderState
{
    float yRot;
    boolean hasFoup;
    ItemStack foupContent;
    OverheadCartState state;
    int actionStart;
    int actionDuration;
    int heightDiff;
}
