package io.github.xfacthd.foup.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.client.renderer.entity.OverheadCartRenderer;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import io.github.xfacthd.foup.common.blockentity.FoupStorageInterfaceBlockEntity;
import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.component.ItemContents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FoupStorageInterfaceRenderer extends ExtractingBlockEntityRenderer<FoupStorageInterfaceBlockEntity, FoupStorageInterfaceRenderState>
{
    private static final ResourceLocation DOOR_TEXTURE = ResourceLocation.withDefaultNamespace("block/vault_top");
    private static final float MIN_XZ = 3F/16F;
    private static final float MAX_XZ = 13F/16F;
    private static final float MAX_Y = 1.95F/16F;
    private static final float TOTAL_TIME = AbstractCartInteractorBlockEntity.State.INTERACTING.getDuration(StationType.STORAGE);
    private static final float DELAY_TIME = 5F;
    private static final float FOUP_TIME = (TOTAL_TIME - (DELAY_TIME * 2F)) / 2F;
    private static final float DOOR_TIME = (TOTAL_TIME - FOUP_TIME - (DELAY_TIME * 2F)) / 2F;
    private static final float DOOR_END_OPEN = DOOR_TIME;
    private static final float DOOR_START_CLOSE = FOUP_TIME + (DELAY_TIME * 2F) + DOOR_TIME;
    private static final float FOUP_START_RAISE = DOOR_TIME + DELAY_TIME;
    private static final float FOUP_END_LOWER = DOOR_TIME + DELAY_TIME + FOUP_TIME;
    private static final float FOUP_BASE_OFFSET = -1F/16F;
    private static final float FOUP_MOVE_DIST = 11F/16F;

    @Nullable
    private static TextureAtlasSprite sprite;

    private final ItemModelResolver itemModelResolver;

    public FoupStorageInterfaceRenderer(BlockEntityRendererProvider.Context ctx)
    {
        this.itemModelResolver = ctx.getItemModelResolver();
    }

    @Override
    public void render(FoupStorageInterfaceRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, Vec3 camera)
    {
        if (sprite == null) return;

        long start = renderState.actionStart;
        float time = computeTime(renderState.gameTime, start, renderState.partialTick);
        if (time < DOOR_END_OPEN || time > DOOR_START_CLOSE)
        {
            VertexConsumer buffer = bufferSource.getBuffer(Sheets.solidBlockSheet());
            PoseStack.Pose pose = poseStack.last();

            float factor = time > DOOR_START_CLOSE ? (TOTAL_TIME - time) : time;
            factor /= DOOR_TIME;
            float off = (.5F - MIN_XZ) * factor;

            float minU = sprite.getU(MIN_XZ + off);
            float cenU = sprite.getU(.5F);
            float maxU = sprite.getU(MAX_XZ - off);
            float minV = sprite.getV(MIN_XZ);
            float maxV = sprite.getV(MAX_XZ);

            float cenMin = .5F - off;
            buffer.addVertex(pose, MIN_XZ, MAX_Y, MIN_XZ).setColor(-1).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, MIN_XZ, MAX_Y, MAX_XZ).setColor(-1).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, cenMin, MAX_Y, MAX_XZ).setColor(-1).setUv(cenU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, cenMin, MAX_Y, MIN_XZ).setColor(-1).setUv(cenU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);

            float cenMax = .5F + off;
            buffer.addVertex(pose, cenMax, MAX_Y, MIN_XZ).setColor(-1).setUv(cenU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, cenMax, MAX_Y, MAX_XZ).setColor(-1).setUv(cenU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, MAX_XZ, MAX_Y, MAX_XZ).setColor(-1).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, MAX_XZ, MAX_Y, MIN_XZ).setColor(-1).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);

            if (time > 0F)
            {
                float cenMinU = sprite.getU(6F/16F);
                buffer.addVertex(pose, cenMin, MAX_Y, MIN_XZ).setColor(-1).setUv(   cenU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
                buffer.addVertex(pose, cenMin, MAX_Y, MAX_XZ).setColor(-1).setUv(   cenU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
                buffer.addVertex(pose, cenMin,    0F, MAX_XZ).setColor(-1).setUv(cenMinU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);
                buffer.addVertex(pose, cenMin,    0F, MIN_XZ).setColor(-1).setUv(cenMinU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1, 0, 0);

                float cenMaxU = sprite.getU(10F/16F);
                buffer.addVertex(pose, cenMax,    0F, MIN_XZ).setColor(-1).setUv(cenMaxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
                buffer.addVertex(pose, cenMax,    0F, MAX_XZ).setColor(-1).setUv(cenMaxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
                buffer.addVertex(pose, cenMax, MAX_Y, MAX_XZ).setColor(-1).setUv(   cenU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
                buffer.addVertex(pose, cenMax, MAX_Y, MIN_XZ).setColor(-1).setUv(   cenU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1, 0, 0);
            }
        }

        float factor = 0F;
        StationAction action = start > -1 ? renderState.activeAction : null;
        if (action == StationAction.LOAD && time > FOUP_START_RAISE)
        {
            factor = Math.min(time - FOUP_START_RAISE, FOUP_TIME) / FOUP_TIME;
        }
        else if (action == StationAction.UNLOAD && time < FOUP_END_LOWER)
        {
            factor = 1F - Math.max((time - FOUP_START_RAISE) / FOUP_TIME, 0F);
        }
        if (factor > 0F)
        {
            poseStack.pushPose();
            poseStack.translate(.5, FOUP_BASE_OFFSET + (FOUP_MOVE_DIST * factor), .5);

            poseStack.pushPose();
            poseStack.scale(1.995F, 1.995F, 1.995F);
            renderState.inflightFoup.render(poseStack, bufferSource, light, overlay);
            poseStack.popPose();

            ItemStackRenderState foupContent = renderState.inflightFoupContent;
            if (!foupContent.isEmpty())
            {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180F - renderState.cartRotation));

                OverheadCartRenderer.renderFoupContents(foupContent, renderState.inflightFoupContentSize, poseStack, bufferSource, 0, -1, 0, light, false);

                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }

    @Override
    public FoupStorageInterfaceRenderState createRenderState()
    {
        return new FoupStorageInterfaceRenderState();
    }

    @Override
    public void extractRenderState(FoupStorageInterfaceBlockEntity be, FoupStorageInterfaceRenderState renderState, float partialTick)
    {
        super.extractRenderState(be, renderState, partialTick);

        renderState.gameTime = Objects.requireNonNull(be.getLevel()).getGameTime();
        renderState.actionStart = be.getActionStart();
        renderState.activeAction = be.getActiveAction();
        renderState.cartRotation = be.getCartRotation();

        itemModelResolver.updateForTopItem(renderState.inflightFoup, FoupContent.ITEM_FOUP.toStack(), ItemDisplayContext.FIXED, null, null, 0);

        renderState.inflightFoupContent.clear();
        ItemStack foup = be.getFoupInFlight();
        ItemStack foupContent;
        if (foup != null && !(foupContent = foup.getOrDefault(FoupContent.DC_TYPE_ITEM_CONTENTS, ItemContents.EMPTY).stack()).isEmpty())
        {
            itemModelResolver.updateForTopItem(renderState.inflightFoupContent, foupContent, ItemDisplayContext.FIXED, null, null, 0);
        }
    }

    private static float computeTime(long gameTime, long start, float partialTick)
    {
        if (start != -1)
        {
            float diff = (float) (gameTime - start);
            return Math.clamp(diff + partialTick, 0F, TOTAL_TIME);
        }
        return 0F;
    }

    @SuppressWarnings("deprecation")
    public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event)
    {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS))
        {
            sprite = event.getAtlas().getSprite(DOOR_TEXTURE);
        }
    }
}
