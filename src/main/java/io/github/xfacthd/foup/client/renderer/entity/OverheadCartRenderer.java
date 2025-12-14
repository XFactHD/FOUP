package io.github.xfacthd.foup.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.entity.OverheadCartState;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

public final class OverheadCartRenderer extends EntityRenderer<OverheadCartEntity, OverheadCartRenderState>
{
    private static final Identifier TEXTURE = Utils.rl("textures/entity/overhead_cart.png");
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
    private final ItemModelResolver itemModelResolver;

    public OverheadCartRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new OverheadCartModel(context.bakeLayer(OverheadCartModel.LAYER_LOCATION));
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(OverheadCartRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - renderState.yRot));
        poseStack.translate(0, 1.5F, 0);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        submitNodeCollector.submitModel(model, renderState, poseStack, model.renderType(TEXTURE), renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);

        ItemStackRenderState foupContent = renderState.foupContent;
        if (!foupContent.isEmpty())
        {
            renderFoupContents(foupContent, renderState.foupContentSize, poseStack, submitNodeCollector, renderState.lightCoords);
        }

        poseStack.popPose();

        super.submit(renderState, poseStack, submitNodeCollector, camera);
    }

    private void renderFoupContents(ItemStackRenderState renderState, int stackSize, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight)
    {
        renderFoupContents(renderState, stackSize, poseStack, submitNodeCollector, model.foup.x, model.foup.y + model.hoistWire.yScale, model.foup.z, packedLight, true);
    }

    public static void renderFoupContents(
            ItemStackRenderState renderState,
            int stackSize,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
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
        if (renderState.usesBlockLight())
        {
            poseStack.translate(0, Y_OFFSET_3D, 0);

            passes = Math.min((int) Math.ceil(stackSize / 16F), 4);
            transformer = TRANSFORMER_3D;
        }
        else
        {
            poseStack.translate(0, Y_OFFSET_FLAT, 0);
            poseStack.mulPose(FLAT_PRE_ROTATION);

            passes = (int) Math.ceil(stackSize / 8F);
            transformer = TRANSFORMER_FLAT;
        }

        for (int i = 0; i < passes; i++)
        {
            poseStack.pushPose();
            transformer.transform(poseStack, i, passes);
            renderState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
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
        renderState.state = cart.getState();
        renderState.actionStart = cart.getActionStart();
        renderState.actionDuration = cart.getActionDuration();
        renderState.heightDiff = cart.getHeightDiff();

        renderState.foupContent.clear();
        ItemStack foupContent = cart.getFoupContentClient();
        if (!foupContent.isEmpty())
        {
            itemModelResolver.updateForTopItem(renderState.foupContent, foupContent, ItemDisplayContext.FIXED, null, null, 0);
            renderState.foupContentSize = foupContent.getCount();
        }
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
