package io.github.xfacthd.foup.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public final class OverheadCartRenderer extends EntityRenderer<OverheadCartEntity>
{
    private static final ResourceLocation TEXTURE = Utils.rl("textures/entity/overhead_cart.png");
    private static final float Y_OFFSET_FLAT = 9F/16F;
    private static final float Y_OFFSET_3D = 12.5F/16F;
    private static final Quaternionf FLAT_PRE_ROTATION = Axis.XN.rotationDegrees(90F);
    private static final ItemTransformer TRANSFORMER_FLAT = (poseStack, pass, passCount) ->
    {
        poseStack.translate(0, 0, (1F/16F) * pass);
        poseStack.mulPose(Axis.ZP.rotationDegrees(10 * pass * ((pass & 1) * 2 - 1)));
    };
    private static final ItemTransformer TRANSFORMER_3D = (poseStack, pass, passCount) ->
    {
        int xFactor = ((pass & 2) - 1) * ((passCount - 1) / 2);
        int zFactor = ((passCount & 1) == 0 || pass != passCount - 1) ? (pass & 1) * 2 - 1 : 0;
        poseStack.translate(xFactor * .3F, 0, zFactor * .3F);
    };

    private final OverheadCartModel model;
    private final ItemRenderer itemRenderer;

    public OverheadCartRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new OverheadCartModel(context.bakeLayer(OverheadCartModel.LAYER_LOCATION));
        this.itemRenderer = context.getItemRenderer();
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

        ItemStack stack = entity.getFoupContentClient();
        if (!stack.isEmpty())
        {
            renderFoupContents(stack, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderFoupContents(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        renderFoupContents(itemRenderer, stack, poseStack, bufferSource, model.foup.x, model.foup.y + model.hoistWire.yScale, model.foup.z, packedLight, true);
    }

    public static void renderFoupContents(
            ItemRenderer itemRenderer,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float x,
            float y,
            float z,
            int packedLight,
            boolean scaleInverse
    )
    {
        poseStack.pushPose();

        poseStack.translate(x, y, z);

        poseStack.translate(0, .5, 0);
        float xyScale = scaleInverse ? -.5F : .5F;
        poseStack.scale(xyScale, xyScale, .5F);
        poseStack.translate(0, -.5, 0);

        int passes;
        ItemTransformer transformer;
        if (itemRenderer.getModel(stack, null, null, 0).usesBlockLight())
        {
            poseStack.translate(0, Y_OFFSET_3D, 0);

            passes = Math.min((int) Math.ceil(stack.getCount() / 16F), 4);
            transformer = TRANSFORMER_3D;
        }
        else
        {
            poseStack.translate(0, Y_OFFSET_FLAT, 0);
            poseStack.mulPose(FLAT_PRE_ROTATION);

            passes = (int) Math.ceil(stack.getCount() / 8F);
            transformer = TRANSFORMER_FLAT;
        }

        for (int i = 0; i < passes; i++)
        {
            poseStack.pushPose();
            transformer.transform(poseStack, i, passes);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, null, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(OverheadCartEntity entity)
    {
        return TEXTURE;
    }

    @FunctionalInterface
    private interface ItemTransformer
    {
        void transform(PoseStack poseStack, int pass, int passCount);
    }
}
