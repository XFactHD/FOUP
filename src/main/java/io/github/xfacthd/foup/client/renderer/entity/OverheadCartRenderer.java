package io.github.xfacthd.foup.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.entity.OverheadCartState;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

public final class OverheadCartRenderer extends EntityRenderer<OverheadCartEntity, OverheadCartRenderState>
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
    private static final ItemStackRenderState ITEM_SCRATCH_STATE = new ItemStackRenderState();

    private final OverheadCartModel model;
    private final ItemModelResolver itemModelResolver;

    public OverheadCartRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new OverheadCartModel(context.bakeLayer(OverheadCartModel.LAYER_LOCATION));
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void render(OverheadCartRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        model.setupAnim(renderState);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - renderState.yRot));
        poseStack.translate(0, 1.5F, 0);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer buffer = bufferSource.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        ItemStack stack = renderState.foupContent;
        if (!stack.isEmpty())
        {
            renderFoupContents(stack, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();

        super.render(renderState, poseStack, bufferSource, packedLight);
    }

    private void renderFoupContents(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        renderFoupContents(itemModelResolver, stack, poseStack, bufferSource, model.foup.x, model.foup.y + model.hoistWire.yScale, model.foup.z, packedLight, true);
    }

    public static void renderFoupContents(
            ItemModelResolver itemModelResolver,
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

        itemModelResolver.updateForTopItem(ITEM_SCRATCH_STATE, stack, ItemDisplayContext.FIXED, null, null, 0);

        int passes;
        ItemTransformer transformer;
        if (ITEM_SCRATCH_STATE.usesBlockLight())
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
            ITEM_SCRATCH_STATE.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public OverheadCartRenderState createRenderState()
    {
        return new OverheadCartRenderState();
    }

    @Override
    public void extractRenderState(OverheadCartEntity cart, OverheadCartRenderState renderState, float partialTick)
    {
        super.extractRenderState(cart, renderState, partialTick);

        renderState.yRot = cart.getYRot(partialTick);
        renderState.hasFoup = cart.getHasFoup();
        renderState.foupContent = cart.getFoupContentClient();
        renderState.state = cart.getState();
        renderState.actionStart = cart.getActionStart();
        renderState.actionDuration = cart.getActionDuration();
        renderState.heightDiff = cart.getHeightDiff();
    }

    @Override
    protected AABB getBoundingBoxForCulling(OverheadCartEntity cart)
    {
        AABB aabb = super.getBoundingBoxForCulling(cart);
        OverheadCartState state = cart.getState();
        if (state != null && state.hasMovingHoist())
        {
            double diff = cart.getHeightDiff() + (OverheadCartEntity.CART_BASE_DIST / 16F) - (OverheadCartEntity.STATION_BASE_HEIGHT / 16F);
            aabb = aabb.expandTowards(0, -diff, 0);
        }
        return aabb;
    }

    @FunctionalInterface
    private interface ItemTransformer
    {
        void transform(PoseStack poseStack, int pass, int passCount);
    }
}
