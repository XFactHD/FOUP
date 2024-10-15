package io.github.xfacthd.foup.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class OverheadCartRenderer extends EntityRenderer<OverheadCartEntity>
{
    private static final ResourceLocation TEXTURE = Utils.rl("textures/entity/overhead_cart.png");

    private final OverheadCartModel model;

    public OverheadCartRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new OverheadCartModel(context.bakeLayer(OverheadCartModel.LAYER_LOCATION));
    }

    @Override
    public void render(OverheadCartEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        model.setupAnim(entity, 0F, 0F, entity.tickCount + partialTick, 0F, 0F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        poseStack.translate(0, 1.5F, 0);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer buffer = bufferSource.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(OverheadCartEntity entity)
    {
        return TEXTURE;
    }
}
