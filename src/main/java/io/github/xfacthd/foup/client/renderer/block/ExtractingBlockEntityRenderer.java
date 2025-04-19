package io.github.xfacthd.foup.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class ExtractingBlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState> implements BlockEntityRenderer<T>
{
    private final S reusedRenderState = createRenderState();

    @Override
    public final void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 camera)
    {
        if (blockEntity.getLevel() == null || blockEntity.isRemoved()) return;

        extractRenderState(blockEntity, reusedRenderState, partialTick);
        render(reusedRenderState, poseStack, bufferSource, packedLight, packedOverlay, camera);
    }

    public abstract void render(S renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 camera);

    public abstract S createRenderState();

    public void extractRenderState(T be, S renderState, float partialTick)
    {
        renderState.partialTick = partialTick;
        renderState.pos = be.getBlockPos().immutable();
    }
}
