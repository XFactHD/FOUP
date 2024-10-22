package io.github.xfacthd.foup.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import io.github.xfacthd.foup.common.blockentity.FoupStorageInterfaceBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import org.jetbrains.annotations.Nullable;

public final class FoupStorageInterfaceRenderer implements BlockEntityRenderer<FoupStorageInterfaceBlockEntity>
{
    private static final ResourceLocation DOOR_TEXTURE = ResourceLocation.withDefaultNamespace("block/vault_top");
    private static final float MIN_XZ = 2.5F/16F;
    private static final float MAX_XZ = 13.5F/16F;
    private static final float MAX_Y = 1.55F/16F;
    private static final float TOTAL_TIME = AbstractCartInteractorBlockEntity.State.INTERACTING.getDuration(AbstractCartInteractorBlockEntity.Type.STORAGE);
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

    private final ItemRenderer itemRenderer;

    public FoupStorageInterfaceRenderer(BlockEntityRendererProvider.Context ctx)
    {
        itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(FoupStorageInterfaceBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay)
    {
        Level level = be.getLevel();
        if (level == null || sprite == null) return;

        long start = be.getActionStart();
        float time = computeTime(level, start, partialTick);
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
        AbstractCartInteractorBlockEntity.Action action = start > -1 ? be.getActiveAction() : null;
        if (action == AbstractCartInteractorBlockEntity.Action.LOAD && time > FOUP_START_RAISE)
        {
            factor = Math.min(time - FOUP_START_RAISE, FOUP_TIME) / FOUP_TIME;
        }
        else if (action == AbstractCartInteractorBlockEntity.Action.UNLOAD && time < FOUP_END_LOWER)
        {
            factor = 1F - Math.max((time - FOUP_START_RAISE) / FOUP_TIME, 0F);
        }
        if (factor > 0F)
        {
            poseStack.pushPose();
            poseStack.translate(.5, FOUP_BASE_OFFSET + (FOUP_MOVE_DIST * factor), .5);
            poseStack.scale(1.995F, 1.995F, 1.995F);
            itemRenderer.renderStatic(FoupContent.ITEM_FOUP.toStack(), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 0);
            poseStack.popPose();
        }
    }

    private static float computeTime(Level level, long start, float partialTick)
    {
        if (start != -1)
        {
            float diff = (float) (level.getGameTime() - start);
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
