package io.github.xfacthd.foup.client.renderer.special;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.xfacthd.foup.client.renderer.PipelineModifiers;
import io.github.xfacthd.foup.client.util.ClientUtils;
import io.github.xfacthd.foup.client.util.GhostVertexConsumer;
import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.block.AbstractOverheadRailBlock;
import io.github.xfacthd.foup.common.blockentity.OverheadRailStationBlockEntity;
import io.github.xfacthd.foup.common.data.StationType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.function.Function;

public final class OverheadRailInfoRenderer
{
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Direction[] HORIZONTAL_DIRECTIONS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
    private static final int GHOST_OPACITY = 170;
    private static final Function<@Nullable StationType, @Nullable Component> STATION_FORMATTER = type ->
            type != null && type != StationType.UNKNOWN ? type.getTranslation() : null;
    private static final ObjectList<BlockModelPart> SCRATCH_PART_LIST = new ObjectArrayList<>();

    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
        {
            if (Objects.requireNonNull(mc.player).isSpectator()) return;
            if (!(mc.hitResult instanceof BlockHitResult hitResult)) return;
            if (hitResult.getType() != HitResult.Type.BLOCK) return;

            ItemStack stack = mc.player.getMainHandItem();
            if (stack.is(FoupContent.ITEM_RAIL_INSPECTOR))
            {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = Objects.requireNonNull(mc.level).getBlockState(pos);
                if (state.getBlock() instanceof AbstractOverheadRailBlock)
                {
                    renderRailInfoArround(event.getPoseStack(), event.getCamera(), mc.font, mc.level, pos, state, false);
                }
            }
            else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AbstractOverheadRailBlock block)
            {
                BlockPlaceContext context = new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, stack, hitResult);
                BlockState state = block.getStateForPlacement(context, true);
                BlockPos pos = context.getClickedPos();
                if (canPlaceAt(Objects.requireNonNull(mc.level), pos, state, context, mc.player))
                {
                    renderRailInfoArround(event.getPoseStack(), event.getCamera(), mc.font, mc.level, pos, state, true);
                }
            }
        }
    }

    private static boolean canPlaceAt(Level level, BlockPos pos, @Nullable BlockState state, BlockPlaceContext ctx, Player player)
    {
        if (level.getBlockState(pos).canBeReplaced(ctx))
        {
            return state == null || level.isUnobstructed(state, pos, CollisionContext.of(player));
        }
        return false;
    }

    private static void renderRailInfoArround(
            PoseStack poseStack, Camera camera, Font font, ClientLevel level, BlockPos pos, @Nullable BlockState state, boolean renderGhost
    )
    {
        RenderSystem.pushPipelineModifier(PipelineModifiers.NO_DEPTH_TEST);

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer quadBuilder = buffers.getBuffer(ClientUtils.INFO_QUADS);
        if (state != null)
        {
            if (renderGhost)
            {
                Vec3 offset = Vec3.atLowerCornerOf(pos).subtract(camera.getPosition());
                poseStack.pushPose();
                poseStack.translate(offset.x, offset.y, offset.z);

                RenderType bufferType = NeoForgeRenderTypes.TRANSLUCENT_ON_PARTICLES_TARGET.get();
                VertexConsumer builder = new GhostVertexConsumer(buffers.getBuffer(bufferType), GHOST_OPACITY);

                SCRATCH_PART_LIST.clear();
                BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                BlockStateModel model = blockRenderer.getBlockModel(state);
                model.collectParts(level, pos, state, RANDOM, SCRATCH_PART_LIST);

                blockRenderer.renderBatched(state, pos, level, poseStack, $ -> builder, false, SCRATCH_PART_LIST);

                poseStack.popPose();
            }

            renderRailInfo(poseStack, camera, buffers, quadBuilder, font, level, pos, state, !renderGhost);
        }

        for (BlockPos adjPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1)))
        {
            if (adjPos.equals(pos)) continue;

            BlockState adjState = level.getBlockState(adjPos);
            if (adjState.getBlock() instanceof AbstractOverheadRailBlock)
            {
                renderRailInfo(poseStack, camera, buffers, quadBuilder, font, level, adjPos, adjState, true);
            }
        }

        buffers.endBatch(ClientUtils.INFO_QUADS);
        if (renderGhost && state != null)
        {
            buffers.endBatch(NeoForgeRenderTypes.TRANSLUCENT_ON_PARTICLES_TARGET.get());
        }

        RenderSystem.popPipelineModifier();
    }

    private static void renderRailInfo(
            PoseStack poseStack,
            Camera camera,
            MultiBufferSource.BufferSource buffers,
            VertexConsumer builder,
            Font font,
            Level level,
            BlockPos pos,
            BlockState state,
            boolean stationInfo
    )
    {
        poseStack.pushPose();

        Vec3 offset = Vec3.atCenterOf(pos).add(0, .25, 0).subtract(camera.getPosition());
        poseStack.translate(offset.x, offset.y, offset.z);

        for (Direction dir : HORIZONTAL_DIRECTIONS)
        {
            AbstractOverheadRailBlock block = (AbstractOverheadRailBlock) state.getBlock();
            if (block.isEntrySide(state, dir))
            {
                renderArrow(poseStack, builder, dir, -.05F, false);
            }
            else if (block.isExitSide(state, dir))
            {
                renderArrow(poseStack, builder, dir, -.05F, true);
            }
        }

        if (stationInfo && state.is(FoupContent.BLOCK_RAIL_STATION) && level.getBlockEntity(pos) instanceof OverheadRailStationBlockEntity station)
        {
            renderStationInfo(poseStack, camera, buffers, font, station.getName(), station.getLinkedType(), STATION_FORMATTER);
        }

        poseStack.popPose();
    }

    public static void renderArrow(PoseStack poseStack, VertexConsumer builder, Direction dir, float start, boolean arrow)
    {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(dir.toYRot()));

        Matrix4f pose = poseStack.last().pose();
        float end = arrow ? .4F : .5F;
        builder.addVertex(pose, -.05F, 0, start).setColor(0xFF0000FF);
        builder.addVertex(pose, -.05F, 0,   end).setColor(0xFF0000FF);
        builder.addVertex(pose,  .05F, 0,   end).setColor(0xFF0000FF);
        builder.addVertex(pose,  .05F, 0, start).setColor(0xFF0000FF);

        if (arrow)
        {
            builder.addVertex(pose, -.15F, 0, .35F).setColor(0xFF0000FF);
            builder.addVertex(pose,    0F, 0,  .5F).setColor(0xFF0000FF);
            builder.addVertex(pose,    0F, 0,  .5F).setColor(0xFF0000FF);
            builder.addVertex(pose,  .15F, 0, .35F).setColor(0xFF0000FF);
        }

        poseStack.popPose();
    }

    public static void renderStationInfo(
            PoseStack poseStack,
            Camera camera,
            MultiBufferSource.BufferSource buffers,
            Font font,
            String name,
            @Nullable StationType type,
            Function<@Nullable StationType, @Nullable Component> formatter
    )
    {
        poseStack.pushPose();

        poseStack.translate(0, .5, 0);
        poseStack.mulPose(camera.rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.scale(1F/40F, 1F/40F, 1);

        Component typeName = formatter.apply(type);

        Matrix4f pose = poseStack.last().pose();
        font.drawInBatch(name, -(font.width(name) / 2F), -9, 0xFFBB00FF, false, pose, buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        if (typeName != null)
        {
            font.drawInBatch(typeName, -(font.width(typeName) / 2F), 1, 0xFFBB00FF, false, pose, buffers, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }

        poseStack.popPose();
    }

    private OverheadRailInfoRenderer() { }
}
